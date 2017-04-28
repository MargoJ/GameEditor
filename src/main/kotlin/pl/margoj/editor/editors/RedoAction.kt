package pl.margoj.editor.editors

import pl.margoj.mrf.MargoResource

interface RedoAction<E : AbstractEditor<E, T>, T : MargoResource>
{
    fun redo(editor: E, obj: T): UndoAction<E, T>

    val actionName: String
}
