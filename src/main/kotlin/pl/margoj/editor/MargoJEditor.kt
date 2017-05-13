package pl.margoj.editor

import javafx.collections.FXCollections
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCode
import javafx.scene.text.Text
import pl.margoj.editor.gui.controllers.WorkspaceController
import pl.margoj.editor.gui.objects.ResourceCellFactory
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.utils.FileUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.Paths
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.bundle.MountResourceBundle
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.tileset.TilesetFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.Comparator
import java.util.LinkedList
import java.util.TreeMap
import java.util.TreeSet

class MargoJEditor private constructor()
{
    lateinit var workspaceController: WorkspaceController
        private set

    lateinit var tilesetEditor: TilesetEditor
        private set

    val mapEditor: MapEditor = MapEditor(this)
    var mrfFile: File? = null
    val editors = listOf(this.mapEditor)
    val resourceItems: MutableList<MenuItem> = ArrayList()

    var currentResourceBundle: MountResourceBundle? = null
        set(value)
        {
            val old = field
            if (old != null && old is AutoCloseable)
            {
                try
                {
                    old.close()
                }
                catch (e: Exception)
                {
                    System.err.println("Couldn't close the bundle")
                    e.printStackTrace()
                }
            }

            this.resourceItems.forEach { it.isDisable = value == null }
            field = value
            this.updateResourceView()
            tilesetEditor.bundle = value
        }

    fun init(workspaceController: WorkspaceController)
    {
        this.workspaceController = workspaceController
        this.editors.forEach { it.workspaceController = workspaceController }

        val resourceView = this.workspaceController.listResourceView
        resourceView.cellFactory = ResourceCellFactory(this)
        resourceView.setOnKeyPressed {
            event ->
            val text = resourceView.selectionModel.selectedItem
            if (event.code == KeyCode.DELETE && text != null && text is ResourceText)
            {
                this.deleteResource(text.view)
            }
        }

        this.tilesetEditor = TilesetEditor(workspaceController)
        this.tilesetEditor.init(this)

        Paths.TEMP_DIR = FileUtils.TEMP_DIRECTORY
        this.updateResourceView()
    }

    fun updateResourceView()
    {
        if (this.currentResourceBundle == null)
        {
            this.workspaceController.listResourceView.items = FXCollections.singletonObservableList(Text("Nie załadowano żadnego zasobu"))
            return
        }

        val resources = this.currentResourceBundle!!.resources
        val views = TreeMap<MargoResource.Category, TreeSet<ResourceView>>(Comparator.comparing(MargoResource.Category::name))

        for (category in MargoResource.Category.values())
        {
            views.put(category, TreeSet<ResourceView>(Comparator.comparing(ResourceView::id)))
        }

        for (view in resources)
        {
            views[view.category]!!.add(view)
        }

        val items = FXCollections.observableList(ArrayList<Text>(resources.size + views.size))

        for ((key, value) in views)
        {
            val categoryText = Text(key.readableName)
            categoryText.style = "-fx-font-weight: bold"
            items.add(categoryText)

            if (value.isEmpty())
            {
                val emptyText = Text("\tBrak zasobów w tej kategorii")
                emptyText.style = "-fx-fill: red"
                items.add(emptyText)
                continue
            }

            value.mapTo(items) { ResourceText("\t ${it.id} ${if (it.name.isEmpty()) "" else "[${it.name}]"}", it) }
        }


        this.workspaceController.listResourceView.items = items
    }

    fun addResourceItems(vararg items: MenuItem)
    {
        this.resourceItems.addAll(items)
    }

    fun loadResource(view: ResourceView)
    {
        if (this.currentResourceBundle == null)
        {
            QuickAlert.create().error().header("Błąd odczytu zestawu zasobów").content("Brak wybranego zestawu").showAndWait()
            return
        }

        val input = this.currentResourceBundle!!.loadResource(view)

        // TODO: Loading

        if (input == null)
        {
            QuickAlert.create().error().header("Błąd oczytu zestawu zasobów").content("Nieznany zasób").showAndWait()
            return
        }

        when (view.category)
        {
            MargoResource.Category.MAPS ->
            {
                if (this.mapEditor.currentMap != null && this.mapEditor.touched && !this.mapEditor.askForSave())
                {
                    return
                }

                this.mapEditor.currentMap = mapEditor.mapDeserializer.deserialize(input)

                if (mapEditor.currentMap == null)
                {
                    QuickAlert.create().information().header("Brak wymaganych tilesetow!").content("Załaduj je z zestawu").showAndWait()
                    return
                }

                mapEditor.save = { this.addMapToBundle(mapEditor.currentMap!!) }

                QuickAlert.create().information().header("Załadowano zasób").content("Zasób został załadowany poprawnie").showAndWait()
                this.workspaceController.tabPane.selectionModel.select(this.workspaceController.tabMapEditor)
            }
            else -> QuickAlert.create().error().header("Błąd oczytu zestawu zasobów").content("Nieznana kategoria").showAndWait()
        }
    }

    fun deleteResource(view: ResourceView)
    {
        if (this.currentResourceBundle == null)
        {
            QuickAlert.create().error().header("Błąd odczytu zestawu zasobów").content("Brak wybranego zestawu").showAndWait()
            return
        }

        val type = QuickAlert.create().confirmation().header("Usuwanie zasobu").content("Czy na pewno chcesz usunac zasob ${view.resourceReadableName}").showAndWait()

        if (type.buttonData != ButtonBar.ButtonData.OK_DONE)
        {
            return
        }

        this.currentResourceBundle!!.deleteResource(view)

        QuickAlert.create().information().header("Usunięto zasób").content("Zasób ${view.resourceReadableName} został usunięty").showAndWait()

        this.updateResourceView()
    }

    fun addMapToBundle(map: MargoMap): Boolean
    {
        // check if all tilesets are in bundle
        val bundle = this.currentResourceBundle!!
        val tilesetsInBundle = bundle.getResourcesByCategory(MargoResource.Category.TILESETS).map(MargoResource::id)
        val missingTilesets = HashSet<String>()

        map.forEach {
            layer ->
            layer.forEach {
                fragment ->

                if (fragment.tileset != null)
                {
                    fragment.tileset!!.files.stream().map(TilesetFile::name).filter { !tilesetsInBundle.contains(it) }.forEach { missingTilesets.add(it) }
                }
            }
        }

        if (missingTilesets.isNotEmpty())
        {
            val result = QuickAlert.create()
                    .confirmation()
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                    .header("Mapa nie może być załadowana, w zestawie brakuje tilesetów!")
                    .content("Czy chcesz je dodać?\nLista tilesetów: " + missingTilesets.joinToString("\n"))
                    .showAndWait()

            if(result.buttonData == ButtonBar.ButtonData.YES)
            {
                for (id in missingTilesets)
                {
                    bundle.saveResource(ResourceView(id, "", MargoResource.Category.TILESETS, "$id.png"), FileInputStream(File(FileUtils.TILESETS_DIRECTORY, "$id.png")))
                }

                tilesetEditor.reload()
            }
            else
            {
                return false
            }
        }

        // save it
        val stream = ByteArrayInputStream(mapEditor.mapSerializer.serialize(map))
        bundle.saveResource(map, stream)
        this.updateResourceView()
        this.mapEditor.touched = false
        return true
    }

    companion object
    {
        val INSTANCE = MargoJEditor()
    }
}

class ResourceText(text: String, val view: ResourceView) : Text(text)