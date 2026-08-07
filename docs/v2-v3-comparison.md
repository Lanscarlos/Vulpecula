# Vulpecula v2 → v3 业务与语句结构对照

> 基准：`E:\Minecraft\Develop\Vulpecula-v2`（单模块，2.x）与 `E:\Minecraft\Develop\Vulpecula`（多模块，分支 `v3`，3.0.0）
>
> 用途：为「把 v2 的语句完全以 v3 形式重新实现」提供事实基础与改写规则。

---

## 一、总体差异速览

| 维度 | v2 | v3 |
| --- | --- | --- |
| 工程结构 | 单模块 `src/main/kotlin`，一个 jar | 多模块（`common-*` / `module-*` / `extension-*` / `platform-*` / `plugin-*` / `workflow-*`） |
| 语句所在位置 | 全部编译进主 jar | `module-*` 进主 jar；`extension-action-*` / `extension-property-*` **独立打包**，运行时从 `plugins/Vulpecula/extension/` 动态加载 |
| 语句定义单元 | **函数** 上的 `@BacikalParser("id")`，返回 `ScriptActionParser<*>` | **类** 上的 `@Parser("a.b.c")`，实现 `ClassActionResolver`，唯一 `resolve` 函数 |
| 参数声明 | `combine(LiveData…) { … }` 手工组合 | `resolve(...)` 的 Kotlin 形参 + 参数注解，反射解析 |
| 语句名注册 | `action-registry.yml` 手工映射 `namespace:name`，可 `disable` | 由 id 自动推导，无注册配置文件 |
| 多级语句 | 每个根语句手写 `QuestAction` + `Resolver` 注册表 + `when(nextToken())` | id 中的 `.` 自动构建 `ComplexActionParser` 层级树 |
| 数据流转 | `>>` 管道 + 帧变量 `@Transfer` | 上下文变量 `@VULPECULA_CONTEXT_XXX` + `X.switch` / `X.context` |
| 类型转换 | `LiveData` 的 `liveInt` / `liveEntity` … 扩展属性 | `common-core` 的 `Applicative` 体系（`ApplicativeRegistry` 按 Class 查表） |
| 属性定义 | `BacikalGenericProperty<T>` 抽象类，返回 `OpenResult` | `BacikalProperty<T>` 接口，直接返回值 / 抛 `NoSuchPropertyException`；外面套一层 `BacikalPropertyResolver` |
| 配置服务 | `DynamicConfig` + `automatic-reload` 逐项开关 | `Configs.register(ConfigService)` + SHA-256 指纹比对 + `FileWatcher` |
| 事件调度 | **dispatcher + handler 两段式** | **单文件 dispatcher**，`Pipeline` 责任链，`execute: 'script@xxx'` |
| 文本输出 | TabooLib `sendLang` / 硬编码字符串混用 | 强制走 `common-core` 的 `Lang` 全局枚举 |
| 命令 | 手写 `CommandVulpecula` 子命令 | `CommandScanner` 扫 `@CommandBody` 的 `SimpleCommandBody`，自动挂 `/vul` |
| 语句自省 | 无 | `ComplexActionParser.buildStructure` 打印语句树，`common-diagram` 渲染 |

---

## 二、语句结构（核心）

### 2.1 v2 的语句模型

**注册链路**

```
BacikalRegistry : ClassInjector（LifeCycle.LOAD）
  ├─ visit(method)      → 找 @BacikalParser + 返回值 ScriptActionParser → registerAction(id, parser)
  └─ visitStart(class)  → 找 @BacikalProperty + BacikalGenericProperty  → registerProperty(...)

registerAction(id, parser)
  ├─ 查 action-registry.yml：不存在 → warning 并跳过；disable: true → 跳过
  ├─ local:  [ "kether:v-canvas", "vulpecula:canvas" ] → Kether.scriptRegistry.registerAction
  └─ remote: [ … ] → getOpenContainers().call(REMOTE_ADD_ACTION)
```

要点：**语句 id 与脚本里写的语句名完全解耦**，映射在 `action-registry.yml` 里，一个 id 可以挂多个命名空间多个名字，也可以整条禁用。改这个文件必须重启。

**单条语句的写法**

```kotlin
object ActionMemory {
    @BacikalParser("memory")
    fun parser() = bacikal {
        combine(
            text("key"),
            optional("to",   then = any(), def = "@GET"),
            optional("by", "with", then = any()),
            optional("using", then = text("default"), def = "default")
        ) { key, value, unique, storage -> /* ScriptFrame.() -> R */ }
    }
}
```

