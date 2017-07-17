@file:Suppress("UNCHECKED_CAST")

package pl.margoj.editor.editors

import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.controllers.WorkspaceController
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.mrf.MargoResource
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList

abstract class AbstractEditor<E : AbstractEditor<E, T>, T : MargoResource>(val editor: MargoJEditor, val extensionFilter: ExtensionFilter, val extension: String)
{
    private val logger = LogManager.getLogger(this.javaClass)
    private val undoActions = LinkedList<UndoAction<E>>()
    private val redoActions = LinkedList<RedoAction<E>>()
    var undoLimit = 50
    var saveFile: File? = null
    var save: () -> Boolean = this::saveWithDialog
    lateinit var workspaceController: WorkspaceController
    var touched: Boolean = false
        set(value)
        {
            field = value
            logger.trace("this.touched = $value")
        }

    fun touch()
    {
        this.touched = true
    }

    fun addUndoAction(action: UndoAction<E>)
    {
        logger.debug("New undo action $action")
        while (this.undoActions.size >= this.undoLimit)
        {
            this.undoActions.removeFirst()
        }
        this.undoActions.add(action)
        this.redoActions.clear()
        this.touch()
        this.updateUndoRedoMenu()
    }

    val newestUndo: UndoAction<E>
        get() = this.undoActions.last

    val newestRedo: RedoAction<E>
        get() = this.redoActions.last

    fun canUndo(): Boolean
    {
        return !this.undoActions.isEmpty()
    }

    fun canRedo(): Boolean
    {
        return !this.redoActions.isEmpty()
    }

    fun undo(): Boolean
    {
        if (!this.canUndo())
        {
            this.updateUndoRedoMenu()
            return false
        }
        logger.debug("Performing undo ${this.newestUndo}")
        val redo = this.newestUndo.undo(this as E)
        this.undoActions.removeLast()
        this.redoActions.add(redo)

        this.updateUndoRedoMenu()
        return true
    }

    fun redo(): Boolean
    {
        if (!this.canRedo())
        {
            this.updateUndoRedoMenu()
            return false
        }

        logger.debug("Performing redo ${this.newestRedo}")

        val undo = this.newestRedo.redo(this as E)
        this.redoActions.removeLast()
        this.undoActions.add(undo)

        this.updateUndoRedoMenu()
        return true
    }

    fun clearUndoRedoHistory()
    {
        logger.trace("clearUndoRedoHistory()")
        this.undoActions.clear()
        this.redoActions.clear()
        this.updateUndoRedoMenu()
    }

    fun saveAsWithDialog(): Boolean
    {
        logger.trace("saveAsWithDialog()")
        if (this.currentEditingObject == null)
        {
            QuickAlert.create().error().header("Nie można zapisać").content("Nie wybrano żadnego obiektu do zapisu").showAndWait()
            return false
        }

        val fileChooser = FileChooser()
        fileChooser.title = "Gdzie chcesz zapisać?"
        fileChooser.extensionFilters.add(this.extensionFilter)
        var file = fileChooser.showSaveDialog(this.workspaceController.scene.stage) ?: return false

        if (!file.name.endsWith(this.extension))
        {
            file = File(file.absolutePath + this.extension)
        }

        this.saveFile = file

        this.saveWithDialog()

        return true
    }

    fun saveWithDialog(): Boolean
    {
        logger.trace("saveWithDialog()")
        if (this.saveFile == null)
        {
            return this.saveAsWithDialog()
        }

        try
        {
            val bytes = this.doSave() ?: return false

            FileOutputStream(this.saveFile!!).use { out -> out.write(bytes) }

            this.touched = false
            QuickAlert.create().information().header("Plik zapisany pomyślnie").showAndWait()
            return true
        }
        catch (e: Exception)
        {
            QuickAlert.create().exception(e).content("Nie można zapisać pliku").showAndWait()
            return false
        }
    }

    fun askForSaveIfNecessary(): Boolean
    {
        logger.trace("askForSaveIfNecessary()")
        if (this.currentEditingObject == null || !this.touched)
        {
            return true
        }

        return this.askForSave()
    }

    fun askForSave(): Boolean
    {
        logger.trace("askForSave()")

        val button = QuickAlert.create()
                .confirmation()
                .buttonTypes(
                        ButtonType("Tak", ButtonBar.ButtonData.YES),
                        ButtonType("Nie", ButtonBar.ButtonData.NO),
                        ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE)
                )
                .header("Niezapisane zmiany")
                .content(
                        "Nie zapisałeś zmian w pliku: ${this.currentEditingObject?.resourceReadableName}\nCzy chcesz zapisać je teraz?"
                )
                .showAndWait()

        when (button?.buttonData)
        {
            ButtonBar.ButtonData.YES ->
            {
                return this.save()
            }
            ButtonBar.ButtonData.NO -> return true
            else -> return false
        }
    }

    abstract fun openFile(file: File)

    protected abstract fun doSave(): ByteArray?

    protected fun defaultUndoRedoUpdate(undo: MenuItem, redo: MenuItem)
    {
        undo.isDisable = !this.canUndo()
        undo.text = if (this.canUndo()) "Cofnij: " + this.newestUndo.actionName else "Cofnij"
        redo.isDisable = !this.canRedo()
        redo.text = if (this.canRedo()) "Powtórz: " + this.newestRedo.actionName else "Powtórz"
    }

    abstract fun updateUndoRedoMenu()

    abstract val currentEditingObject: T?
}
