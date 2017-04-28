package pl.margoj.editor.map

import javafx.scene.canvas.GraphicsContext
import pl.margoj.mrf.map.Point

class RectangleSelection(val x: Int, val y: Int, val width: Int, val height: Int) : Selection(calcPoints(x, y, width, height))
{
    constructor(corner1: Point, corner2: Point) :
            this(
                    Math.min(corner1.x, corner2.x),
                    Math.min(corner1.y, corner2.y),
                    Math.abs(corner1.x - corner2.x) + 1,
                    Math.abs(corner1.y - corner2.y) + 1
            )

    override fun draw(context: GraphicsContext, pointSize: Int)
    {
        this.changedPoints.clear()

        for (x in 0..this.width - 1)
        {
            this.changedPoints.add(Point(this.x + x, this.y))
            this.changedPoints.add(Point(this.x + x, this.y + this.height - 1))
        }

        for (y in 0..this.height - 1)
        {
            this.changedPoints.add(Point(this.x, this.y + y))
            this.changedPoints.add(Point(this.x + this.width - 1, this.y + y))
        }

        context.strokeRect((this.x * 32 + 1).toDouble(), (this.y * 32 + 1).toDouble(), (this.width * 32 - 2).toDouble(), (this.height * 32 - 2).toDouble())
    }

    override fun toString(): String
    {
        return "RectangleSelection(x=$x, y=$y, width=$width, height=$height)"
    }
}

@Suppress("LoopToCallChain")
private fun calcPoints(x: Int, y: Int, width: Int, height: Int): List<Point>
{
    val out = ArrayList<Point>()

    for (currentX in x..x + width - 1)
    {
        for (currentY in y..y + height - 1)
        {
            out.add(Point(currentX, currentY))
        }
    }

    return out
}
