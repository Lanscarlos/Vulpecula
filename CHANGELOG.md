# 更新日志

本文件记录 Vulpecula 的版本变更，格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

发布流程：版本号以 `gradle.properties` 里的 `version` 为准，改动它并合入 `v2`，
Release 工作流就会打上对应的 tag 并发布正式版；发布说明的正文取自本文件里
同版本号的小节，所以升版本号之前先把改动写进下面的「未发布」，再一起提交。

## [未发布]

## [2.3.16] - 2026-08-10

### 变更

- 更新 TabooLib 至 6.3.0-75b18a2

### 构建

- 新增自动发布工作流：正式版随 `gradle.properties` 版本号变更发布，快照随 `v2` 推送发布

2.3.8 – 2.3.15 只改过版本号，从未发布过 release，因此从 2.3.7 升级到本版会一并
包含它们的全部改动。

## [2.3.15] - 2026-08-06

### 修复

- 修复 func 语句无法调用脚本函数的问题

## [2.3.14] - 2026-07-26

### 修复

- 修复日程执行时间累积偏移

## [2.3.13] - 2026-04-26

### 变更

- 更新 TabooLib 版本

## [2.3.12] - 2026-01-11

### 修复

- 修复 BacikalRegistry 注册异常问题

## [2.3.11] - 2025-07-01

### 修复

- 修复 FileWatcher 函数缺失问题
- 修复 ReflexClass 引用
- 修复 Particle 引用问题

### 变更

- 更新 TabooLib 版本

## [2.3.10] - 2025-06-02

### 修复

- 修复 Memory 语句注册失效问题

### 变更

- 更新 TabooLib 至 6.2.3-8cc2f66
- 更新 Gradle 至 8.9

## [2.3.8] - 2024-11-07

### 修复

- 修复 Item Color 语句

2.3.9 未曾存在，版本号从 2.3.8 直接跳到了 2.3.10。

## 2.3.7 及更早

见 [GitHub Releases](https://github.com/Lanscarlos/Vulpecula/releases)。
