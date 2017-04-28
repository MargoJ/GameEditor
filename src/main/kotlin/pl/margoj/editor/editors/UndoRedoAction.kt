package pl.margoj.editor.editors

import pl.margoj.mrf.MargoResource

interface UndoRedoAction<E : AbstractEditor<E, T>, T : MargoResource> : UndoAction<E, T>, RedoAction<E, T>