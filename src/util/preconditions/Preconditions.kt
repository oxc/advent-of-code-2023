package util.preconditions

import kotlin.math.abs

val EPSILON: Double = 0.000001

fun <T> checkEqual(a: T, b: T) {
    check(a == b) {
        "Expected values to be equal, got $a and $b"
    }
}

fun checkEqual(a: Double, b: Double, epsilon: Double = EPSILON) {
    val bothInfinity = a == Double.POSITIVE_INFINITY && b == Double.POSITIVE_INFINITY
            || a == Double.NEGATIVE_INFINITY && b == Double.NEGATIVE_INFINITY
    check(bothInfinity || abs(a / b - 1) < epsilon) {
        "Expected values to be relatively equal within $epsilon, got $a and $b"
    }
}