- `combine(p1..p16, func)` 同步、`combineOf(...)` 返回 `CompletableFuture`、`discrete { }` 无参数。
- 参数类型是 `LiveData<T>`，构造器：`bool() int() long() float() double() text() multiline() literal() any() list() action() vector() location() color() entity() player() item() inventory()`，以及各自的 `xxxOrNull()`。
- 参数修饰（`BacikalReader` 上的方法或 `LiveData` 的链式调用）：

  | 写法 | 语义 |
  | --- | --- |
  | `trim("to", then = X)` | 可选吃掉字面量 `to`，不影响取值 |
  | `expect("to", then = X)` | **必须**出现 `to`，否则 `LoadError.NOT_MATCH` |
  | `optional("by", then = X)` | 出现 `by` 才读，返回 `T?` |
  | `optional("by", then = X, def = v)` | 同上，缺省返回 `v` |
  | `argument("prefix", then = X[, def])` | 附加参数，脚本写 `-prefix <值>`（**单横线**），底层 `LiveDataProxy` |

- 附加参数识别正则 `-\D+`；`applyLiveData` 的读取顺序是：顺序读非附加参数 → 遇到第一个附加参数后记断点 → 循环读所有 `-xxx` → 再读断点之后剩余的位置参数。
- 多参数在运行期通过 `applicative(p1.accept(frame), p2.accept(frame), …)` **同时组合**后 `thenApply`。

**复合语句（entity / item / target / vector / location / inventory / illusion / event / canvas）**

每个根语句是一个手写的 `QuestAction<Any?>`：

```kotlin
class ActionItem : QuestAction<Any?>() {
    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            val next = reader.nextToken()
            handlers += registry[next.lowercase()]?.resolve(Reader(next, reader, handlers.isEmpty()))
                ?: error("Unknown sub action \"$next\" at item action.")
            if (handlers.lastOrNull() !is Transfer) { /* 管道关闭，禁止再写 >> */ break }
        } while (reader.hasNextToken(">>"))
        return this
    }
}
```

- 子语句由 `ClassInjector` 扫描 `ActionItem.Resolver` 的实现类，按 `override val name: Array<String>` 注册进 `registry`。
- `Reader.source()`：**根位置**读一个 `item()` / `entity()` / …；**非根位置**从帧变量 `@Transfer` 取上一环的结果。
- `Handler` vs `Transfer`：`Transfer` 返回宿主类型并可继续 `>>`；普通 `Handler` 关闭管道（后面再写 `>>` 直接编译报错）。
- 第三级分发再手写一层 `when (reader.nextToken())`，`else -> reader.reset()` 分支作为「读取器」缺省行为：

```kotlin
// item lore &item add &line to &index
when (reader.nextToken()) {
    "add", "insert" -> reader.transfer { combine(source, text(), optional("to", then = int()),
                                                 optional("before", …), optional("after", …)) { … } }
    "modify", "set" -> …
    "remove", "rm"  -> …
    "clear"         -> …
    "reset"         -> …
    else -> { reader.reset(); reader.handle { combine(source) { it.itemMeta?.lore } } }
}
```

`target` 更特殊，还接受 `@selector` 简写与 `selector|select|sel` / `filter` / `foreach` 三类前缀，缺省回落到 `foreach`。

---

### 2.2 v3 的语句模型

**注册链路**

```
BacikalScanner   (LOAD)        扫描主 jar 内的 @Parser / @Property → NativeExtension
ExtensionScanner (LOAD, 优先级6) 扫描 plugins/Vulpecula/extension/*.jar → ExternalExtension，复用 visitClass
BacikalRegistry  (LOAD, 优先级8) 统一注册进 Kether.scriptRegistry + 远程注册
```

解析失败的语句不中断启动，包装成 `ExceptionalActionParser` 保留错误，可用命令查看。

**单条语句的写法**

```kotlin
@Parser("entity.damage")
object ActionEntityDamage : ClassActionResolver {
    fun resolve(
        frame: BacikalFrame,                          // 自动注入，不消耗 token
        damage: Double,                               // 必需位置参数
        @Optional(["by"]) damager: Entity? = null     // 可选前缀参数
    ) {
        val entity = ActionEntity.getContext(frame)
        require(entity is Damageable) { Lang.XXX.asText(console(), entity.type.name) }
        entity.damage(damage, damager)
    }
}
```

**注册期强制校验**（`ClassActionConstructor` / `ClassActionFunction`）：

- 必须实现 `ClassActionResolver`。
- **有且仅有一个**名为 `resolve` 的函数。
- 要么是 `object`，要么只有一个构造函数且参数为空或仅 `BacikalReader`。
- `@Optional` / `@Additional` 参数**必须**可空或有默认值；前缀不能为空、不能是纯数字。

**参数类型的特殊处理**（`ClassActionParameter.read`）：

