package pl.margoj.editor.map.cursor

import pl.margoj.editor.map.RectangleSelection
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.actions.CollisionsUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo.Change
import pl.margoj.editor.map.actions.WaterUndoRedo
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.fragment.empty.EmptyMapFragment
import java.util.LinkedList

class ErasingCursor : Cursor
{
    private val changes = LinkedList<Change>()
    private val changed = LinkedList<Point>()
    private val collisions = LinkedList<Point>()
    private val water = LinkedList<WaterUndoRedo.WaterChange>()

    override val supportsCollisions: Boolean = true
    override val supportsWater: Boolean = true

    override fun getSelection(context: CursorContext): Selection
    {
        return RectangleSelection(context.currentPoint.x, context.currentPoint.y, 1, 1)
    }

    override fun mouseClicked(context: CursorContext)
    {
        this.changes.clear()
        this.collisions.clear()
        this.changed.clear()
        this.water.clear()
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
        else if (context.isWaterLayerSelected)
        {
            context.editor.addUndoAction(WaterUndoRedo(this.water))
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
        else if(context.isWaterLayerSelected)
        {
            this.water.add(WaterUndoRedo.WaterChange(point, map.getWaterLevelAt(point), 0))
            map.setWaterLevelAt(point, 0)
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
