package pl.margoj.editor.editors

interface UndoAction<E : AbstractEditor<E, *>>
{
    fun undo(editor: E): RedoAction<E>

    fun isValid(): Boolean = true

    val actionName: String
}
