@file:Suppress("UNCHECKED_CAST")

package pl.margoj.editor.editors

import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.controllers.WorkspaceController
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.mrf.MargoResource
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList

abstract class AbstractEditor<E : AbstractEditor<E, T>, T : MargoResource>(protected val editor: MargoJEditor, private val extensionFilter: ExtensionFilter, private val extension: String)
{
    private val undoActions = LinkedList<UndoAction<E, T>>()
    private val redoActions = LinkedList<RedoAction<E, T>>()
    var touched: Boolean = false
    var undoLimit = 50
    var saveFile: File? = null
    var save: () -> Boolean = this::saveWithDialog
    lateinit var workspaceController: WorkspaceController

    fun touch()
    {
        this.touched = true
    }

    fun addUndoAction(action: UndoAction<E, T>)
    {
        while (this.undoActions.size >= this.undoLimit)
        {
            this.undoActions.removeFirst()
        }
        this.undoActions.add(action)
        this.redoActions.clear()
        this.touch()
        this.updateUndoRedoMenu()
    }

    val newestUndo: UndoAction<E, T>
        get() = this.undoActions.last

    val newestRedo: RedoAction<E, T>
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
        val redo = this.newestUndo.undo(this as E, this.currentEditingObject!!)
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
        val undo = this.newestRedo.redo(this as E, this.currentEditingObject!!)
        this.redoActions.removeLast()
        this.undoActions.add(undo)

        this.updateUndoRedoMenu()
        return true
    }

    fun saveAsWithDialog(): Boolean
    {
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
        if (this.saveFile == null)
        {
            return this.saveAsWithDialog()
        }

        try
        {
            FileOutputStream(this.saveFile!!).use { out -> out.write(this.doSave()) }

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

    fun askForSave(): Boolean
    {
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

        when (button.buttonData)
        {
            ButtonBar.ButtonData.YES ->
            {
                return this.save()
            }
            ButtonBar.ButtonData.NO -> return true
            else -> return false
        }
    }

    protected abstract fun doSave(): ByteArray?

    abstract fun updateUndoRedoMenu()

    abstract val currentEditingObject: T?
}
