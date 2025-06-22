package top.lanscarlos.vulpecula.bacikal

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-20 22:15
 */

data class Future2<T1, T2>(
    val t1: T1,
    val t2: T2
)

data class Future3<T1, T2, T3>(
    val t1: T1,
    val t2: T2,
    val t3: T3
)

data class Future4<T1, T2, T3, T4>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4
)

data class Future5<T1, T2, T3, T4, T5>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5
)

data class Future6<T1, T2, T3, T4, T5, T6>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6
)

data class Future7<T1, T2, T3, T4, T5, T6, T7>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7
)

data class Future8<T1, T2, T3, T4, T5, T6, T7, T8>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8
)

data class Future9<T1, T2, T3, T4, T5, T6, T7, T8, T9>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9
)

data class Future10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9,
    val t10: T10
)

data class Future11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9,
    val t10: T10,
    val t11: T11
)

data class Future12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9,
    val t10: T10,
    val t11: T11,
    val t12: T12
)

data class Future13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9,
    val t10: T10,
    val t11: T11,
    val t12: T12,
    val t13: T13
)

data class Future14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9,
    val t10: T10,
    val t11: T11,
    val t12: T12,
    val t13: T13,
    val t14: T14
)

data class Future15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9,
    val t10: T10,
    val t11: T11,
    val t12: T12,
    val t13: T13,
    val t14: T14,
    val t15: T15
)

data class Future16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7,
    val t8: T8,
    val t9: T9,
    val t10: T10,
    val t11: T11,
    val t12: T12,
    val t13: T13,
    val t14: T14,
    val t15: T15,
    val t16: T16
)

/**
 * 将一组 CompletableFuture 转成对应列表结果
 * */
inline fun <reified T: Any?> combineFutureList(queue: List<CompletableFuture<T>>): CompletableFuture<List<T>> {
    val result = CompletableFuture<List<T>>()

    if (queue.isEmpty()) {
        // 队列为空
        result.complete(listOf())
    } else if (queue.size == 1) {
        // 队列仅有一个
        val future = queue.first()
        if (future.isDone) {
            result.complete(listOf(future.getNow(null)))
        } else {
            future.thenAccept { result.complete(listOf(it)) }
        }
    } else {
        // 队列有多个
        val counter = AtomicInteger(0)
        for (it in queue) {
            if (it.isDone) {
                val count = counter.incrementAndGet()

                // 判断 futures 是否全部执行完毕
                if (!result.isDone && count >= queue.size) {
                    result.complete(queue.map { it.getNow(null) })
                    return result
                }
            } else {
                it.thenRun {
                    val count = counter.incrementAndGet()

                    // 判断 futures 是否全部执行完毕
                    if (!result.isDone && count >= queue.size) {
                        result.complete(queue.map { it.getNow(null) })
                    }
                }
            }
        }
    }
    return result
}

fun <P1, P2> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>
): CompletableFuture<Future2<P1, P2>> {
    if (p1.isDone) {
        val t1 = p1.getNow(null)
        return if (p2.isDone) {
            val t2 = p2.getNow(null)
            CompletableFuture.completedFuture(Future2(t1, t2))
        } else {
            p2.thenApply { t2 -> Future2(t1, t2) }
        }
    } else {
        return p1.thenCompose { t1 ->
            if (p2.isDone) {
                val t2 = p2.getNow(null)
                CompletableFuture.completedFuture(Future2(t1, t2))
            } else {
                p2.thenApply { t2 -> Future2(t1, t2) }
            }
        }
    }
}

fun <P1, P2, P3> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>
): CompletableFuture<Future3<P1, P2, P3>> {
    val future = combineFuture(combineFuture(p1, p2), p3)
    return if (future.isDone) {
        val app = future.getNow(null)
        CompletableFuture.completedFuture(
            Future3(
                app.t1.t1,
                app.t1.t2,
                app.t2
            )
        )
    } else {
        future.thenApply { app ->
            Future3(
                app.t1.t1,
                app.t1.t2,
                app.t2
            )
        }
    }
}

fun <P1, P2, P3, P4> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>
): CompletableFuture<Future4<P1, P2, P3, P4>> {
    val ap = combineFuture(
        combineFuture(p1, p2),
        combineFuture(p3, p4)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future4(
                app.t1.t1,
                app.t1.t2,
                app.t2.t1,
                app.t2.t2
            )
        )
    } else {
        ap.thenApply { app ->
            Future4(
                app.t1.t1,
                app.t1.t2,
                app.t2.t1,
                app.t2.t2
            )
        }
    }
}