| 形参类型 | 行为 |
| --- | --- |
| `BacikalFrame` | 直接注入当前帧，**不消耗 token** |
| `Player` / `ProxyPlayer` | 取脚本执行者，**不消耗 token**；非空且取不到则报错 |
| `ParsedAction` | 原样传递未执行的动作（延迟执行用） |
| `org.bukkit.Location` | 走 `LocationApplicative` 再 `toBukkitLocation()` |
| `Any` | 不做转换 |
| 其他 | `ApplicativeRegistry.getApplicative(type).convert(value)`；枚举类自动生成 `EnumApplicative` |

**参数修饰符注解**：

| 注解 | 脚本形态 | 约束 |
| --- | --- | --- |
| 无 | `<值>` | 位置必需 |
| `@Expected(["to"])` | `to <值>` | 前缀必须出现 |
| `@Optional(["by"])` | `[by <值>]` | 可空或有默认值 |
| `@Additional(["owner"])` | `--owner <值>` （**双横线**） | 可空或有默认值；可乱序出现在参数序列中间 |

附加参数识别正则 `--\D+`，读取顺序与 v2 一致（顺序参数 → 断点 → 循环读 `--xxx` → 断点后剩余）；未被赋值的参数替换成 `DefaultAction` 并累加 mask，最终调用 Kotlin 合成的 `resolve$default`。

**层级 id 与 Kether 命名**（`BacikalRegistry.registerActionParser` + `registerAction`）：

`@Parser("item.lore.get")` 会：
1. 自动补齐父节点 `item`、`item.lore` 为 `ComplexActionParser`（父路径若已存在末端语句则直接报错）；
2. 把末端语句以 `id.replace('.', '-')` 注册进 Kether，即 `item-lore-get`；
3. 父节点自身也注册为 `item`、`item-lore`。

因此下面两种写法等价：

```
item lore get 1
item-lore-get 1
```

⚠️ **`aliases` 的作用域要注意**：有层级的 id 注册进 Kether 的名字**只有** `a-b-c` 一个，`name` 与 `aliases` 只在父 `ComplexActionParser` 的子节点分发表里生效。所以 `@Parser("item.amount.increase", aliases = ["inc", "add"])` 的实际可用写法是 `item amount inc` / `item amount add` / `item-amount-increase`，但**没有** `item-amount-inc`。

**运行期执行**：参数按声明顺序**串行**执行（`CompletableFuture` fold 链，前一个抛异常直接中断），与 v2 的并行组合不同。`resolve` 返回 `CompletableFuture<T>` 表示异步语句。

**元数据依赖**：参数名来自 Kotlin `@Metadata`。`workflow-metadata` 在构建期用 ASM 把 `@Metadata.d1` Base64 写进 `resources/metadata/<全限定类名>.metadata`，运行时 `ClassActionFunction.getMetadata` 读回并用 `JvmProtoBufUtil` 解析。**新增语句类必须走 `generateMetadata`，否则启动时报 `Metadata xxx not found`。**

---

### 2.3 数据流转：`>>` 管道 → 上下文变量

这是对脚本使用者影响最大的一处变更。

**v2**

```
item &source >> lore add "&7新的一行" >> amount set 64 >> give &player
```

- 根语句读一个源对象；`Transfer` 型子语句把结果写进帧变量 `@Transfer` 传给下一环。
- 非 `Transfer` 子语句（如 `lore` 的缺省 getter）关闭管道。

**v3**

```
item switch &source
item lore append "&7新的一行"
item amount set 64
item context      # 需要取回当前物品时
```

- `X.switch <value>` 写入上下文；`X.context` 读回上下文。
- 上下文键：`@VULPECULA_CONTEXT_ENTITY`、`@VULPECULA_CONTEXT_ITEM`、`@VULPECULA_CONTEXT_TARGET`、`@VULPECULA_CONTEXT_MEMORY`。
- `ActionEntity.getContext` 在上下文为空时**回落到脚本执行者**并写回上下文；`ActionItem` 等无此回落。
- 上下文的生命周期是**帧变量**，跨语句共享；`>>` 的「一次性链式」语义没有等价物 —— 移植时凡是 v2 用 `>>` 表达的一串操作，v3 都要拆成「先 switch，再逐条语句」。

---

### 2.4 参数类型转换：LiveData → Applicative

| v2 | v3 |
| --- | --- |
| `LiveData.Companion` 里的 `Any.liveInt` / `liveEntity` / `liveItemStack` / `liveLocation` / `liveColor` / `liveVector` / `liveInventory` / `livePlayer` / `liveBoolean` / `liveStringList` … | `common-core/applicative` 下一类一个 `AbstractApplicative` 实现，`ApplicativeRegistry`（`ClassVisitor`，LOAD，优先级 -4）自动扫描注册 |
| 转换失败返回 `null`，由 `int(def, display)` 之类决定报什么错 | 转换失败抛 `TypeConversionException` / `ValueConversionException` / `NullValueException` |
| 显示名靠 `display` 参数手写 | 用参数名（来自 Metadata）自动生成错误信息 |

