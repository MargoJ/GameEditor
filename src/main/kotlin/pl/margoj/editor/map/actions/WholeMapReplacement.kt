package pl.margoj.editor.map.actions

import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.editor.map.MapEditor
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.fragment.MapFragment

class WholeMapReplacement(private val oldFragments: Array<Array<Array<MapFragment>>>, private val newFragments: Array<Array<Array<MapFragment>>>,
                          private val oldName: String, private val newName: String
) : UndoRedoAction<MapEditor, MargoMap>
{


    override fun undo(editor: MapEditor, obj: MargoMap): RedoAction<MapEditor, MargoMap>
    {
        this.replace(editor, obj, true)
        return this
    }

    override fun redo(editor: MapEditor, obj: MargoMap): UndoAction<MapEditor, MargoMap>
    {
        this.replace(editor, obj, false)
        return this
    }

    override val actionName: String = "Edycja mapy"

    private fun replace(editor: MapEditor, map: MargoMap, old: Boolean)
    {
        map.fragments = if (old) this.oldFragments else this.newFragments
        map.width = map.fragments.size
        map.height = if (map.fragments.isEmpty()) 0 else map.fragments[0].size
        map.name = if (old) this.oldName else this.newName

        if (map == editor.currentMap)
        {
            editor.resetImageCacheBuffers()
            editor.redrawMap()
            editor.redrawObjects()
            editor.updateMapInfo()
        }
    }
}
