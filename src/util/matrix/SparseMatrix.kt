package util.matrix

import println

data class SparseFieldData<T>(
    val value: T,
    val meta: SparseFieldMeta,
) {
    override fun toString(): String = value.toString()
}

data class SparseFieldMeta(
    val realX: Long, val realY: Long,
    val realWidth: Long, val realHeight: Long
) {
    val lastRealX = realX + realWidth - 1
    val lastRealY = realY + realHeight - 1
}

typealias SparseField<T> = Field<SparseFieldData<T>>
typealias SparseMatrixElement<T> = AbstractMatrixElement<SparseFieldData<T>>
typealias SparseBaseMatrix<T> = Matrix<SparseFieldData<T>>

val <T> SparseField<T>.innerValue get() = value.value
val <T> SparseField<T>.realX get() = value.meta.realX
val <T> SparseField<T>.realY get() = value.meta.realY
val <T> SparseField<T>.lastRealX get() = value.meta.lastRealX
val <T> SparseField<T>.lastRealY get() = value.meta.lastRealY
val <T> SparseField<T>.realWidth get() = value.meta.realWidth
val <T> SparseField<T>.realHeight get() = value.meta.realHeight

fun <T> SparseMatrixElement<T>.count(predicate: (SparseField<T>) -> Boolean) =
    asSequence().filter(predicate).sumOf { it.realWidth * it.realHeight }

class SparseMatrix<T>(val matrix: SparseBaseMatrix<T>) {
    private val xs = matrix.row(0).asSequence().filter { it.realHeight == 1L }.map { it.realX to it.x }.toMap()
    private val ys = matrix.column(0).asSequence().filter { it.realWidth == 1L }.map { it.realY to it.y }.toMap()
    private val startXs = matrix.row(0).asSequence().map { it.realX to it.x }.toMap()
    private val startYs = matrix.column(0).asSequence().map { it.realY to it.y }.toMap()
    private val endXs = matrix.row(0).asSequence().map { it.lastRealX to it.x }.toMap()
    private val endYs = matrix.column(0).asSequence().map { it.lastRealY to it.y }.toMap()

    init {
        println("xs=$xs, ys=$ys, startXs=$startXs, startYs=$startYs, endXs=$endXs, endYs=$endYs")
    }

    private fun getX(realX: Long) = xs[realX] ?: throw IllegalArgumentException("No single-width column at $realX")
    private fun getY(realY: Long) = ys[realY] ?: throw IllegalArgumentException("No single-height row at $realY")

    private fun getStartX(realX: Long) =
        startXs[realX] ?: throw IllegalArgumentException("No sparse column starting at $realX")

    private fun getStartY(realY: Long) =
        startYs[realY] ?: throw IllegalArgumentException("No sparse row starting at $realY")

    private fun getEndX(realX: Long) =
        endXs[realX] ?: throw IllegalArgumentException("No sparse column ending at $realX")

    private fun getEndY(realY: Long) =
        endYs[realY] ?: throw IllegalArgumentException("No sparse row ending at $realY")

    private fun getXs(realXs: LongRange) = getStartX(realXs.first)..getEndX(realXs.last)
    private fun getYs(realYs: LongRange) = getStartY(realYs.first)..getEndY(realYs.last)


    operator fun get(realX: Long, realY: Long): SparseField<T> = matrix[getX(realX), getY(realY)]

    operator fun get(realX: Long, realYs: LongRange): VLine<SparseFieldData<T>> =
        matrix[getX(realX), getYs(realYs)]

    operator fun get(realXs: LongRange, realY: Long): HLine<SparseFieldData<T>> =
        matrix[getXs(realXs), getY(realY)]

    operator fun get(realXs: LongRange, realYs: LongRange): SubMatrix<SparseFieldData<T>> =
        matrix[getXs(realXs), getYs(realYs)]

    companion object {
        fun <T> buildSparseMatrix(
            fixedColumns: Iterable<Long>,
            fixedRows: Iterable<Long>,
            padValue: (SparseFieldMeta.() -> T)? = null,
            initValue: SparseFieldMeta.() -> T,
        ): SparseMatrix<T> {
            val xys = listOf(fixedColumns, fixedRows).map { fixed ->
                fixed.toSortedSet().let { if (it.first() == 0L) it else listOf(null) + it } + listOf(null)
            }.println()
            val (xws, yws) = xys.map { fixed ->
                fixed.zipWithNext().flatMap { (current, next) ->
                    val fix = current?.let { it to 1L }
                    val sparse = next?.let { it - (current ?: 0) - 1 }
                        ?.takeUnless { it < 1 }
                        ?.let { w -> (current ?: 0) + 1 to w }
                    listOfNotNull(fix, sparse)
                }
            }.println()

            val lines = yws.map { (realY, realHeight) ->
                xws.map { (realX, realWidth) ->
                    val meta = SparseFieldMeta(realX, realY, realWidth, realHeight)
                    SparseFieldData(meta.initValue(), meta)
                }
            }

            val matrix =
                Matrix(BaseMatrix(lines, {
                    // TODO x,y are not correct, derive from xs/ys
                    val meta = SparseFieldMeta(x.toLong(), y.toLong(), 1, 1)
                    val value = (padValue ?: initValue).invoke(meta)
                    SparseFieldData(value, meta)
                }))
            return SparseMatrix(matrix)
        }

    }
}