已有的 Applicative：`Boolean Color Double Entity Enum Float Int Inventory ItemStack List Location Long Map Player String Vector`。

> 移植时若 v2 语句用到了 `liveXxx` 里有但 Applicative 里没有的类型（例如 Bukkit `Block`、`World`），需要先补一个 `AbstractApplicative` 实现。

---

### 2.5 属性（Property）

| | v2 | v3 |
| --- | --- | --- |
| 注解 | `@BacikalProperty(id, bind, shared = true)` | `@Property(id = "", bind = T::class)` |
| 基类 | `abstract class BacikalGenericProperty<T>(id) : ScriptProperty<T>("vulpecula.$id.operator")` | `interface BacikalProperty<T>` |
| 读 | `readProperty(instance, key): OpenResult` | `readProperty(instance, key): Any?`，未命中抛 `NoSuchPropertyException` |
| 写 | `writeProperty(instance, key, value): OpenResult` | `writeProperty(instance, key, value)`，无返回 |
| 深路径 | 基类拆 `a.b.c`，逐级 `readGenericProperty` 遍历所有 `ScriptProperty` | `BacikalPropertyResolver` 统一处理，**支持 `a.b?.c` 空安全后缀** |
| 挂载 Kether | 每个 Property 自己就是 `ScriptProperty` | 每个绑定类生成**一个** `BacikalPropertyResolver`，内部按类继承关系（具体 → 抽象）聚合所有可用 `BacikalProperty`，并缓存 key → property |
| 开关 | `property-registry.yml` 的 `disable` / `shared` | 无配置文件 |
| 分发 | 与主 jar 同包 | `extension-property-*` 独立 jar |

v3 新增 `module-bacikal/develop/PropertyClassGenerator`，按 `resources/template/property.kt` 模板反射生成属性样板到 `plugins/Vulpecula/develop/`，可用来批量起 v2 属性的骨架。

---

## 三、v2 语句全量清单与 v3 移植状态

图例：✅ 已移植 ／ 🔶 部分移植或语义变化 ／ ❌ 未移植

### 3.1 entity（v3 `extension-action-entity`）

| v2 | v3 | 状态 | 备注 |
| --- | --- | --- | --- |
| `entity <源> >> …` 根参数 | `entity.switch` | ✅ | 语法由「根读源」变为「显式切换上下文」 |
| — | `entity.context` | 🆕 | 读回上下文，空则回落到脚本执行者 |
| `damage` / `dmg` | `entity.damage` | ✅ | `by` 由 `optional` → `@Optional(["by"])` |
| `potion` add/set | `entity.potion.set` | ✅ | add 与 set 合并 |
| `potion` remove/rm | `entity.potion.remove` | ✅ | |
| `potion` clear | `entity.potion.clear` | ✅ | |
| `potion` contains/has | `entity.potion.has` | ✅ | |
| — | `entity.potion.size` | 🆕 | |
| `teleport` / `tp` | `entity.teleport` | ✅ | |
| — | `entity.equipment.get` / `.set` | 🆕 | |

### 3.2 item（v3 `extension-action-item`）

| v2 | v3 | 状态 | 备注 |
| --- | --- | --- | --- |
| `item <源> >> …` | `item.switch` / `item.context` | ✅ | |
| `amount` add/give、sub/take、set/modify、current/cur、max | `item.amount.increase(inc,add)` / `.decrease(dec)` / `.set` / `.get` / `.maximum(max)` | ✅ | |
| `build` / `create` | `item.build` | ✅ | |
| `color` | `item.color.get` / `.set` / `.mix` | ✅ | v2 的 `potion color` 也归到这里 |
| `consume` | — | ❌ | |
| `destroy` | — | ❌ | |
| `drop` | — | ❌ | |
| `durability` add/fix、sub/take/damage、set、current、max | `item.durability.increase/.decrease/.set/.get/.maximum/.repair` | ✅ | v3 另有 `item.damage.*` 直接操作原生 damage 值 |
| — | `item.damage.get/.set/.increase/.decrease/.maximum` | 🆕 | |
| `enchantment` add/plus、sub/minus、modify/set、remove/rm、clear、has/contains、level/lvl | `item.enchantment.set(add)/.remove/.clear/.has/.get/.size` | 🔶 | v2 的 `add`（叠加等级）与 `sub` 需在 v3 用 get + set 组合，或另开语句 |
| `flag` add/plus、remove/rm、clear、has/contains | `item.flag.add/.remove/.has/.size` | 🔶 | **`clear` 缺失** |
| `give` | — | ❌ | |
| `lore` add/insert（含 `to` / `before` / `after` 定位）、modify/set、remove/rm、clear、reset | `item.lore.insert/.append(add)/.set/.remove(delete)/.clear/.override/.get/.size` | 🔶 | **语义收窄**：v2 支持按正则 `before`/`after` 定位插入，v3 `insert` 只接受行号；v2 索引 0 起，v3 **1 起**，`get`/`set` 支持 `*` / `all` |
| `match` | — | ❌ | |
| `modify` / `set`（一次性设置多字段） | — | ❌ | v3 拆散成各子域的 `.set` |
| `potion` add/plus、sub/minus、modify/set、remove/rm、clear、has/contains、color | `item.potion.set(add)/.remove/.clear/.has/.size` + `item.color.*` | 🔶 | 同 enchantment，`add`/`sub` 叠加语义丢失 |
| `tag` / `nbt` get、set、remove、has/contains、all | `item.tag.get/.set/.remove/.has` | ✅ | v2 的 `all` → v3 `item tag get *`；`get ... as <type>` 用 `@Optional(["as"])`；`set` 用 `@Expected(["to"])` |
| `unbreakable` / `unbreak` | `item.unbreakable.state/.enable/.disable` | ✅ | |
| — | `item.name.get` / `.set` | 🆕 | |

