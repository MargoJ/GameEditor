package pl.margoj.editor.map.actions

import org.apache.commons.lang3.Validate
import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.editor.map.MapEditor
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject

class MapObjectUndoRedo(
        private val position: Point,
        private val old: MapObject<*>?,
        private val new: MapObject<*>?,
        override val actionName: String = "Edycja obiektu"
) : UndoRedoAction<MapEditor, MargoMap>
{
    init
    {
        Validate.isTrue((this.old == null || this.old.position == position), "Old position doesn't mach")
        Validate.isTrue((this.new == null || this.new.position == position), "New position doesn't mach")
    }

    override fun undo(editor: MapEditor, obj: MargoMap): RedoAction<MapEditor, MargoMap>
    {
        this.swap(obj, old)
        return this
    }

    override fun redo(editor: MapEditor, obj: MargoMap): UndoAction<MapEditor, MargoMap>
    {
        this.swap(obj, new)
        return this
    }

    private fun swap(map: MargoMap, mapObject: MapObject<*>?)
    {
        if (mapObject == null)
        {
            map.deleteObject(position)
        }
        else
        {
            map.addObject(mapObject)
        }
    }
}