package top.lanscarlos.vulpecula.bacikal

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-20 22:15
 */

data class Applicative2<T1, T2>(
    val t1: T1,
    val t2: T2
)

data class Applicative3<T1, T2, T3>(
    val t1: T1,
    val t2: T2,
    val t3: T3
)

data class Applicative4<T1, T2, T3, T4>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4
)

data class Applicative5<T1, T2, T3, T4, T5>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5
)

data class Applicative6<T1, T2, T3, T4, T5, T6>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6
)

data class Applicative7<T1, T2, T3, T4, T5, T6, T7>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6,
    val t7: T7
)

fun <P1, P2> applicative(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>
): CompletableFuture<Applicative2<P1, P2>> {
    if (p1.isDone) {
        val t1 = p1.getNow(null)
        return if (p2.isDone) {
            val t2 = p2.getNow(null)
            CompletableFuture.completedFuture(Applicative2(t1, t2))
        } else {
            p2.thenApply { t2 -> Applicative2(t1, t2) }
        }
    } else {
        return p1.thenCompose { t1 ->
            if (p2.isDone) {
                val t2 = p2.getNow(null)
                CompletableFuture.completedFuture(Applicative2(t1, t2))
            } else {
                p2.thenApply { t2 -> Applicative2(t1, t2) }
            }
        }
    }
}

fun <P1, P2, P3> applicative(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>
): CompletableFuture<Applicative3<P1, P2, P3>> {
    val future = applicative(applicative(p1, p2), p3)
    return if (future.isDone) {
        val app = future.getNow(null)
        CompletableFuture.completedFuture(
            Applicative3(
                app.t1.t1,
                app.t1.t2,
                app.t2
            )
        )
    } else {
        future.thenApply { app ->
            Applicative3(
                app.t1.t1,
                app.t1.t2,
                app.t2
            )
        }
    }
}

fun <P1, P2, P3, P4> applicative(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>
): CompletableFuture<Applicative4<P1, P2, P3, P4>> {
    val ap = applicative(
        applicative(p1, p2),
        applicative(p3, p4)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Applicative4(
                app.t1.t1,
                app.t1.t2,
                app.t2.t1,
                app.t2.t2
            )
        )
    } else {
        ap.thenApply { app ->
            Applicative4(
                app.t1.t1,
                app.t1.t2,
                app.t2.t1,
                app.t2.t2
            )
        }
    }
}

fun <P1, P2, P3, P4, P5> applicative(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
): CompletableFuture<Applicative5<P1, P2, P3, P4, P5>> {
    val ap = applicative(
        applicative(p1, p2, p3),
        applicative(p4, p5)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Applicative5(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t2.t1,
                app.t2.t2
            )
        )
    } else {
        ap.thenApply { app ->
            Applicative5(
                app.t1.t1,
                app.t1.t2,
                app.t1.t3,
                app.t2.t1,
                app.t2.t2
            )
        }
    }
}

fun <P1, P2, P3, P4, P5, P6> applicative(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
): CompletableFuture<Applicative6<P1, P2, P3, P4, P5, P6>> {
    val ap = applicative(
        applicative(p1, p2, p3),
        applicative(p4, p5, p6)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Applicative6(
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
            Applicative6(
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

fun <P1, P2, P3, P4, P5, P6, P7> applicative(
    p1: CompletableFuture<P1>,
    p2: CompletableFuture<P2>,
    p3: CompletableFuture<P3>,
    p4: CompletableFuture<P4>,
    p5: CompletableFuture<P5>,
    p6: CompletableFuture<P6>,
    p7: CompletableFuture<P7>,
): CompletableFuture<Applicative7<P1, P2, P3, P4, P5, P6, P7>> {
    val ap = applicative(
        applicative(p1, p2, p3, p4),
        applicative(p5, p6, p7)
    )
    return if (ap.isDone) {
        val app = ap.getNow(null)
        CompletableFuture.completedFuture(
            Applicative7(
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
            Applicative7(
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