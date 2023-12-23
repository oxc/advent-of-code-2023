package util.matrix

typealias Fields3D<T> = ArrayList<ArrayList<ArrayList<Field3D<T>>>>


fun interface Highlight3D<T> {
    fun highlightCode(field: Field3D<T>): String?

    companion object {
        fun <T> none() = Highlight3D<T> { null }

        fun <T> fields(fields: Set<Field3D<T>>, code: String) = Highlight3D<T> {
            if (it in fields) code else null
        }
    }
}

fun interface Projector3D<T> {
    fun Sequence<Field3D<T>>.project(printer: Printer3D<T>): CharSequence

    companion object {
        fun <T> projectFirst() = Projector3D<T> { printer -> with(printer) { first().printField() } }
    }
}

fun interface Printer3D<T> {
    fun Field3D<T>.printField(): CharSequence

    companion object {
        fun <T> default(): Printer3D<T> = Printer3D { value.toString() }

        fun <T> width(width: Int, printFun: Field3D<T>.() -> CharSequence) = Printer3D { printFun().padStart(width) }
    }
}

class BaseMatrix3D<T>(values: List<List<List<T>>>, val padValue: Point3D.() -> T) {
    private val fields: Fields3D<T> = values.mapIndexedTo(ArrayList(values.size)) { z, ys ->
        if (ys.size != values[0].size) throw Exception("Mis-shaped matrix")
        ys.mapIndexedTo(ArrayList(ys.size)) { y, xs ->
            if (xs.size != ys[0].size) throw Exception("Mis-shaped matrix")
            xs.mapIndexedTo(ArrayList(xs.size)) { x, value ->
                Field3D(this, x, y, z, value)
            }
        }
    }

    val sizeX = fields[0][0].size
    val sizeY = fields[0].size
    val sizeZ = fields.size

    operator fun get(x: Int, y: Int, z: Int): Field3D<T> {
        if (x in 0..<sizeX && y in 0..<sizeY && z in 0..<sizeZ) {
            return fields[z][y][x]
        }
        return Field3D(this, x, y, z, Point3D(x, y, z).padValue(), isOutOfBounds = true)
    }

}

abstract class AbstractMatrix3DElement<T>(
    private val baseMatrix: BaseMatrix3D<T>,
    private val offset: Offset3D = Offset3D.NONE
) {
    abstract val firstField: Field3D<T>
    abstract val lastField: Field3D<T>

    val minX get() = firstField.x - offset.minX
    val minY get() = firstField.y - offset.minY
    val minZ get() = firstField.z - offset.minZ
    val maxX get() = lastField.x + offset.maxX
    val maxY get() = lastField.y + offset.maxY
    val maxZ get() = lastField.z + offset.maxZ

    val sizeX get() = maxX - minX + 1
    val sizeY get() = maxY - minY + 1
    val sizeZ get() = maxZ - minZ + 1

    operator fun get(x: Int = 0, y: Int = 0, z: Int = 0): Field3D<T> {
        return baseMatrix[firstField.x + x, firstField.y + y, firstField.z + z]
    }

    operator fun get(xs: IntRange = 0..<sizeX, ys: IntRange = 0..<sizeY, zs: IntRange = 0..<sizeZ): SubMatrix3D<T> {
        return SubMatrix3D(baseMatrix, this[xs.first, ys.first, zs.first], this[xs.last, ys.last, zs.last])
    }

    fun asSequence(): Sequence<Field3D<T>> = sequence {
        for (z in 0..<sizeZ) {
            for (y in 0..<sizeY) {
                for (x in 0..<sizeX) {
                    yield(this@AbstractMatrix3DElement[x, y, z])
                }
            }
        }
    }

    abstract fun print(
        printer: Printer3D<T> = Printer3D.default(),
        highlight: Highlight3D<T> = Highlight3D.none(),
        stringBuilder: StringBuilder = StringBuilder()
    ): StringBuilder
}

enum class Projection {
    Front, Left
}

