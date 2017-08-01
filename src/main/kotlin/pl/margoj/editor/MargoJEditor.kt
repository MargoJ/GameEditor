package pl.margoj.editor

import javafx.collections.FXCollections
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.input.KeyCode
import javafx.scene.text.Text
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.gui.controllers.WorkspaceController
import pl.margoj.editor.gui.objects.ResourceCellFactory
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.editor.item.ItemEditor
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.npc.NpcEditor
import pl.margoj.editor.utils.FileUtils
import pl.margoj.editor.utils.LastBundlesUtil
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.Paths
import pl.margoj.mrf.ResourceView
import pl.margoj.mrf.bundle.MountResourceBundle
import pl.margoj.mrf.bundle.local.MargoMRFResourceBundle
import pl.margoj.mrf.item.MargoItem
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.tileset.TilesetFile
import pl.margoj.mrf.npc.NpcScript
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.Comparator
import java.util.TreeMap
import java.util.TreeSet

class MargoJEditor private constructor()
{
    lateinit var workspaceController: WorkspaceController
        private set

    lateinit var tilesetEditor: TilesetEditor
        private set

    private val logger = LogManager.getLogger(MargoJEditor::class.java)
    private var allItems: List<Text>? = null

    val mapEditor: MapEditor = MapEditor(this)
    val itemEditor: ItemEditor = ItemEditor(this)
    val npcEditor: NpcEditor = NpcEditor(this)
    var mrfFile: File? = null
    val editors = listOf(this.mapEditor, this.itemEditor, this.npcEditor)
    val resourceItems: MutableList<(Boolean) -> Unit> = ArrayList()

    var currentResourceBundle: MountResourceBundle? = null
        set(value)
        {
            logger.trace("currentResourceBundle = $currentResourceBundle")
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

            this.resourceItems.forEach { it(value == null) }
            field = value
            this.updateResourceView()

            if (value is MargoMRFResourceBundle)
            {
                LastBundlesUtil.addToList(value.mrfFile.absolutePath)
                this.updateLastBundles()
            }

            tilesetEditor.bundle = value
        }

    fun init(workspaceController: WorkspaceController)
    {
        logger.trace("init($workspaceController)")

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

        this.workspaceController.fieldResourceSearch.textProperty().addListener { _, _, _ -> this.updateResourceViewElement() }

        this.updateLastBundles()

        this.tilesetEditor = TilesetEditor(workspaceController)
        this.tilesetEditor.init(this)

        Paths.TEMP_DIR = FileUtils.TEMP_DIRECTORY
        this.updateResourceView()
    }

    fun updateResourceView()
    {
        logger.trace("updateResourceView()")

        if (this.currentResourceBundle == null)
        {
            this.allItems = null
            this.updateResourceViewElement()
            this.itemEditor.updateItemsView()
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

        val items = ArrayList<Text>(resources.size + views.size)

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

        this.allItems = items
        this.updateResourceViewElement()

        this.itemEditor.updateItemsView()
        this.npcEditor.updateNpcsView()
    }

    fun updateResourceViewElement()
    {
        logger.trace("updateResourceViewElement()")

        if (this.allItems == null)
        {
            this.workspaceController.listResourceView.items = FXCollections.singletonObservableList(Text("Nie załadowano żadnego zasobu"))
            return
        }

        val visibleItems = FXCollections.observableList(ArrayList<Text>(this.allItems!!.size))

        for (item in this.allItems!!)
        {
            if (item !is ResourceText || StringUtils.containsIgnoreCase(item.text, this.workspaceController.fieldResourceSearch.text))
            {
                visibleItems.add(item)
            }
        }

        this.workspaceController.listResourceView.items = visibleItems
    }

    fun updateLastBundles()
    {
        logger.trace("updateLastBundles()")

        val bundles = LastBundlesUtil.getLastBundles()
        val texts = FXCollections.observableList(ArrayList<Text>(bundles.size))

        for (bundle in bundles)
        {
            val text = Text(bundle)

            text.setOnMouseClicked { event ->
                if (event.clickCount == 2)
                {
                    this.loadMRF(File(bundle))
                }
            }

            texts.add(text)
        }

        this.workspaceController.listBundlesLastUsed.items = texts
    }

    fun addResourceDisableListener(disable: (Boolean) -> Unit)
    {
        logger.trace("addResourceDisableListener($disable)")

        this.resourceItems.add(disable)
    }

    fun loadResource(view: ResourceView)
    {
        logger.trace("loadResource($view)")

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

        logger.debug("view.category = ${view.category}")

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
            MargoResource.Category.ITEMS ->
            {
                this.itemEditor.loadFromBundle(input)
            }
            MargoResource.Category.NPC_SCRIPTS ->
            {
                this.npcEditor.loadFromBundle(view.id, input)
            }
            else -> QuickAlert.create().error().header("Błąd oczytu zestawu zasobów").content("Nieznana kategoria").showAndWait()
        }
    }