### 3.3 target（v3 `extension-action-target`）

| v2 | v3 | 状态 |
| --- | --- | --- |
| `@<selector>` 简写、`selector` / `select` / `sel` 前缀 | `target.select.*`（无 `@` 简写） | 🔶 |
| `select Self` | `target.select.self` | ✅ |
| `select Player` | — | ❌ |
| `select PlayerOnServer` / `PlayersOnServer` / `Server` | `target.select.server` | ✅ |
| `select` in-radius 系 | `target.select.in-radius` | ✅ |
| `select` in-ring 系 | — | ❌ |
| `select` nearest 系 | — | ❌ |
| `select` world 系 | `target.select.world` | ✅ |
| — | `target.select.in-box` | 🆕 |
| `filter type` | `target.filter.type` | ✅ |
| `filter instance` / `inst` | — | ❌ |
| `filter foreach` / `each` | — | ❌ |
| `foreach`（根级，缺省分支） | — | ❌ |

### 3.4 event（v3 `extension-action-event`）

| v2 | v3 | 状态 | 备注 |
| --- | --- | --- | --- |
| `cancel` | `event.cancel` | 🔶 | v3 写入 `@VULPECULA_CONTEXT_EVENT_STATUS = CANCELED`，由 `DefaultDispatcher` 消费 |
| `cancelled` | — | ❌ | |
| `name` | — | ❌ | |
| `wait` / `require` | — | ❌ | |
| — | `event.ignore` | 🆕 | 状态 `IGNORED`，终止后续 flow |

### 3.5 illusion（v3 `extension-action-illusion`）

v3 的 illusion 是**重新设计**的一套，与 v2 无对应关系。

| v2 | v3 | 状态 |
| --- | --- | --- |
| `switch` | — | ❌ |
| `health` | — | ❌ |
| `hologram` | — | ❌ |
| — | `illusion.glow` | 🆕 |
| — | `illusion.warning.set` / `.breathing` / `.clear`（`WarningEffect` 体系） | 🆕 |
| — | `illusion.worldborder` | 🆕 |

### 3.6 memory（v3 `extension-action-memory`）

| v2 | v3 | 状态 | 备注 |
| --- | --- | --- | --- |
| `memory <key> [to …] [by/with …] [using …]` 单条语句四态 | `memory.get` / `.set` / `.remove` / `.switch` | 🔶 | v2 用 `to @GET` / `@REMOVE` 哨兵值区分读写删，v3 拆成独立语句 |
| `using metadata` | — | ❌ | |
| `using luckperms` / `lp` | — | ❌ | |
| `using aboleth` / `abo` | — | ❌ | |
| 默认 cache / globalCache | `VulpeculaStorage` | ✅ | v3 抽象出 `MemoryStorage`，目前只实现 `vulpecula` |
| `by` / `with`（归属实体） | `@Additional(["owner"])` | ✅ | 默认值来自扩展配置 `default-owner` |
| `using`（存储容器） | `@Additional(["storage"])` / `memory.switch` | ✅ | 默认值来自扩展配置 `default-storage` |

### 3.7 完全未移植的语句族