abstract class AbstractMatrix3D<T>(baseMatrix: BaseMatrix3D<T>, offset: Offset3D = Offset3D.NONE) :
    AbstractMatrix3DElement<T>(baseMatrix, offset) {
    fun print(
        projection: Projection,
        projector: Projector3D<T> = Projector3D.projectFirst(),
        printer: Printer3D<T> = Printer3D.default(),
        highlight: Highlight3D<T> = Highlight3D.none(),
        stringBuilder: StringBuilder = StringBuilder(),
    ): StringBuilder = when (projection) {
        Projection.Front -> get(ys = 0..0).print(printer = {
            val field = this
            with(projector) {
                field.get(ys = 0..<this@AbstractMatrix3D.sizeY).asSequence().project(printer)
            }
        }, highlight, stringBuilder)

        Projection.Left -> get(xs = 0..0).print(printer = {
            val field = this
            with(projector) {
                field.get(xs = 0..<this@AbstractMatrix3D.sizeX).asSequence().project(printer)
            }
        }, highlight, stringBuilder)

    }

    override fun print(
        printer: Printer3D<T>,
        highlight: Highlight3D<T>,
        stringBuilder: StringBuilder
    ): StringBuilder {
        for (z in (sizeZ - 1).downTo(0)) {
            for (y in 0..<sizeY) {
                for (x in 0..<sizeX) {
                    this[x, y, z].print(printer, highlight, stringBuilder)
                }
            }
            stringBuilder.append("\n")
        }
        return stringBuilder
    }
}

class Matrix3D<T>(baseMatrix: BaseMatrix3D<T>) : AbstractMatrix3D<T>(baseMatrix) {
    override val firstField = baseMatrix[0, 0, 0]
    override val lastField = baseMatrix[baseMatrix.sizeX - 1, baseMatrix.sizeY - 1, baseMatrix.sizeZ - 1]

    override fun toString(): String {
        return "Matrix3D(xs=$minX..$maxX, ys=$minY..$maxY, zs=$minZ..$maxZ)"
    }

    companion object {
        fun <T> ofSize(
            xs: IntRange,
            ys: IntRange,
            zs: IntRange,
            padValue: (Point3D.() -> T)? = null,
            initValue: Point3D.() -> T
        ): Matrix3D<T> {
            val base = BaseMatrix3D(zs.map { z ->
                ys.map { y ->
                    xs.map { x ->
                        Point3D(x, y, z).initValue()
                    }
                }
            }, padValue ?: initValue)
            return Matrix3D(base)
        }
    }
}

class SubMatrix3D<T>(
    baseMatrix: BaseMatrix3D<T>,
    override val firstField: Field3D<T>,
    override val lastField: Field3D<T>,
    offset: Offset3D = Offset3D.NONE,
) : AbstractMatrix3D<T>(
    baseMatrix,
    offset,
) {
    override fun toString(): String {
        return "SubMatrix3D(xs=$minX..$maxX, ys=$minY..$maxY, zs=$minZ..$maxZ)"
    }
}


data class Offset3D(
    val minX: Int = 0, val minY: Int = 0, val minZ: Int = 0,
    val maxX: Int = 0, val maxY: Int = 0, val maxZ: Int = 0,

    ) {
    companion object {
        val NONE = Offset3D()
    }
}

interface BasePoint3D {
    val x: Int
    val y: Int
    val z: Int
}

data class Point3D(override val x: Int, override val y: Int, override val z: Int) : BasePoint3D {

}

data class Delta3D(val x: Int = 0, val y: Int = 0, val z: Int = 0)

class Field3D<T>(
    baseMatrix: BaseMatrix3D<T>,
    override val x: Int,
    override val y: Int,
    override val z: Int,
    val value: T,
    val isOutOfBounds: Boolean = false,
) : AbstractMatrix3DElement<T>(baseMatrix), BasePoint3D {
    override val firstField get() = this
    override val lastField get() = this

    override fun toString(): String {
        if (isOutOfBounds) {
            return "Field3D([$x,$y,$z], $value, isOutOfBounds = true)"
        }
        return "Field3D([$x,$y,$z], $value)"
    }

    override fun print(
        printer: Printer3D<T>,
        highlight: Highlight3D<T>,
        stringBuilder: StringBuilder
    ): StringBuilder {
        val repr = with(printer) { printField() }
        val code = highlight.highlightCode(this) ?: return stringBuilder.append(repr)
        return stringBuilder.append("\u001b[${code}m$repr\u001b[0m")
    }
}