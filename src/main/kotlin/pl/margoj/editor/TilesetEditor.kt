package pl.margoj.editor

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import org.apache.commons.io.IOUtils
import pl.margoj.editor.gui.controllers.WorkspaceController
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.editor.utils.FileUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.bundle.MountResourceBundle
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.LinkedList

class TilesetEditor(val workspaceController: WorkspaceController)
{
    var bundle: MountResourceBundle? = null
        set(value)
        {
            field = value
            this.reload()
        }

    private lateinit var nodes: Collection<Node>
    lateinit var editor: MargoJEditor
        private set

    fun init(editor: MargoJEditor)
    {
        this.editor = editor

        workspaceController.tilesetButtonToLocal.isDisable = true
        workspaceController.tilesetButtonToBundle.isDisable = true

        this.nodes = Arrays.asList(workspaceController.tilesetListLocal, workspaceController.tilesetListBundle, workspaceController.tilesetButtonUpdateBundle, workspaceController.tilesetButtonUpdateLocal)

        addListener(workspaceController.tilesetListLocal, workspaceController.tilesetListBundle, workspaceController.tilesetButtonToBundle, workspaceController.tilesetButtonToLocal)
        addListener(workspaceController.tilesetListBundle, workspaceController.tilesetListLocal, workspaceController.tilesetButtonToLocal, workspaceController.tilesetButtonToBundle)

        workspaceController.tilesetButtonOpenFolder.onAction = EventHandler { Desktop.getDesktop().open(File(FileUtils.TILESETS_DIRECTORY)) }
        workspaceController.tilesetButtonReload.onAction = EventHandler { this.reload() }

        workspaceController.tilesetListLocal.selectionModel.selectionMode = SelectionMode.MULTIPLE
        workspaceController.tilesetListBundle.selectionModel.selectionMode = SelectionMode.MULTIPLE


        val toBundleAction = EventHandler<ActionEvent> {
            val items = workspaceController.tilesetListLocal.selectionModel.selectedItems
            val errors = LinkedList<String>()
            if (items != null && !items.isEmpty())
            {
                for (id in items)
                {
                    val file = File(FileUtils.TILESETS_DIRECTORY, "$id.png")
                    if (!file.exists())
                    {
                        errors.add("Nie znaleziono: $id.png")
                        continue
                    }
                    else if (!MargoResource.ID_PATTERN.matcher(id).matches())
                    {
                        errors.add("Nazwa pliku jest niepoprawna: $id.png")
                        continue
                    }

                    bundle!!.saveResource(ResourceView(id, "", null, MargoResource.Category.TILESETS, "$id.png"), FileInputStream(file))
                    bundle!!.touched = true
                }
            }

            FXUtils.showMultipleErrorsAlert("Nie znaleziono następujących tilesetów", errors)
            workspaceController.tilesetButtonToBundle.isDisable = true

            this.reload()
        }

        workspaceController.tilesetButtonToBundle.onAction = toBundleAction
        workspaceController.tilesetListLocal.onMouseClicked = EventHandler {
            if (it.clickCount == 2)
            {
                toBundleAction.handle(ActionEvent())
            }
        }

        val toLocalAction = EventHandler<ActionEvent> {
            val items = workspaceController.tilesetListBundle.selectionModel.selectedItems

            val errors = LinkedList<String>()
            if (items != null && !items.isEmpty())
            {
                val bundle = bundle!!
                for (id in items)
                {
                    val view = bundle.getResource(MargoResource.Category.TILESETS, id)
                    if (view == null)
                    {
                        errors.add("Nie znaleziono: $id.png")
                        continue
                    }

                    try
                    {
                        val file = File(FileUtils.TILESETS_DIRECTORY, "$id.png")
                        FileOutputStream(file).use {
                            IOUtils.copyLarge(bundle.loadResource(view)!!, it)
                        }
                    }
                    catch (e: IOException)
                    {
                        errors.add(e.toString())
                        return@EventHandler
                    }

                    bundle.deleteResource(view)
                    bundle.touched = true
                }
            }

            FXUtils.showMultipleErrorsAlert("Nie znaleziono następujących tilesetów", errors)
            workspaceController.tilesetButtonToLocal.isDisable = true
            this.reload()
        }

        workspaceController.tilesetButtonToLocal.onAction = toLocalAction
        workspaceController.tilesetListBundle.onMouseClicked = EventHandler {
            if (it.clickCount == 2)
            {
                toLocalAction.handle(ActionEvent())
            }
        }

        workspaceController.tilesetButtonUpdateLocal.onAction = EventHandler {
            bundle!!.resources.filter { it.category == MargoResource.Category.TILESETS }.forEach { view ->
                val file = File(FileUtils.TILESETS_DIRECTORY, "${view.id}.png")

                FileOutputStream(file).use {
                    IOUtils.copyLarge(bundle!!.loadResource(view)!!, it)
                }
            }

            QuickAlert.create().information().header("Załadowano tilesety!").content("Wszystkie tilesety z zestawu zostaly zapisane w folderze tilesetow!").showAndWait()
        }

        workspaceController.tilesetButtonUpdateBundle.onAction = EventHandler {
            bundle!!.resources.filter { it.category == MargoResource.Category.TILESETS }.forEach { view ->
                val filename = "${view.id}.png"
                val file = File(FileUtils.TILESETS_DIRECTORY, filename)

                if (file.exists())
                {
                    bundle!!.saveResource(ResourceView(filename.substring(0, filename.lastIndexOf('.')), "", null, MargoResource.Category.TILESETS, filename), FileInputStream(file))
                    bundle!!.touched = true
                }
            }

            QuickAlert.create().information().header("Załadowano tilesety!").content("Wszystkie zostały zaaktualizowane w zestawie!").showAndWait()
        }

        this.reload()
    }

    private fun addListener(list: ListView<String>, other: ListView<String>, enable: Button, disable: Button)
    {
        list.selectionModel.selectedItemProperty().addListener({ _, _, new ->

            if (new != null)
            {
                other.selectionModel.select(null)
                enable.isDisable = false
                disable.isDisable = true
            }
        })
    }

    fun reload()
    {
        this.workspaceController.tilesetListLocal.items.clear()
        this.workspaceController.tilesetListBundle.items.clear()

        Arrays.stream(File(FileUtils.TILESETS_DIRECTORY).list()).filter { it.endsWith(".png") }.sorted().forEach {
            this.workspaceController.tilesetListLocal.items.add(it.substring(0, it.lastIndexOf('.')))
        }

        if (this.bundle == null)
        {
            this.nodes.forEach { it.isDisable = true }
        }
        else
        {
            this.nodes.forEach { it.isDisable = false }

            this.workspaceController.tilesetListBundle.items.clear()

            this.bundle!!.resources.stream().filter { it.category == MargoResource.Category.TILESETS }.map { it.id }.sorted().forEach {
                this.workspaceController.tilesetListBundle.items.add(it)
                this.workspaceController.tilesetListLocal.items.remove(it)
            }
        }

        this.editor.updateResourceView()
        this.editor.mapEditor.reloadTilesets()
    }
}