    fun deleteResource(view: ResourceView)
    {
        logger.trace("deleteResource($view)")

        if (this.currentResourceBundle == null)
        {
            QuickAlert.create().error().header("Błąd odczytu zestawu zasobów").content("Brak wybranego zestawu").showAndWait()
            return
        }

        val type = QuickAlert.create().confirmation().header("Usuwanie zasobu").content("Czy na pewno chcesz usunac zasob ${view.resourceReadableName}").showAndWait()

        if (type?.buttonData != ButtonBar.ButtonData.OK_DONE)
        {
            return
        }

        this.currentResourceBundle!!.deleteResource(view)

        QuickAlert.create().information().header("Usunięto zasób").content("Zasób ${view.resourceReadableName} został usunięty").showAndWait()

        this.updateResourceView()
    }


    fun loadMRF(file: File)
    {
        logger.trace("loadMRF($file)")

        val mount = File(FileUtils.MOUNT_DIRECTORY, "loadmrf_" + System.currentTimeMillis())
        mount.mkdirs()
        val bundle = MargoMRFResourceBundle(file, mount)
        this.currentResourceBundle = bundle
        this.mrfFile = file


        QuickAlert.create()
                .information()
                .header("Zasób otworzony")
                .content("Zasób został otworzony pomyślnie")
                .showAndWait()
    }

    fun addMapToBundle(map: MargoMap): Boolean
    {
        logger.trace("addMapToBundle($map)")

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

            if (result?.buttonData == ButtonBar.ButtonData.YES)
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
        QuickAlert.create().information().header("Mapa została dodana do zestawu zasobów!").showAndWait()
        return true
    }

    fun addItemToBundle(item: MargoItem): Boolean
    {
        logger.trace("addItemToBundle($item)")

        // check if all tilesets are in bundle
        val bundle = this.currentResourceBundle!!
        val stream = ByteArrayInputStream(itemEditor.serializer.serialize(item))
        bundle.saveResource(item, stream)
        this.updateResourceView()
        this.itemEditor.touched = false
        QuickAlert.create().information().header("Przedmiot został dodany do zestawu zasobów!").showAndWait()

        return true
    }

    fun addNpcScriptToBundle(script: NpcScript): Boolean
    {
        logger.trace("addNpcScriptToBundle($script)")

        // check if all tilesets are in bundle
        val bundle = this.currentResourceBundle!!
        val stream = ByteArrayInputStream(npcEditor.serializer.serialize(script))
        bundle.saveResource(script, stream)
        this.updateResourceView()
        this.npcEditor.touched = false
        QuickAlert.create().information().header("Skrypt został dodany do zestawu zasobów!").showAndWait()

        return true
    }

    companion object
    {
        val INSTANCE = MargoJEditor()
    }
}

class ResourceText(text: String, val view: ResourceView) : Text(text)