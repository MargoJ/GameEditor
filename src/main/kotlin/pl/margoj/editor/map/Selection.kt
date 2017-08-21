package pl.margoj.editor.map

import java.util.ArrayList

import pl.margoj.mrf.map.Point

import javafx.scene.canvas.GraphicsContext

open class Selection(points: Collection<Point>) : Iterable<Point>
{
    private var lowestX: Int = 0
    private var lowestY: Int = 0
    private var arraySizeX: Int = 0
    private var arraySizeY: Int = 0
    private var pointsArray: Array<Array<Point>>? = null
    val changedPoints: MutableList<Point> = ArrayList()


    @Suppress("UNCHECKED_CAST")
    var points: Collection<Point>? = null
        set(points)
        {
            field = points

            if(points == null)
            {
                return
            }

            var lowestX = Integer.MAX_VALUE
            var lowestY = Integer.MAX_VALUE
            var highestX = Integer.MIN_VALUE
            var highestY = Integer.MIN_VALUE

            for ((x, y) in points)
            {
                highestX = Math.max(x, highestX)
                highestY = Math.max(y, highestY)
                lowestX = Math.min(x, lowestX)
                lowestY = Math.min(y, lowestY)
            }

            this.lowestX = lowestX
            this.lowestY = lowestY
            this.arraySizeX = highestX - lowestX + 1
            this.arraySizeY = highestY - lowestY + 1

            val pointArray = Array(this.arraySizeX) { arrayOfNulls<Point>(this.arraySizeY) }

            for (point in points)
            {
                pointArray[point.x - lowestX][point.y - lowestY] = point
            }

            this.pointsArray = pointArray as Array<Array<Point>>
        }

    init
    {
        this.points = points
    }

    fun draw(context: GraphicsContext)
    {
        this.draw(context, 32)
    }

    open fun draw(context: GraphicsContext, pointSize: Int)
    {
        this.changedPoints.clear()
        for (point in this.points!!)
        {
            var modified = false
            if (this.getSafe(pointsArray!!, lowestX, lowestY, point.x - 1, point.y) == null)
            {
                this.strokePart(context, point, pointSize, 1, 1, 1, pointSize - 1)
                modified = true
            }

            if (this.getSafe(pointsArray!!, lowestX, lowestY, point.x + 1, point.y) == null)
            {
                this.strokePart(context, point, pointSize, pointSize - 1, 1, pointSize - 1, pointSize - 1)
                modified = true
            }

            if (this.getSafe(pointsArray!!, lowestX, lowestY, point.x, point.y - 1) == null)
            {
                this.strokePart(context, point, pointSize, 1, 1, pointSize - 1, 1)
                modified = true
            }

            if (this.getSafe(pointsArray!!, lowestX, lowestY, point.x, point.y + 1) == null)
            {
                this.strokePart(context, point, pointSize, 1, pointSize - 1, pointSize - 1, pointSize - 1)
                modified = true
            }

            if (modified)
            {
                this.changedPoints.add(point)
            }
        }
    }

    private fun strokePart(context: GraphicsContext, point: Point, pointSize: Int, x1: Int, y1: Int, x2: Int, y2: Int)
    {
        context.strokeLine((point.x * pointSize + x1).toDouble(), (point.y * pointSize + y1).toDouble(), (point.x * pointSize + x2).toDouble(), (point.y * pointSize + y2).toDouble())
    }

    private fun getSafe(array: Array<Array<Point>>, lowestX: Int, lowestY: Int, startX: Int, startY: Int): Point?
    {
        val x = startX - lowestX
        val y = startY - lowestY

        if (x >= array.size || y >= array[0].size || x < 0 || y < 0)
        {
            return null
        }

        return array[x][y]
    }

    fun relativize(startPoint: Point): Selection
    {
        val newPoints = this.points!!.map { Point(it.x + startPoint.x, it.y + startPoint.y) }

        return Selection(newPoints)
    }

    fun toRawSelection(): Array<Array<Point>>
    {
        return pointsArray!!
    }

    override fun iterator(): Iterator<Point>
    {
        return this.points!!.iterator()
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }
        if (other == null || javaClass != other.javaClass)
        {
            return false
        }

        val points1 = other as Selection?

        return if (this.points != null) this.points == points1!!.points else points1!!.points == null
    }

    override fun hashCode(): Int
    {
        return if (this.points != null) this.points!!.hashCode() else 0
    }

    override fun toString(): String
    {
        return "Selection(points=${this.points!!.size})"
    }
}
