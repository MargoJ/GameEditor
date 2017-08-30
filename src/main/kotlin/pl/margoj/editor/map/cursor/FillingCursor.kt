package pl.margoj.editor.map.cursor

import javafx.scene.input.MouseButton
import pl.margoj.editor.map.RectangleSelection
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.actions.CollisionsUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo
import pl.margoj.editor.map.actions.SimpleUndoRedo.Change
import pl.margoj.editor.map.actions.WaterUndoRedo
import pl.margoj.mrf.map.Point
import java.util.ArrayList
import java.util.LinkedList

class FillingCursor : Cursor
{
    override val supportsCollisions: Boolean = true
    override val supportsWater: Boolean = true

    var targetWaterLevel = 0

    override fun getSelection(context: CursorContext): Selection
    {
        return RectangleSelection(context.startPoint.x, context.startPoint.y, 1, 1)
    }

    override fun mouseClicked(context: CursorContext)
    {
        if (context.isWaterLayerSelected)
        {
            this.targetWaterLevel = context.map.getWaterLevelAt(context.currentPoint)

            if (context.button == MouseButton.MIDDLE)
            {
                this.targetWaterLevel--
            }
            else
            {
                this.targetWaterLevel++
            }

            this.targetWaterLevel = Math.max(0, this.targetWaterLevel)
            this.targetWaterLevel = Math.min(8, this.targetWaterLevel)
        }
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
        else if(context.isWaterLayerSelected)
        {
            val changes = LinkedList<WaterUndoRedo.WaterChange>()
            for (point in selection.points!!)
            {
                changes.add(WaterUndoRedo.WaterChange(point, context.map.getWaterLevelAt(point), this.targetWaterLevel))
                context.map.setWaterLevelAt(point, this.targetWaterLevel)
            }
            context.editor.addUndoAction(WaterUndoRedo(changes))
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
