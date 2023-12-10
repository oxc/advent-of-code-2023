@file:Suppress("MemberVisibilityCanBePrivate")

package util.matrix

typealias Fields<T> = ArrayList<ArrayList<Field<T>>>

class BaseMatrix<T>(
    values: List<List<T>>,
    private val padValue: T
) {
    val height = values.size
    val width = values[0].size
    private val fields: Fields<T> = values.mapIndexedTo(ArrayList(values.size)) { y, row ->
        if (row.size != width) throw Exception("Mis-shaped matrix")
        row.mapIndexedTo(ArrayList(row.size)) { x, value ->
            Field(this, x, y, value)
        }
    }

    operator fun get(x: Int, y: Int): Field<T> {
        if (x in 0..<width && y in 0..<height) {
            return fields[y][x]
        }
        return Field(this, x, y, padValue, isOutOfBounds = true)
    }
}

abstract class AbstractMatrix<T>(
    private val baseMatrix: BaseMatrix<T>,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
) {
    val width = maxX - minX + 1
    val height = maxY - minY + 1

    operator fun get(x: Int, y: Int): Field<T> {
        val baseX = minX + x
        val baseY = minY + y
        return baseMatrix[baseX, baseY]
    }

    operator fun get(xs: IntRange, y: Int): Line<T> {
        return Line(baseMatrix, y = minY + y, minX = minX + xs.first, maxX = minX + xs.last)
    }

    operator fun get(xs: IntRange, ys: IntRange): Matrix<T> {
        return Matrix(
            baseMatrix,
            minX = minX + xs.first, maxX = minX + xs.last,
            minY = minY + ys.first, maxY = minY + ys.last
        )
    }

    fun grow(s: Int): Matrix<T> {
        return grow(s, s, s, s)
    }

    fun grow(x: Int, y: Int): Matrix<T> {
        return grow(x, y, x, y)
    }

    fun grow(l: Int, t: Int, r: Int, b: Int): Matrix<T> {
        return Matrix(
            baseMatrix,
            minX = minX - l, maxX = maxX + r,
            minY = minY - t, maxY = maxY + b
        )
    }

    fun asSequence(): Sequence<Field<T>> {
        return sequence {
            for (y in 0..<height) {
                for (x in 0..<width) {
                    yield(this@AbstractMatrix[x, y])
                }
            }
        }
    }

    abstract fun print(stringBuilder: StringBuilder = StringBuilder()): StringBuilder
}

class Matrix<T>(baseMatrix: BaseMatrix<T>, minX: Int, minY: Int, maxX: Int, maxY: Int) :
    AbstractMatrix<T>(baseMatrix, minX, minY, maxX, maxY) {

    operator fun get(y: Int): Line<T> {
        return get(0..<width, y)
    }


    fun lines(): Iterable<Line<T>> {
        return (0..<height).map { y -> this[y] }
    }

    fun scanAllLines(predicate: (T) -> Boolean): Sequence<Line<T>> {
        return lines().asSequence().flatMap { it.scanAll(predicate = predicate) }
    }

    override fun print(stringBuilder: StringBuilder): StringBuilder {
        lines().forEach {
            it.print(stringBuilder)
            stringBuilder.append('\n')
        }
        return stringBuilder
    }

    override fun toString(): String {
        return "Matrix(${width}x${height}, ${lines()})"
    }

    companion object {
        fun fromLines(lines: List<String>, padChar: Char = '.'): Matrix<Char> {
            return fromLines(lines, padChar) { it };
        }

        fun <T> fromLines(lines: List<String>, padElement: T? = null, mapper: (Char) -> T): Matrix<T> {
            val base = BaseMatrix(lines.map { it.toList().map(mapper) }, padElement ?: mapper('.'))
            return Matrix(base, 0, 0, base.width - 1, base.height - 1)
        }
    }

}

open class Line<T>(
    baseMatrix: BaseMatrix<T>,
    val y: Int,
    minX: Int,
    maxX: Int
) : AbstractMatrix<T>(
    baseMatrix,
    minX = minX,
    maxX = maxX,
    minY = y,
    maxY = y,
) {
    override fun print(stringBuilder: StringBuilder): StringBuilder {
        this.asSequence().forEach {
            it.print(stringBuilder)
        }
        return stringBuilder
    }

    operator fun get(x: Int): Field<T> {
        return this[x, 0]
    }

    fun scan(offset: Int = 0, predicate: (T) -> Boolean): Line<T>? {
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

    fun scanAll(offset: Int = 0, predicate: (T) -> Boolean): Sequence<Line<T>> {
        return sequence {
            var startX = offset
            do {
                val line = scan(startX, predicate) ?: break
                yield(line)
                startX = line.maxX + 1 - minX
            } while (true)
        }
    }

    override fun toString(): String {
        return "Line(y=$y, x=$minX..$maxX, ${print()})"
    }
}

class Field<T>(
    baseMatrix: BaseMatrix<T>,
    val x: Int,
    val y: Int,
    val value: T,
    val isOutOfBounds: Boolean = false
) : AbstractMatrix<T>(
    baseMatrix,
    minX = x,
    maxX = x,
    minY = y,
    maxY = y,
) {
    val left get() = get(-1, 0)
    val topLeft get() = get(-1, -1)
    val top get() = get(0, -1)
    val topRight get() = get(1, -1)
    val right get() = get(1, 0)
    val bottomRight get() = get(1, 1)
    val bottom get() = get(0, 1)
    val bottomLeft get() = get(-1, 1)

    val directNeighbours get() = listOf(left, top, right, bottom)
    val diagonalNeighbours get() = listOf(topLeft, topRight, bottomLeft, bottomRight)
    val allNeighbours get() = listOf(left, topLeft, top, topRight, right, bottomRight, bottom, bottomLeft)

    override fun print(stringBuilder: StringBuilder): StringBuilder {
        return stringBuilder.append(value)
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
typealias CharLine = Line<Char>
typealias CharField = Field<Char>

fun CharLine.asString() = this.print().toString()