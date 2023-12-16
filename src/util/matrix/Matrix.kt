@file:Suppress("MemberVisibilityCanBePrivate")

package util.matrix

import println
import kotlin.math.absoluteValue

typealias Fields<T> = ArrayList<ArrayList<Field<T>>>

fun interface Highlight<T> {
    fun highlightCode(field: Field<T>): String?

    companion object {
        fun <T> none() = Highlight<T> { null }

        fun <T> fields(fields: Set<Field<T>>, code: String) = Highlight<T> {
            if (it in fields) code else null
        }
    }
}

class BaseMatrix<T>(
    values: List<List<T>>,
    private val padValue: T
) {

    private val fields: Fields<T> = values.mapIndexedTo(ArrayList(values.size)) { y, row ->
        if (row.size != values[0].size) throw Exception("Mis-shaped matrix")
        row.mapIndexedTo(ArrayList(row.size)) { x, value ->
            Field(this, x, y, value)
        }
    }

    val height get() = fields.size
    val width get() = fields[0].size


    operator fun get(x: Int, y: Int): Field<T> {
        if (x in 0..<width && y in 0..<height) {
            return fields[y][x]
        }
        return Field(this, x, y, padValue, isOutOfBounds = true)
    }

    fun insertRowAfter(y: Int, fill: T?) {
        val newValue = fill ?: padValue
        for (row in (height - 1).downTo(y + 1)) {
            for (field in fields[row]) {
                field.run { unsafe_setY(this.y + 1) }
            }
        }
        val newRow = (0..<width).mapTo(ArrayList(width)) { x ->
            Field(this, x, y + 1, newValue)
        }
        fields.add(y + 1, newRow)
        fields.println()
    }

    fun insertColumnAfter(x: Int, fill: T?) {
        val newValue = fill ?: padValue
        val width = width
        for ((y, row) in fields.withIndex()) {
            for (col in (width - 1).downTo(x + 1)) {
                row[col].run { unsafe_setX(this.x + 1) }
            }
            row.add(x + 1, Field(this, x + 1, y, newValue))
        }
        fields.println()

    }
}

abstract class AbstractMatrixElement<T>(
    private val baseMatrix: BaseMatrix<T>,
    private val offset: Offset = Offset.NONE
) {
    protected abstract val topLeftField: Field<T>
    protected abstract val bottomRightField: Field<T>

    val minX get() = topLeftField.x - offset.left
    val minY get() = topLeftField.y - offset.top
    val maxX get() = bottomRightField.x + offset.right
    val maxY get() = bottomRightField.y + offset.bottom

    val width get() = maxX - minX + 1
    val height get() = maxY - minY + 1

    operator fun get(x: Int, y: Int): Field<T> {
        val baseX = minX + x
        val baseY = minY + y
        return baseMatrix[baseX, baseY]
    }

    operator fun get(xs: IntRange, y: Int): HLine<T> {
        return HLine(baseMatrix, this[xs.first, y], this[xs.last, y])
    }

    operator fun get(x: Int, ys: IntRange): VLine<T> {
        return VLine(baseMatrix, this[x, ys.first], this[x, ys.last])
    }

    operator fun get(xs: IntRange, ys: IntRange): Matrix<T> {
        return Matrix(baseMatrix, this[xs.first, ys.first], this[xs.last, ys.last])
    }

    fun grow(s: Int) = grow(Offset(s, s, s, s))

    fun grow(x: Int, y: Int) = grow(Offset(x, y, x, y))

    fun grow(l: Int, t: Int, r: Int, b: Int) = grow(Offset(l, t, r, b))

    fun grow(offset: Offset): Matrix<T> {
        return Matrix(baseMatrix, topLeftField, bottomRightField, offset)
    }

    protected fun _baseInsertRowAfter(y: Int, fill: T? = null) {
        check(
            offset == Offset.NONE
                    && width == baseMatrix.width
                    && height == baseMatrix.height
        ) { "Only full projections can be used to add rows" }
        baseMatrix.insertRowAfter(y = y, fill)
    }

    protected fun _baseInsertColumnAfter(x: Int, fill: T? = null) {
        check(
            offset == Offset.NONE
                    && width == baseMatrix.width
                    && height == baseMatrix.height
        ) { "Only full projections can be used to add columns" }
        baseMatrix.insertColumnAfter(x = x, fill)
    }


    fun asSequence(): Sequence<Field<T>> {
        return sequence {
            for (y in 0..<height) {
                for (x in 0..<width) {
                    yield(this@AbstractMatrixElement[x, y])
                }
            }
        }
    }

    abstract fun print(
        highlight: Highlight<T> = Highlight.none(),
        stringBuilder: StringBuilder = StringBuilder()
    ): StringBuilder
}

