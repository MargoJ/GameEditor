package pl.margoj.editor.map.cursor

import java.util.LinkedList

import pl.margoj.editor.map.RectangleSelection
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.actions.CollisionsUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo.Change
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.fragment.empty.EmptyMapFragment

class ErasingCursor : Cursor
{
    private val changes = LinkedList<Change>()
    private val changed = LinkedList<Point>()
    private val collisions = LinkedList<Point>()

    override val supportsCollisions: Boolean = true

    override fun getSelection(context: CursorContext): Selection
    {
        return RectangleSelection(context.currentPoint.x, context.currentPoint.y, 1, 1)
    }

    override fun mouseClicked(context: CursorContext)
    {
        this.changes.clear()
        this.collisions.clear()
        this.changed.clear()
    }

    override fun mouseDragged(context: CursorContext)
    {
        this.redraw(context, Selection(this.perform(context)))
    }

    override fun apply(context: CursorContext): Collection<Point>
    {
        val points = this.perform(context)

        if (context.isCollisionLayerSelected)
        {
            context.editor.addUndoAction(CollisionsUndoRedo(this.collisions, false))
        }
        else
        {
            context.editor.addUndoAction(SimpleUndoRedo(this.changes))
        }

        return points
    }

    private fun perform(context: CursorContext): List<Point>
    {
        val map = context.map
        val point = context.currentPoint

        if (this.changed.contains(point))
        {
            return emptyList()
        }
        this.changed.add(point)

        if (context.isCollisionLayerSelected)
        {
            this.collisions.add(point)
            map.setCollisionAt(point, false)
        }
        else
        {
            val oldFragment = map.getFragment(point, context.editor.currentLayer)
            val fragment = EmptyMapFragment(point, context.editor.currentLayer)
            if (map.setFragment(fragment))
            {
                this.changes.add(Change(oldFragment!!, fragment))
            }
        }

        return listOf(point)
    }

    companion object
    {
        val INSTANCE = ErasingCursor()
    }
}