fun <P1, P2, P3, P4, P5> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>
): CompletableFuture<Future5<P1, P2, P3, P4, P5>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3),
        combineFuture(p4, p5)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future5(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t2.t1,
                app.t2.t2
            )
        )
    } else {
        ap.thenApply { app ->
            Future5(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t2.t1,
                app.t2.t2
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>
): CompletableFuture<Future6<P1, P2, P3, P4, P5, P6>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3),
        combineFuture(p4, p5, p6)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future6(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3
            )
        )
    } else {
        ap.thenApply { app ->
            Future6(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>
): CompletableFuture<Future7<P1, P2, P3, P4, P5, P6, P7>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4),
        combineFuture(p5, p6, p7)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future7(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3
            )
        )
    } else {
        ap.thenApply { app ->
            Future7(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>
): CompletableFuture<Future8<P1, P2, P3, P4, P5, P6, P7, P8>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4),
        combineFuture(p5, p6, p7, p8)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future8(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4
            )
        )
    } else {
        ap.thenApply { app ->
            Future8(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>
): CompletableFuture<Future9<P1, P2, P3, P4, P5, P6, P7, P8, P9>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5),
        combineFuture(p6, p7, p8, p9)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future9(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4
            )
        )
    } else {
        ap.thenApply { app ->
            Future9(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>,
    p10: CompletableFuture<P10>
): CompletableFuture<Future10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5),
        combineFuture(p6, p7, p8, p9, p10)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future10(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5
            )
        )
    } else {
        ap.thenApply { app ->
            Future10(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>,
    p10: CompletableFuture<P10>,
    p11: CompletableFuture<P11>,
): CompletableFuture<Future11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5, p6),
        combineFuture(p7, p8, p9, p10, p11)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future11(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5
            )
        )
    } else {
        ap.thenApply { app ->
            Future11(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>,
    p10: CompletableFuture<P10>,
    p11: CompletableFuture<P11>,
    p12: CompletableFuture<P12>,
): CompletableFuture<Future12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5, p6),
        combineFuture(p7, p8, p9, p10, p11, p12)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future12(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6
            )
        )
    } else {
        ap.thenApply { app ->
            Future12(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>,
    p10: CompletableFuture<P10>,
    p11: CompletableFuture<P11>,
    p12: CompletableFuture<P12>,
    p13: CompletableFuture<P13>,
): CompletableFuture<Future13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5, p6, p7),
        combineFuture(p8, p9, p10, p11, p12, p13)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future13(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
            )
        )
    } else {
        ap.thenApply { app ->
            Future13(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>,
    p10: CompletableFuture<P10>,
    p11: CompletableFuture<P11>,
    p12: CompletableFuture<P12>,
    p13: CompletableFuture<P13>,
    p14: CompletableFuture<P14>,
): CompletableFuture<Future14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5, p6, p7),
        combineFuture(p8, p9, p10, p11, p12, p13, p14)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future14(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
                app.t2.t7
            )
        )
    } else {
        ap.thenApply { app ->
            Future14(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
                app.t2.t7
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>,
    p10: CompletableFuture<P10>,
    p11: CompletableFuture<P11>,
    p12: CompletableFuture<P12>,
    p13: CompletableFuture<P13>,
    p14: CompletableFuture<P14>,
    p15: CompletableFuture<P15>,
): CompletableFuture<Future15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5, p6, p7, p8),
        combineFuture(p9, p10, p11, p12, p13, p14, p15)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future15(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t1.t8,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
                app.t2.t7
            )
        )
    } else {
        ap.thenApply { app ->
            Future15(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t1.t8,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
                app.t2.t7
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> combineFuture(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
    p8: CompletableFuture<P8>,
    p9: CompletableFuture<P9>,
    p10: CompletableFuture<P10>,
    p11: CompletableFuture<P11>,
    p12: CompletableFuture<P12>,
    p13: CompletableFuture<P13>,
    p14: CompletableFuture<P14>,
    p15: CompletableFuture<P15>,
    p16: CompletableFuture<P16>,
): CompletableFuture<Future16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>> {
    val ap = combineFuture(
        combineFuture(p1, p2, p3, p4, p5, p6, p7, p8),
        combineFuture(p9, p10, p11, p12, p13, p14, p15, p16)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Future16(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t1.t8,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
                app.t2.t7,
                app.t2.t8
            )
        )
    } else {
        ap.thenApply { app ->
            Future16(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t1.t4,
                app.t1.t5,
                app.t1.t6,
                app.t1.t7,
                app.t1.t8,
                app.t2.t1,
                app.t2.t2,
                app.t2.t3,
                app.t2.t4,
                app.t2.t5,
                app.t2.t6,
                app.t2.t7,
                app.t2.t8
            )
        }
    }
}