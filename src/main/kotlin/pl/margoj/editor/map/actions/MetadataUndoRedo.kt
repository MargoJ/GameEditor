package pl.margoj.editor.map.actions

import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.editor.map.MapEditor
import pl.margoj.mrf.map.metadata.MetadataElement

class MetadataUndoRedo(val old: Collection<MetadataElement>, val new: Collection<MetadataElement>) : UndoRedoAction<MapEditor>
{
    override val actionName: String = "Edytuj dane mapy"

    override fun redo(editor: MapEditor): UndoAction<MapEditor>
    {
        editor.currentMap!!.metadata = new
        return this
    }

    override fun undo(editor: MapEditor): RedoAction<MapEditor>
    {
        editor.currentMap!!.metadata = old
        return this
    }
}