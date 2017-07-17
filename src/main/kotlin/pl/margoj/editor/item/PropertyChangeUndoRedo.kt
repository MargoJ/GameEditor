package pl.margoj.editor.item

import org.apache.logging.log4j.LogManager
import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import pl.margoj.mrf.item.ItemProperty

class PropertyChangeUndoRedo<T>(val property: ItemProperty<T>, val oldValue: T, val newValue: T) : UndoRedoAction<ItemEditor>
{
    private val logger = LogManager.getLogger(PropertyChangeUndoRedo::class.java)

    override val actionName: String = "Zmiana właściwości: " + property.name

    override fun undo(editor: ItemEditor): RedoAction<ItemEditor>
    {
        this.setTo(editor, oldValue)
        return this
    }

    override fun redo(editor: ItemEditor): UndoAction<ItemEditor>
    {
        this.setTo(editor, newValue)
        return this
    }

    private fun setTo(editor: ItemEditor, to: T)
    {
        logger.trace("setTo(editor = $editor, to = $to)")
        val item = editor.currentItem!!
        item[this.property] = to
        editor.updatePropertiesFromItem(item)
    }
}