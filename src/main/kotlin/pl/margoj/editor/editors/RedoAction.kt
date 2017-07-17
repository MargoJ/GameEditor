package pl.margoj.editor.editors

interface RedoAction<E : AbstractEditor<E, *>>
{
    fun redo(editor: E): UndoAction<E>

    val actionName: String
}
