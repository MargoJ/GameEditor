package pl.margoj.editor.map.cursor

import java.util.ArrayList
import java.util.LinkedList

import pl.margoj.editor.map.RectangleSelection
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.actions.CollisionsUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo.Change
import pl.margoj.mrf.map.Point

class SingleElementCursor : Cursor
{
    private var currentCollision: Boolean = false
    private val collisions = LinkedList<Point>()
    private val changes = LinkedList<Change>()

    override val supportsCollisions: Boolean = true

    override fun getSelection(context: CursorContext): Selection
    {
        return RectangleSelection(context.currentPoint.x,
                context.currentPoint.y,
                if (context.isCollisionLayerSelected) 1 else context.onTilesetSelection.width,
                if (context.isCollisionLayerSelected) 1 else context.onTilesetSelection.height
        )
    }

    override fun mouseClicked(context: CursorContext)
    {
        if (context.isCollisionLayerSelected)
        {
            this.currentCollision = !context.map.getCollisionAt(context.currentPoint)
            this.collisions.clear()
        }
        this.changes.clear()
    }

    override fun mouseDragged(context: CursorContext)
    {
        val changes = Selection(this.perform(context))

        this.redraw(context, changes)
    }

    override fun apply(context: CursorContext): Collection<Point>
    {
        val result = this.perform(context)
        if (context.isCollisionLayerSelected)
        {
            context.editor.addUndoAction(CollisionsUndoRedo(this.collisions, this.currentCollision))
        }
        else
        {
            context.editor.addUndoAction(SimpleUndoRedo(this.changes))
        }
        return result
    }

    private fun perform(context: CursorContext): List<Point>
    {
        val points = ArrayList<Point>()

        if (context.isCollisionLayerSelected)
        {
            val current = context.currentPoint

            val currentMap = context.editor.currentMap
            if (this.currentCollision == currentMap!!.getCollisionAt(current))
            {
                return points
            }
            currentMap.setCollisionAt(current, this.currentCollision)
            points.add(current)
            this.collisions.add(Point(current.x, current.y))
        }
        else
        {
            for (x in 0 until context.onTilesetSelection.width)
            {
                for (y in 0 until context.onTilesetSelection.height)
                {
                    val tilePoint = Point(context.onTilesetSelection.x + x, context.onTilesetSelection.y + y)
                    val mapPoint = Point(context.currentPoint.x + x, context.currentPoint.y + y)

                    if (!context.map.inBounds(mapPoint))
                    {
                        continue
                    }

                    val oldFragment = context.map.fragments[mapPoint.x][mapPoint.y][context.editor.currentLayer]
                    val fragment = context.tileset.getFragmentAt(context.map,
                            tilePoint,
                            mapPoint,
                            context.editor.currentLayer
                    )

                    this.changes.add(Change(oldFragment, fragment))

                    if (context.map.setFragment(fragment))
                    {
                        points.add(mapPoint)
                    }
                }
            }
        }

        return points
    }

    companion object
    {
        val INSTANCE = SingleElementCursor()
    }
}
