package pl.margoj.editor.map.actions

import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.map.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import java.util.ArrayList

class WaterUndoRedo(changes: Collection<WaterChange>) : UndoRedoAction<MapEditor>
{
    private val changes: List<WaterChange> = ArrayList(changes)

    override fun undo(editor: MapEditor): RedoAction<MapEditor>
    {
        this.doSwap(editor, editor.currentMap!!, false)
        return this
    }

    override fun redo(editor: MapEditor): UndoAction<MapEditor>
    {
        this.doSwap(editor, editor.currentMap!!, true)
        return this
    }

    override fun isValid(): Boolean
    {
        if (changes.all { it.newWater == it.oldWater })
        {
            return false
        }
        return true
    }

    override val actionName: String = "Zmiana poziomu wody"

    private fun doSwap(editor: MapEditor, map: MargoMap, new: Boolean)
    {
        for (change in this.changes)
        {
            map.setWaterLevelAt(change.point, if (new) change.newWater else change.oldWater)
        }
        if (map == editor.currentMap)
        {
            editor.redrawObjects(Selection(this.changes.map { it.point }))
        }
    }

    data class WaterChange(val point: Point, val oldWater: Int, val newWater: Int)
}