| v2 语句 | 子语句数 | 说明 |
| --- | --- | --- |
| `vector` | 24（add/sub/mul/div/build/clone/angle/cross/dot/distance/distance2/length/length2/midpoint/modify/normalize/random/rotate-x/y/z/rotate-axis/rotate-axis-non-unit/rotate-euler） | 纯数学，移植成本低、收益高，建议优先 |
| `location` | 9（add/sub/mul/div/build/clone/distance/distance2/modify） | 同上 |
| `inventory` | 5（check/count/find/switch/take） | |
| `canvas` 家族 | `canvas` / `canvas-brush` / `canvas-draw` / `canvas-duration` / `canvas-fx` / `canvas-pattern` / `canvas-viewers` / `particles`，另含 7 种 fx、11 种 pattern/transformer | 体量最大，自带独立 `CanvasScriptContext` 与私有命名空间 `vulpecula-canvas`，与 v3 层级模型冲突最深 |
| 数学函数 | `pow` `sqrt` `ceil` `ln` `lg` `radian` `sin` `cos` `tan` `coerce` | 都是单参无前缀，v3 写起来近乎一行 |
| 流程 / 工具 | `if-else` `try-catch` `input` `regex` `sound` `tell` `tell-raw` `unicode` `function-call` | `if-else` / `try-catch` 依赖 v2 的 `ActionBlock`（`{ … }` 块），v3 `DefaultReader.readAction` 里的 `{` 分支目前是空壳（两个分支实现相同），需先补齐块解析 |
| 自省语句 | `vulpecula` `vulpecula-dispatcher` `vulpecula-schedule` `vulpecula-script` | v3 里 script/schedule 的调用改走配置侧的 `script@id` / `schedule@id`，是否还需要语句形式待定 |

---

## 四、业务模块对照

### 4.1 事件调度（dispatcher）

**v2：两段式**

```yaml
# dispatchers/xxx.yml
on-interact:
  listen: 'player-interact'        # 别名，经 listen-mapping.yml 解析
  priority: 'normal'
  ignore-cancelled: false
  pre-handle: |- …
  post-handle: [ { type: 'ke', content: '…' } ]
  baffle: { type: 'time', time: 200 }   # 或 { type: 'count', count: n }
  player-ref: '…'                  # 反射取玩家字段
```

```yaml
# handlers/xxx.yml
example-handler-1:
  bind: 'on-interact'   # 绑定到 dispatcher，支持多个
  priority: 8
  condition: '…'
  deny: '…'
  handle: |- …
  exception:
```

多个 handler 按 priority 排序后**合并编译**进 dispatcher 的脚本。

**v3：单文件 + Pipeline + ScriptFlow**

```yaml
listen-event: 'org.bukkit.event.entity.EntityShootBowEvent'   # 全限定类名
listen-priority: 'NORMAL'
listen-cancelled: false
weight: 1
rule:
  player-required: true
  baffle: 100ms
  baffle-cancel: true
pre-processing:  |- …      # ⚠️ 见下方风险点
post-processing: |- …
execute: 'script@example'
debug:
  config-auto-reload: true
```

- `handler` 概念**取消**，横切逻辑由 `Pipeline` 责任链承担（`BafflePipeline` / `ListPipeline` / `ReflexPlayerPipeline` / 各事件专用 Pipeline，`PipelineRegistry` 自动注册）。
- 事件不再走别名映射（`listen-mapping.yml` 取消），直接写类名。
- 执行流由 `ScriptFlow` 串起 pre → execute → post，前置处理通过 `@VULPECULA_CONTEXT_EVENT_STATUS` 回传 `CANCELED` / `IGNORED`。

### 4.2 定时任务（schedule）

| | v2 | v3 |
| --- | --- | --- |
| 类型 | 仅周期 | `type: cron` / `type: periodic` |
| 时间 | `start` / `end` / `period: '1h30m10s'` | periodic：`period` / `delay` / `max-duration` / `max-replication`；cron：`seconds/minutes/hours/days/months/years` 或 `cron` 表达式 |
| 启停 | `disable` | `auto-start`、`prototype`（允许多实例） |
| 执行者 | 无 | `sender: '@console'`，另有 `SenderSelector` 体系（console/self/player/online/world/area/range） |
| 执行内容 | `execute` 纯脚本 | `execute` 支持 `script@id` / `schedule@id` |

### 4.3 自定义脚本（script）

| v2 | v3 | 说明 |
| --- | --- | --- |
| `build-setting`（target-path / target-override / auto-compile / escape-unicode） | — | v3 取消「编译落盘到 `.compile/`」 |
| `namespace` | `namespace` | 保留 |
| `main` | `main` | 保留 |
| `variables` | `variables` | 保留 |
| `functions: { name: { args: [...], content: ... } }` | `functions: { name: \|- ... }` | 结构简化 |
| `fragments`（`$id` / `${id}` 替换） | `fragments` | 保留 |
| `condition` / `deny` | — | ❌ 取消，用脚本内 `if` |
| `exception: \|- …`（单块） | `exception: [ { catch: 'NullPointerException', handle: ... } ]` | 🆕 按异常类型分派 |
| — | `parameters` | 🆕 脚本形参 |
| — | `return-conversion: 'location'` | 🆕 返回值走 Applicative 转换 |
| — | `timeout` / `on-timeout` | 🆕 |

