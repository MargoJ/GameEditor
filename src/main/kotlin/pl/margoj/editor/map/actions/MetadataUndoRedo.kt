package pl.margoj.editor.map.actions

import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.editor.map.MapEditor
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.metadata.MetadataElement

class MetadataUndoRedo(val old: Collection<MetadataElement>, val new: Collection<MetadataElement>) : UndoRedoAction<MapEditor, MargoMap>
{
    override val actionName: String = "Edytuj dane mapy"

    override fun redo(editor: MapEditor, obj: MargoMap): UndoAction<MapEditor, MargoMap>
    {
        obj.metadata = new
        return this
    }

    override fun undo(editor: MapEditor, obj: MargoMap): RedoAction<MapEditor, MargoMap>
    {
        obj.metadata = old
        return this
    }
}