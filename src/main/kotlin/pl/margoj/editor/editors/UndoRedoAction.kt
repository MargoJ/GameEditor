package pl.margoj.editor.editors

interface UndoRedoAction<E : AbstractEditor<E, *>> : UndoAction<E>, RedoAction<E>