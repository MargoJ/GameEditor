package pl.margoj.editor.map.actions

import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.map.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.fragment.MapFragment
import java.util.ArrayList
import java.util.Collections
import java.util.stream.Collectors

class SimpleUndoRedo(changes: List<SimpleUndoRedo.Change>) : UndoRedoAction<MapEditor>
{
    private val changes: List<Change>

    init
    {
        this.changes = ArrayList(changes)
        Collections.reverse(this.changes)
    }

    override fun undo(editor: MapEditor): SimpleUndoRedo
    {
        val map = editor.currentMap!!
        this.changes.forEach { change -> map.setFragment(change.oldFragment) }
        this.doRedrawIfNecessary(editor, map, this.changes, true)
        return this
    }

    override fun redo(editor: MapEditor): SimpleUndoRedo
    {
        val map = editor.currentMap!!
        this.changes.forEach { change -> map.setFragment(change.newFragment) }
        this.doRedrawIfNecessary(editor, map, this.changes, false)
        return this
    }

    private fun doRedrawIfNecessary(editor: MapEditor, map: MargoMap, values: Collection<Change>, old: Boolean)
    {
        if (editor.currentMap != map)
        {
            return
        }

        val selection = Selection(
                values.stream()
                        .map { change -> if (old) change.oldFragment.point else change.newFragment.point }
                        .collect(Collectors.toList())
        )

        editor.redrawMapFragment(selection)
    }

    override val actionName: String get() = "Zmiana bloków"

    class Change(val oldFragment: MapFragment, val newFragment: MapFragment)
    {
        init
        {
            if (oldFragment.point != newFragment.point)
            {
                throw IllegalArgumentException("Points don't match")
            }
        }
    }
}
