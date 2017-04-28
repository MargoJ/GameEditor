package pl.margoj.editor.map.actions

import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.map.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import java.util.ArrayList

class CollisionsUndoRedo(changes: Collection<Point>, private val collisions: Boolean) : UndoRedoAction<MapEditor, MargoMap>
{
    private val changes: List<Point> = ArrayList(changes)

    override fun undo(editor: MapEditor, obj: MargoMap): RedoAction<MapEditor, MargoMap>
    {
        this.doSwap(editor, obj, !this.collisions)
        return this
    }

    override fun redo(editor: MapEditor, obj: MargoMap): UndoAction<MapEditor, MargoMap>
    {
        this.doSwap(editor, obj, this.collisions)
        return this
    }

    override val actionName: String = "Zmiana kolizji"

    private fun doSwap(editor: MapEditor, map: MargoMap, what: Boolean)
    {
        for (change in this.changes)
        {
            map.setCollisionAt(change, what)
        }
        if (map == editor.currentMap)
        {
            editor.redrawObjects(Selection(this.changes))
        }
    }
}