class Matrix<T>(
    baseMatrix: BaseMatrix<T>,
    override val topLeftField: Field<T>,
    override val bottomRightField: Field<T>,
    offset: Offset = Offset.NONE
) : AbstractMatrixElement<T>(baseMatrix, offset) {


    operator fun get(y: Int) = row(y)

    fun row(y: Int): HLine<T> {
        return get(0..<width, y)
    }

    fun column(x: Int): VLine<T> {
        return get(x, 0..<height)
    }


    fun rows(): Iterable<HLine<T>> {
        return (0..<height).map { y -> this[y] }
    }

    fun columns(): Iterable<VLine<T>> {
        return (0..<width).map { x -> get(x, 0..<height) }
    }

    fun insertRowAfter(y: Int, fill: T? = null) {
        _baseInsertRowAfter(y = y, fill)
    }

    fun insertColumnAfter(x: Int, fill: T? = null) {
        _baseInsertColumnAfter(x = x, fill)
    }


    fun scanAllLines(predicate: (T) -> Boolean): Sequence<HLine<T>> {
        return rows().asSequence().flatMap { it.scanAll(predicate = predicate) }
    }

    override fun print(highlight: Highlight<T>, stringBuilder: StringBuilder): StringBuilder {
        rows().forEach {
            it.print(highlight, stringBuilder)
            stringBuilder.append('\n')
        }
        return stringBuilder
    }

    override fun toString(): String {
        return "Matrix(${width}x${height}, ${rows()})"
    }

    companion object {
        fun fromLines(lines: List<String>, padChar: Char = '.'): Matrix<Char> {
            return fromLines(lines, padChar) { it };
        }

        fun <T> fromLines(lines: List<String>, padElement: T? = null, mapper: (Char) -> T): Matrix<T> {
            val base = BaseMatrix(lines.map { it.toList().map(mapper) }, padElement ?: mapper('.'))
            return Matrix(base, base[0, 0], base[base.width - 1, base.height - 1])
        }
    }

}

interface BaseDelta {
    val x: Int
    val y: Int
    operator fun plus(delta: Delta) = Delta(x + delta.x, y + delta.y)

    fun distance() = x.absoluteValue + y.absoluteValue
}

data class Offset(val left: Int = 0, val top: Int = 0, val right: Int = 0, val bottom: Int = 0) {
    companion object {
        val NONE = Offset()
    }
}

data class Delta(override val x: Int, override val y: Int) : BaseDelta {
    constructor(delta: BaseDelta) : this(delta.x, delta.y)
}

enum class Direction(override val x: Int, override val y: Int) : BaseDelta {
    Left(-1, 0), Top(0, -1), Right(1, 0), Bottom(0, 1);

    fun next(n: Int = 1) {
        entries[(this.ordinal + n) % 4]
    }

    fun asDelta() = Delta(this)
}

