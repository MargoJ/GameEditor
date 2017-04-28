package pl.margoj.editor.map.cursor

import pl.margoj.editor.map.RectangleSelection
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.actions.CollisionsUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo.Change
import pl.margoj.mrf.map.Point
import java.util.ArrayList

class FillingCursor : Cursor
{
    override val supportsCollisions: Boolean = true

    override fun getSelection(context: CursorContext): Selection
    {
        return RectangleSelection(context.startPoint.x, context.startPoint.y, 1, 1)
    }

    override fun mouseClicked(context: CursorContext)
    {

    }

    override fun mouseDragged(context: CursorContext)
    {

    }

    override fun apply(context: CursorContext): Collection<Point>
    {
        if (context.startPoint != context.currentPoint)
        {
            return emptyList()
        }

        // TODO selecting
        val selection = RectangleSelection(0, 0, context.map.width, context.map.height)

        if (context.isCollisionLayerSelected)
        {
            val target = !context.map.getCollisionAt(context.startPoint)
            for (point in selection.points!!)
            {
                context.map.setCollisionAt(point, target)
            }
            context.editor.addUndoAction(CollisionsUndoRedo(selection.points!!, target))
        }
        else
        {
            val map = context.map
            val tileset = context.tileset
            val tilesetPoint = Point(context.onTilesetSelection.x, context.onTilesetSelection.y)

            val changes = ArrayList<Change>(selection.points!!.size)

            for (point in selection.points!!)
            {
                val oldFragment = map.getFragment(point, context.editor.currentLayer)
                val newFragment = tileset.getFragmentAt(map, tilesetPoint, point, context.editor.currentLayer)

                if (map.setFragment(newFragment))
                {
                    changes.add(Change(oldFragment!!, newFragment))
                }
            }

            context.editor.addUndoAction(SimpleUndoRedo(changes))
        }

        return selection.points!!
    }

    companion object
    {
        val INSTANCE = FillingCursor()
    }
}
