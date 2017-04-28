package pl.margoj.editor.editors

import pl.margoj.mrf.MargoResource

interface UndoAction<E : AbstractEditor<E, T>, T : MargoResource>
{
    fun undo(editor: E, obj: T): RedoAction<E, T>

    val actionName: String
}