class HLine<T>(
    baseMatrix: BaseMatrix<T>,
    override val topLeftField: Field<T>,
    override val bottomRightField: Field<T>,
) : AbstractMatrixElement<T>(
    baseMatrix,
) {
    init {
        require(topLeftField.y == bottomRightField.y)
    }

    val y get() = minY

    override fun print(highlight: Highlight<T>, stringBuilder: StringBuilder): StringBuilder {
        this.asSequence().forEach {
            it.print(highlight, stringBuilder)
        }
        return stringBuilder
    }

    operator fun get(x: Int): Field<T> {
        return this[x, 0]
    }

    fun scan(offset: Int = 0, predicate: (T) -> Boolean): HLine<T>? {
        var startX = offset
        while (startX < width) {
            if (predicate(this[startX].value)) break
            startX += 1
        }
        if (startX >= width) return null
        // we've got at least one match
        var endX = startX
        while (endX < width) {
            val next = this[endX + 1]
            if (next.isOutOfBounds || !predicate(next.value)) break
            endX += 1
        }
        return this[startX..endX, 0]
    }

    fun scanAll(offset: Int = 0, predicate: (T) -> Boolean): Sequence<HLine<T>> {
        return sequence {
            var startX = offset
            do {
                val line = scan(startX, predicate) ?: break
                yield(line)
                startX = line.maxX + 1 - minX
            } while (true)
        }
    }

    override fun toString() = "HLine(y=$y, x=$minX..$maxX, ${print()})"
}

class VLine<T>(
    baseMatrix: BaseMatrix<T>,
    override val topLeftField: Field<T>,
    override val bottomRightField: Field<T>,
) : AbstractMatrixElement<T>(baseMatrix) {
    init {
        require(topLeftField.x == bottomRightField.x)
    }

    val x get() = minX

    override fun print(highlight: Highlight<T>, stringBuilder: StringBuilder): StringBuilder {
        this.asSequence().forEach {
            it.print(highlight, stringBuilder)
        }
        return stringBuilder
    }

    operator fun get(y: Int): Field<T> {
        return this[0, y]
    }

    override fun toString() = "VLine(x=$x, y=$minY..$maxY, ${print()})"

}

class Field<T>(
    baseMatrix: BaseMatrix<T>,
    x: Int,
    y: Int,
    val value: T,
    val isOutOfBounds: Boolean = false
) : AbstractMatrixElement<T>(baseMatrix) {

    override val topLeftField get() = this
    override val bottomRightField get() = this

    var x: Int = x
        private set

    var y: Int = y
        private set

    fun unsafe_setX(value: Int) {
        x = value
    }

    fun unsafe_setY(value: Int) {
        y = value
    }

    operator fun get(delta: BaseDelta) = get(delta.x, delta.y)

    val left get() = get(Direction.Left)
    val topLeft get() = get(-1, -1)
    val top get() = get(Direction.Top)
    val topRight get() = get(1, -1)
    val right get() = get(Direction.Right)
    val bottomRight get() = get(1, 1)
    val bottom get() = get(Direction.Bottom)
    val bottomLeft get() = get(-1, 1)

    val directNeighbours get() = listOf(left, top, right, bottom)
    val diagonalNeighbours get() = listOf(topLeftField, topRight, bottomLeft, bottomRightField)
    val allNeighbours get() = listOf(left, topLeftField, top, topRight, right, bottomRightField, bottom, bottomLeft)

    operator fun minus(field: Field<T>) = Delta(x - field.x, y - field.y)

    override fun print(highlight: Highlight<T>, stringBuilder: StringBuilder): StringBuilder {
        val code = highlight.highlightCode(this) ?: return stringBuilder.append(value)
        return stringBuilder.append("\u001b[${code}m$value\u001b[0m")
    }

    override fun hashCode(): Int {
        return y * 31 + x
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Field<*>) return false
        return x == other.x && y == other.y
    }

    override fun toString(): String {
        if (isOutOfBounds) return "Field(x=$x, y=$y, out of bounds)"
        return "Field(x=$x, y=$y, value=$value)"
    }
}

typealias CharMatrix = Matrix<Char>
typealias CharLine = HLine<Char>
typealias CharField = Field<Char>

fun CharLine.asString() = this.print().toString()