package pl.margoj.editor.npc

import pl.margoj.editor.editors.RedoAction
import pl.margoj.editor.editors.UndoAction
import pl.margoj.editor.editors.UndoRedoAction
import kotlin.concurrent.withLock

class NpcEditorTextChangeUndoRedo(val editor: NpcEditor, val old: String, val new: String) : UndoRedoAction<NpcEditor>
{
    override val actionName: String = "Edycja tekstu"

    override fun undo(editor: NpcEditor): RedoAction<NpcEditor>
    {
        this.update(editor, old)
        return this
    }

    override fun redo(editor: NpcEditor): UndoAction<NpcEditor>
    {
        this.update(editor, new)
        return this
    }

    private fun update(editor: NpcEditor, text: String)
    {
        editor.lock.withLock {
            editor.previousText = text
            editor.updateContent(text)
        }
    }
}