v3 另有 `ScriptFlow` / `ScriptTask` / `CompiledScript` / `NativeScript` / `ProxyScript` 的分层，v2 只有 `ExternalScript` + `ScriptWorkspace` + `ScriptCompiler`。

### 4.4 自定义命令（command）

| v2 | v3 |
| --- | --- |
| `disable` / `name` / `aliases` / `description` / `usage` / `permission` / `permission-default` / `permission-message` | `name` / `main` / `components`（元信息字段目前精简） |
| `components.<id>.parent` | 同 |
| `components.<id>.dynamic: 'file'` + `suggest: [...]` | `components.<id>.parameters: [ { name: 'xxx' } ]`，配 `Restrictor`（Int/Double）与 `Suggester`（Boolean/List/Material/OfflinePlayer/Player/World）类 |
| 节点模型内联在 `CustomCommand` | `MainNode` / `LiteralNode` / `ParameterNode` / `Node` 分层 |

### 4.5 配置、文本、命令基础设施

| | v2 | v3 |
| --- | --- | --- |
| 配置 | `DynamicConfig` + `bindConfigNode`，`config.yml` 里 `automatic-reload` 逐项布尔开关 | `ConfigService(id, name, directory, priority, callback)` 注册到 `Configs`；SHA-256 指纹比对分派 `onFileCreated/Modified/Deleted/Exception`；`FileWatcher` 配置项级自动重载；`#` 开头文件跳过 |
| 文本 | TabooLib `sendLang` / `asLangText` + 硬编码字符串 | **强制** `Lang` 全局枚举（`common-core`），`path` 由枚举名推导（`ACTION_ENTITY_NOT_FOUND` → `action-entity-not-found`）；各模块 `lang/zh_CN.yml` 由 `mergeResources` 合并 |
| 命令 | 手写 `CommandVulpecula` + `CommandDispatcher` / `CommandSchedule` / `CommandScript` / `CommandUtilTiming` | `CommandScanner` 扫 `@CommandBody` 的 `SimpleCommandBody`，`CommandRegistry` 在 ENABLE 挂到 `/vulpecula`（别名 `/vul`）；`@CommandDevelop` 挂 `/vul develop` |
| 输出渲染 | 纯文本 | `common-diagram` 的 `TreeDiagram` / `TableDiagram`，语句树由 `AbstractActionParser.onDrawStructure` 生成 |
| 语句开关 | `action-registry.yml` / `property-registry.yml` | 无（改由拓展 jar 的装卸控制） |

---

## 五、移植改写规则（Recipes）

把 v2 语句改写成 v3 时按下面套路机械转换：

1. **拆层级**
   `@BacikalParser("item")` + `Resolver(name = ["lore"])` + `when("add")` → `@Parser("item.lore.append")`，一个末端一个类。父节点自动生成，不用手写。

2. **删根参数**
   v2 `source()` 读的宿主对象 → v3 用 `XXX.getContext(frame)`；如果 v2 允许在根位置直接传值，v3 对应写法是先 `xxx switch <值>`。

3. **参数逐条对照**

   | v2 | v3 |
   | --- | --- |
   | `text(display = "line")` | `line: String` |
   | `int(0)` | `line: Int = 0` |
   | `entity(display = "damager")` | `damager: Entity` |
   | `optional("by", then = entity())` | `@Optional(["by"]) damager: Entity? = null` |
   | `expect("to", then = any())` | `@Expected(["to"]) value: Any` |
   | `trim("to", then = text())` | 无直接对应；`@Optional(["to"])` 或把 `to` 做成必需前缀 |
   | `argument("owner", then = text(), def = "@")` | `@Additional(["owner"]) owner: String = "@"`（脚本写 `--owner`） |
   | `player()` / `frame.playerOrNull()` | 直接声明 `sender: Player` 形参 |
   | `action()` | `action: ParsedAction<*>` |
   | `any()` | `value: Any?` |
   | `list()` | `values: List<*>`（`ListApplicative`） |

4. **返回值**
   v2 `reader.transfer { }` 返回宿主对象供 `>>` 用 → v3 直接改上下文、`resolve` 返回 `Unit`。
   v2 `reader.handle { }` 的读取型 → v3 `resolve` 返回具体类型。
   v2 `combineOf` 异步 → v3 `resolve` 返回 `CompletableFuture<T>`。

5. **报错文本**
   v2 `error("No entity source selected.")` → v3 在 `common-core` 的 `Lang` 加枚举常量 + 在**所属模块**的 `lang/zh_CN.yml` 加键，用 `require(...) { Lang.XXX.asText(console(), args) }`。

6. **落位与构建**
   新语句族放 `extension-action-<name>` 独立模块 → `settings.gradle.kts` 注册 → `plugin-snapshot/build.gradle.kts` 加 `compileOnly(project(":xxx"))` → 确认 `generateMetadata` 跑过。

---

## 六、需要先决策 / 先补齐的事项

1. **`>>` 管道的替代方案是否足够。** 上下文变量是**帧级共享**的，v2 的 `>>` 是**表达式级**的。`item &a >> lore get` 这类「临时对某个对象取值而不污染上下文」的用法在 v3 目前没有等价写法。要么接受语义变化，要么给 `ComplexActionParser` 加一个「临时上下文」机制。

2. **`{ … }` 语句块尚未实现。** `DefaultReader.readAction()` 里 `hasToken("{")` 的两个分支实现完全相同，等于没做块解析。`if-else`、`try-catch`、`canvas` 这类依赖 v2 `ActionBlock` 的语句移植前必须先补。

3. **叠加型操作缺失。** v2 的 `enchantment add`（等级叠加）、`potion add/sub`、`flag clear`、`item modify` 在 v3 没有对应语句，需要确认是「补语句」还是「让用户用 get + set 组合」。

4. **`aliases` 对有层级 id 不生效于 Kether 名。** 见 §2.2。如果希望 `item-amount-add` 这类扁平别名也可用，需要改 `BacikalRegistry.registerAction`。

5. **canvas 家族的归属。** v2 用了私有命名空间 `vulpecula-canvas` 和自己的 `CanvasScriptContext`，与 v3「一个 id 一棵树」的模型冲突最大，建议单独立项而不是混在常规移植里。

### 顺带发现的疑似缺陷

以下均已修复：

- `module-dispatcher` 的 `example.yml` 沿用了 v2 的键名，与 `DefaultDispatcher` 实际读取的键不一致，共 5 处 —— 已按代码为准更新示例配置：

  | 示例原写法 | 代码实际读取 | 读取位置 |
  | --- | --- | --- |
  | `pre-processing` | `before-execute` | `DefaultDispatcher.kt:43` |
  | `post-processing` | `after-execute` | `DefaultDispatcher.kt:45` |
  | `rule.baffle` | `rule.baffle-time`（或 `rule.baffle-count`，二者互斥） | `BafflePipeline.kt:32,34` |
  | `debug.config-auto-reload` | `debug.auto-reload` | `ConfigService.kt:182` |
  | `listen-cancelled` | 无任何代码读取 | — |

  其中 `listen-cancelled` 不是键名不一致，而是**功能缺失**：v2 的 `ignore-cancelled` 在 v3 没有对应实现，`Listener.register` 未传递该语义。已从示例中移除，待补齐监听器注册逻辑后再加回。

- `BacikalPropertyResolver.write` 的分支写反了：`if (key.contains('.')) writeProperty(...) else writePropertyDeep(...)`，与 `read` 的判断相反，导致**所有深路径写入 `a.b = x` 全部失效**。已修正为与 `read` 对称。

- `BacikalPropertyResolver` 私有的 `writeProperty(instance: T, key, value)` 在遍历 `relatedProperties` 成功写入后**没有 return**，会继续写给所有匹配的 property，最后仍抛 `NoSuchPropertyException`；`readProperty` 是有 `return` 的。已补 `return`，并与 `readProperty` 一致地写入 `relatedBacikalCache`（此前该缓存只由 `readProperty` 填充，而写入路径开头却会查它）。
  泛型重载 `writeProperty`（`@JvmName("writePropertyGeneric")`）有完全相同的缺陷，一并修复 —— `writePropertyDeep` 正是调用这个重载，只修前者不解决问题。

- 连带修复：`writePropertyDeep` 取父路径后无条件调用 `readPropertyDeep`，但后者有 `require(paths.size >= 2)`。两级路径（最常见的 `a.b = v`）父路径只剩一级，必然抛 `Invalid path`。已改为按父路径级数分派到 `readPropertyDeep` / `readProperty`。

### 仍待决策：深路径的 `?` 空安全语义不一致

`readPropertyDeep` 与 `writePropertyDeep` 对 `?` 的归属理解相反，且首段无法标记：

- `readPropertyDeep`：判空发生在 `while` 循环开头，检查的是**当前段** `paths[index]` 的 `?`，而此时 `cache` 持有的是**前一段**的值。即 `a.b?.c` 实际表达「若 `a` 为空则返回 null」，与 Kotlin 惯例（若 `b` 为空则返回 null）相差一位。
- `writePropertyDeep`：检查 `parentPath.last() == '?'`，即 `a.b?.c = v` 表达「若 `b` 为空则跳过」—— 符合 Kotlin 惯例，但与上一条相反。
- 首段读取 `readProperty(instance, paths[0])` **未** `removeSuffix("?")`，因此 `a?.b` 会去查找名为 `a?` 的属性并报 `NoSuchPropertyException`。

三者需统一到同一套语义后再改，属于行为变更，未随本次修复一并处理。
