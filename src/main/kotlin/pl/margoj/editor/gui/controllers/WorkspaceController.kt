package pl.margoj.editor.gui.controllers

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.KeyCharacterCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.listener.LayerListener
import pl.margoj.editor.gui.listener.MapListener
import pl.margoj.editor.gui.listener.TilesetSelectionListener
import pl.margoj.editor.gui.objects.MapCursorBox
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.gui.utils.IconUtils
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.editor.map.cursor.ErasingCursor
import pl.margoj.editor.map.cursor.FillingCursor
import pl.margoj.editor.map.cursor.SingleElementCursor
import pl.margoj.editor.map.objects.GatewayObjectTool
import pl.margoj.editor.map.objects.RemoveObjectTool
import pl.margoj.editor.utils.FileUtils
import pl.margoj.editor.utils.JarUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.bundle.local.MargoMRFResourceBundle
import pl.margoj.mrf.bundle.local.MountedResourceBundle
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.ResourceBundle

class WorkspaceController : CustomController
{
    lateinit var scene: CustomScene<*>
        private set

    @FXML
    lateinit var tabPane: TabPane

    @FXML
    lateinit var tabMapEditor: Tab

    @FXML
    lateinit var tabItemEditor: Tab

    @FXML
    lateinit var btnResourceNew: Button

    @FXML
    lateinit var btnResourceLoadFromFile: Button

    @FXML
    lateinit var btnResourceSaveToFile: Button

    @FXML
    lateinit var btnResourceSaveAs: Button

    @FXML
    lateinit var listResourceView: ListView<Text>

    @FXML
    lateinit var fieldResourceSearch: TextField

    @FXML
    lateinit var listBundlesLastUsed: ListView<Text>

    @FXML
    lateinit var leftMenuChoices: ChoiceBox<String>

    @FXML
    lateinit var mapTilesetCanvasContainer: ScrollPane

    @FXML
    lateinit var mapTilesetCanvas: Canvas

    @FXML
    lateinit var objectsList: ListView<String>

    @FXML
    lateinit var mapCanvasContainer: ScrollPane

    @FXML
    lateinit var canvasHolder: StackPane

    @FXML
    lateinit var mapCanvas: Canvas

    @FXML
    lateinit var objectCanvas: Canvas

    @FXML
    lateinit var menuMapNew: MenuItem

    @FXML
    lateinit var menuMapOpen: MenuItem

    @FXML
    lateinit var menuMapSave: MenuItem

    @FXML
    lateinit var menuMapSaveAs: MenuItem

    @FXML
    lateinit var menuEditUndo: MenuItem

    @FXML
    lateinit var menuEditRedo: MenuItem

    @FXML
    lateinit var menuEditMap: MenuItem

    @FXML
    lateinit var menuEditMapMetadata: MenuItem

    @FXML
    lateinit var menuViewRedraw: MenuItem

    @FXML
    lateinit var menuBundleSaveToBundle: MenuItem

    @FXML
    lateinit var menuHelpAbout: MenuItem

    @FXML
    lateinit var btnLayer1: RadioButton

    @FXML
    lateinit var btnLayer2: RadioButton

    @FXML
    lateinit var btnLayer3: RadioButton

    @FXML
    lateinit var btnLayer4: RadioButton

    @FXML
    lateinit var btnLayer5: RadioButton

    @FXML
    lateinit var btnLayer6: RadioButton

    @FXML
    lateinit var btnLayer7: RadioButton

    @FXML
    lateinit var btnLayer8: RadioButton

    @FXML
    lateinit var btnLayer9: RadioButton

    @FXML
    lateinit var btnLayer10: RadioButton

    @FXML
    lateinit var btnLayerCollisions: RadioButton

    @FXML
    lateinit var btnLayerObject: RadioButton

    @FXML
    lateinit var btnShowGrid: CheckBox

    @FXML
    lateinit var labelMapId: Label

    @FXML
    lateinit var labelMapName: Label

    @FXML
    lateinit var hboxCursors: HBox

    @FXML
    lateinit var tilesetListLocal: ListView<String>

    @FXML
    lateinit var tilesetListBundle: ListView<String>

    @FXML
    lateinit var tilesetButtonToLocal: Button

    @FXML
    lateinit var tilesetButtonToBundle: Button

    @FXML
    lateinit var tilesetButtonOpenFolder: Button

    @FXML
    lateinit var tilesetButtonReload: Button

    @FXML
    lateinit var tilesetButtonUpdateBundle: Button

    @FXML
    lateinit var tilesetButtonUpdateLocal: Button

    @FXML
    lateinit var paneItemPropertiesContainer: VBox

    @FXML
    lateinit var fieldSearchItemProperty: TextField

    @FXML
    lateinit var menuItemNew: MenuItem

    @FXML
    lateinit var menuItemOpen: MenuItem

    @FXML
    lateinit var menuItemSave: MenuItem

    @FXML
    lateinit var menuItemSaveAs: MenuItem

    @FXML
    lateinit var menuItemUndo: MenuItem

    @FXML
    lateinit var menuItemRedo: MenuItem

    @FXML
    lateinit var menuItemSaveToBundle: MenuItem

    @FXML
    lateinit var menuMap: MenuBar

    @FXML
    lateinit var menuItems: MenuBar

    @FXML
    lateinit var buttonQuickSaveItem: Button

    @FXML
    lateinit var buttonDeleteItem: Button

    @FXML
    lateinit var listItemList: ListView<Text>

    @FXML
    lateinit var fieldSearchItem: TextField

    @FXML
    lateinit var buttonAddNewItem: Button

    @FXML
    lateinit var titledPaneItemEditorTitle: TitledPane

    val mapEditorItems: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(
                this.menuMapNew, this.menuMapOpen, this.menuMapSave, this.menuMapSaveAs,
                this.menuEditUndo, this.menuEditRedo, this.menuEditMap, this.menuEditMapMetadata,
                this.menuViewRedraw,
                this.menuBundleSaveToBundle,
                this.menuHelpAbout
        )
    }

    val itemEditorItems: Array<MenuItem> by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(
                this.menuItemNew, this.menuItemOpen, this.menuItemSave, this.menuItemSaveAs,
                this.menuItemUndo, this.menuItemRedo,
                this.menuItemSaveToBundle
        )
    }

    val isMapEditorSelected: Boolean get() = this.tabPane.selectionModel?.selectedItem == this.tabMapEditor

    val isItemEditorSelected: Boolean get() = this.tabPane.selectionModel?.selectedItem == this.tabItemEditor

    val fileChooser = FileChooser()

    private val logger = LogManager.getLogger(WorkspaceController::class.java)

    override fun preInit(scene: CustomScene<*>)
    {
        logger.trace("preInit()")

        this.scene = scene
        val editor = MargoJEditor.INSTANCE
        val mapEditor = editor.mapEditor

        LayerListener(
                mapEditor,
                this.btnLayer1,
                this.btnLayer2,
                this.btnLayer3,
                this.btnLayer4,
                this.btnLayer5,
                this.btnLayer6,
                this.btnLayer7,
                this.btnLayer8,
                this.btnLayer9,
                this.btnLayer10,
                this.btnLayerCollisions,
                this.btnLayerObject
        ).autoRegister(scene.scene)

        val accelerators = scene.scene.accelerators
        accelerators[KeyCharacterCombination("G")] = Runnable { this.btnShowGrid.isSelected = !this.btnShowGrid.isSelected }

        val mapCursorBox = MapCursorBox(this.hboxCursors, mapEditor)
        mapCursorBox.addButton(SingleElementCursor.INSTANCE, "single_element", "Pojedyńczy element (A)")
        mapCursorBox.addButton(ErasingCursor.INSTANCE, "eraser", "Gumka (S)")
        mapCursorBox.addButton(FillingCursor.INSTANCE, "fill", "Wypełnij (D)")

        accelerators[KeyCharacterCombination("A")] = Runnable { mapEditor.cursor = SingleElementCursor.INSTANCE }
        accelerators[KeyCharacterCombination("S")] = Runnable { mapEditor.cursor = ErasingCursor.INSTANCE }
        accelerators[KeyCharacterCombination("D")] = Runnable { mapEditor.cursor = FillingCursor.INSTANCE }

        // multiple accelerators
        this.scene.scene.onKeyPressed = EventHandler { keyEvent ->
            fun checkForAcceleratorsWhen(items: Array<MenuItem>, `when`: Boolean)
            {
                if (!`when`)
                {
                    return
                }

                items.filter { it.accelerator != null && it.accelerator.match(keyEvent) }.forEach { keyEvent.consume(); it.fire() }
            }

            checkForAcceleratorsWhen(this.mapEditorItems, this.isMapEditorSelected)
            checkForAcceleratorsWhen(this.itemEditorItems, this.isItemEditorSelected)
        }

        mapEditor.mapCursorBox = mapCursorBox

        scene.stage.onCloseRequest = EventHandler {
            event ->
            logger.trace("stage.onCloseRequest()")

            editor.editors
                    .filter { it.currentEditingObject != null && it.touched && !it.askForSave() }
                    .forEach { event.consume() }

            if (editor.currentResourceBundle != null && editor.currentResourceBundle!!.touched)
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
                                "Nie zapisałeś zmian w zestawie zasobów\nCzy chcesz zapisać je teraz?"
                        )
                        .showAndWait()

                when (button?.buttonData)
                {
                    ButtonBar.ButtonData.YES -> editor.currentResourceBundle!!.saveBundle()
                    ButtonBar.ButtonData.NO -> Platform.exit()
                    else -> event.consume()
                }
            }
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        logger.trace("initialize()")

        // global init
        FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.PROGRAM_DIRECTORY)
        FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.TILESETS_DIRECTORY)
        FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.MOUNT_DIRECTORY)
        FileUtils.ensureDirectoryCreationIfAvailable(FileUtils.TEMP_DIRECTORY)

        val editor = MargoJEditor.INSTANCE
        editor.init(this)

        // =========
        // RESOURCES
        // =========

        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Zestaw zasobow MargoJ (*.mrf)", "*.mrf"))

        editor.addResourceDisableListener { this.menuBundleSaveToBundle.isDisable = it }
        editor.addResourceDisableListener { this.menuItemSaveToBundle.isDisable = it }
        editor.addResourceDisableListener { this.buttonDeleteItem.isDisable = it }
        editor.addResourceDisableListener { this.buttonAddNewItem.isDisable = it }
        editor.addResourceDisableListener { this.fieldSearchItem.isDisable = it }

        btnResourceNew.onAction = EventHandler {
            editor.currentResourceBundle = MountedResourceBundle(File(FileUtils.MOUNT_DIRECTORY, System.currentTimeMillis().toString()))
        }


        btnResourceLoadFromFile.onAction = EventHandler {
            fileChooser.title = "Wybierz plik"
            val file = fileChooser.showOpenDialog(scene.stage) ?: return@EventHandler

            editor.loadMRF(file)
        }

        btnResourceSaveToFile.onAction = EventHandler {
            if (editor.currentResourceBundle == null)
            {
                QuickAlert.create()
                        .error()
                        .header("Błąd odczytu zestawu zasobów")
                        .content("Brak wybranego zestawu")
                        .showAndWait()

                return@EventHandler
            }

            val bundle = editor.currentResourceBundle!!
            if (bundle is MargoMRFResourceBundle && bundle.mrfFile == editor.mrfFile)
            {
                bundle.saveBundle()
            }
            else
            {
                if (editor.mrfFile == null)
                {
                    this.btnResourceSaveAs.fire()
                    return@EventHandler
                }

                val mount = File(FileUtils.MOUNT_DIRECTORY, "newmrf_" + System.currentTimeMillis())
                mount.mkdirs()

                val newBundle = MargoMRFResourceBundle(editor.mrfFile!!, mount)

                for (view in bundle.resources)
                {
                    newBundle.saveResource(view, bundle.loadResource(view)!!)
                }

                newBundle.saveBundle()
                mount.delete()

                editor.currentResourceBundle = newBundle
            }

            QuickAlert.create()
                    .information()
                    .header("Zasób zapisany")
                    .content("Zasób został zapisany pomyślnie")
                    .showAndWait()
        }

        btnResourceSaveAs.onAction = EventHandler {
            fileChooser.title = "Gdzie chcesz zapisać?"

            var file = fileChooser.showSaveDialog(this.scene.stage) ?: return@EventHandler

            if (!file.name.endsWith(".mrf"))
            {
                file = File(file.absolutePath + ".mrf")
            }

            editor.mrfFile = file
            this.btnResourceSaveToFile.fire()
        }


        // =========
        // MAP EDITOR
        // =========

        val mapEditor = editor.mapEditor
        mapEditor.init()

        // menu
        this.menuMapNew.onAction = EventHandler {
            if (this.isMapEditorSelected && mapEditor.askForSaveIfNecessary())
            {
                FXUtils.loadDialog("map/new", "Tworzenie nowej mapy", scene.stage)
            }
        }

        this.menuMapOpen.onAction = EventHandler {
            if (!this.isMapEditorSelected || !mapEditor.askForSaveIfNecessary())
            {
                return@EventHandler
            }

            val fileChooser = FileChooser()
            fileChooser.title = "Wybierz plik"
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Mapa formatu MargoJ (*.mjm)", "*.mjm"))
            val file = fileChooser.showOpenDialog(scene.stage) ?: return@EventHandler

            try
            {
                mapEditor.openFile(file)
                QuickAlert.create().information().header("Mapa załadowana pomyślnie").showAndWait()
            }
            catch (e: Exception)
            {
                QuickAlert.create().exception(e).content("Nie można załadować mapy").showAndWait()
            }
        }

        this.menuMapSave.onAction = EventHandler {
            if (isMapEditorSelected)
            {
                mapEditor.saveWithDialog()
            }
        }

        this.menuMapSaveAs.onAction = EventHandler {
            if (isMapEditorSelected)
            {
                mapEditor.saveAsWithDialog()
            }
        }

        this.menuEditUndo.onAction = EventHandler {
            if (isMapEditorSelected)
            {
                mapEditor.undo()
            }
        }

        this.menuEditRedo.onAction = EventHandler {
            if (isMapEditorSelected)
            {
                mapEditor.redo()
            }
        }

        this.menuEditMap.onAction = EventHandler {
            if (!isMapEditorSelected)
            {
                return@EventHandler
            }

            if (mapEditor.currentMap == null)
            {
                QuickAlert.create().error().header("Nie można edytować mapy").content("Brak załadowanej mapy").showAndWait()
                return@EventHandler
            }

            FXUtils.loadDialog("map/edit", "Edycja mapy", scene.stage)
        }

        this.menuEditMapMetadata.onAction = EventHandler {
            if (!isMapEditorSelected)
            {
                return@EventHandler
            }

            if (mapEditor.currentMap == null)
            {
                QuickAlert.create().error().header("Nie można edytować mapy").content("Brak załadowanej mapy").showAndWait()
                return@EventHandler
            }

            FXUtils.loadDialog("map/editmetadata", "Edycja danych mapy", scene.stage, mapEditor.currentMap)
        }

        this.menuViewRedraw.onAction = EventHandler {
            if (!isMapEditorSelected)
            {
                return@EventHandler
            }

            if (mapEditor.currentMap == null)
            {
                QuickAlert.create().error().header("Nie można odświerzyć mapy").content("Brak załadowanej mapy").showAndWait()
                return@EventHandler
            }

            mapEditor.resetImageCacheBuffers()
            mapEditor.redrawMap()
            mapEditor.redrawObjects()
            QuickAlert.create().information().header("Odświerzanie mapy").content("Mapa została odświerzona").showAndWait()
        }


        this.menuBundleSaveToBundle.onAction = EventHandler {
            if (!isMapEditorSelected)
            {
                return@EventHandler
            }

            val map = mapEditor.currentMap
            if (map == null)
            {
                QuickAlert.create()
                        .error()
                        .header("Nie można dodać mapy")
                        .content("Brak załadowanej mapy")
                        .showAndWait()
                return@EventHandler
            }

            mapEditor.save = { editor.addMapToBundle(mapEditor.currentMap!!) }
            mapEditor.save()
        }


        this.menuHelpAbout.onAction = EventHandler { FXUtils.loadDialog("about", "O programie", scene.stage) }

        // copy default tilesets
//        val defaults = JarUtils.getFilesInJar("/default-tiles")
//        val tilesetDirectory = File(FileUtils.TILESETS_DIRECTORY)
//
//        for (path in defaults)
//        {
//            val tilesetFile = File(tilesetDirectory, path.toString())
//            if (!tilesetFile.exists())
//            {
//                Files.copy(WorkspaceController::class.java.getResourceAsStream("/default-tiles/" + path.toString()), tilesetFile.toPath())
//            }
//        }

        mapEditor.reloadTilesets()

        // init tileset list
        mapEditor.selectTilesetsOnLeftMenu()

        mapEditor.selectedTileset = mapEditor.autoTileset

        // add listener for tiles selection
        this.mapTilesetCanvas.addEventHandler(MouseEvent.ANY, TilesetSelectionListener(this.mapTilesetCanvas, mapEditor, this.mapTilesetCanvasContainer))

        // add listener for map
        this.objectCanvas.addEventHandler(MouseEvent.ANY, MapListener(mapEditor))

        // listener for grid
        this.btnShowGrid.selectedProperty().addListener { _, _, newValue -> mapEditor.showGrid = newValue }

        IconUtils.removeDefaultClass(this.btnShowGrid, "check-box")
        IconUtils.createBinding(this.btnShowGrid.graphicProperty(), this.btnShowGrid.selectedProperty(), "grid")
        IconUtils.addTooltip(this.btnShowGrid, "Pokazuj siatkę")

        // init objects
        mapEditor.mapObjectTools.add(GatewayObjectTool())
        mapEditor.mapObjectTools.add(RemoveObjectTool())

        // =========
        // ITEM EDITOR
        // =========

        val itemEditor = editor.itemEditor
        itemEditor.init()

        this.fieldSearchItemProperty.textProperty().addListener { _, _, new ->
            itemEditor.propertiesRenderer.render(this.paneItemPropertiesContainer, new)
        }

        this.menuItemNew.onAction = EventHandler {
            if (this.isItemEditorSelected && itemEditor.askForSaveIfNecessary())
            {
                FXUtils.loadDialog("item/new", "Tworzenie nowego przedmiotu", this.scene.stage)
            }
        }

        this.menuItemOpen.onAction = EventHandler {
            if (!this.isItemEditorSelected || !itemEditor.askForSaveIfNecessary())
            {
                return@EventHandler
            }

            val fileChooser = FileChooser()
            fileChooser.title = "Wybierz plik"
            fileChooser.extensionFilters.add(itemEditor.extensionFilter)
            val file = fileChooser.showOpenDialog(scene.stage) ?: return@EventHandler

            try
            {
                itemEditor.openFile(file)
                QuickAlert.create().information().header("Przedmiot załadowany pomyślnie").showAndWait()
            }
            catch (e: Exception)
            {
                QuickAlert.create().exception(e).content("Nie można załadować przedmiotu").showAndWait()
            }
        }

        this.menuItemSave.onAction = EventHandler {
            if (!isItemEditorSelected)
            {
                return@EventHandler
            }

            itemEditor.saveWithDialog()
        }

        this.menuItemSaveAs.onAction = EventHandler {
            if (!isItemEditorSelected)
            {
                return@EventHandler
            }

            itemEditor.saveAsWithDialog()
        }

        this.menuItemUndo.onAction = EventHandler {
            if (!isItemEditorSelected)
            {
                return@EventHandler
            }

            itemEditor.undo()
        }

        this.menuItemRedo.onAction = EventHandler {
            if (!isItemEditorSelected)
            {
                return@EventHandler
            }

            itemEditor.redo()
        }

        this.menuItemSaveToBundle.onAction = EventHandler {
            if (!isItemEditorSelected)
            {
                return@EventHandler
            }

            val item = itemEditor.currentItem
            if (item == null)
            {
                QuickAlert.create()
                        .error()
                        .header("Nie można dodać przedmiotu")
                        .content("Brak załadowanego przedmiotu")
                        .showAndWait()
                return@EventHandler
            }

            itemEditor.save = { editor.addItemToBundle(itemEditor.currentItem!!) }

            editor.addItemToBundle(item)
        }

        this.buttonQuickSaveItem.onAction = EventHandler {
            itemEditor.save()
        }

        this.buttonAddNewItem.onAction = EventHandler {
            if (this.isItemEditorSelected && itemEditor.askForSaveIfNecessary())
            {
                val oldItem = itemEditor.currentItem

                val dialog = FXUtils.loadDialog("item/new", "Tworzenie nowego przedmiotu w zestawie zasobów", this.scene.stage)
                dialog.setOnHiding {
                    val newItem = itemEditor.currentItem
                    if (newItem == null || oldItem === newItem)
                    {
                        QuickAlert
                                .create()
                                .warning()
                                .header("Operacja przerwana!")
                                .content("Przedmiot nie zostal utworzony")
                                .showAndWait()
                        return@setOnHiding
                    }

                    itemEditor.save = { editor.addItemToBundle(itemEditor.currentItem!!) }

                    editor.addItemToBundle(newItem)
                }
            }
        }


        this.buttonDeleteItem.onAction = EventHandler {
            val item = itemEditor.currentItem
            val view = if (item == null) null else editor.currentResourceBundle?.getResource(MargoResource.Category.ITEMS, item.id)

            if (item == null || view == null)
            {
                QuickAlert
                        .create()
                        .error()
                        .header("Operacja przerwana!")
                        .content("Nie mozesz usunac tego przedmiotu z zestawu poniewaz nie znajduje sie on w aktualnym zestawie")
                        .showAndWait()

                return@EventHandler
            }

            val result = QuickAlert.create()
                    .confirmation()
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO))
                    .header("Czy na pewno chcesz usuna przedmiot z zestawu!")
                    .content("Przedmiot: ${view.id} [${view.name}]")
                    .showAndWait()

            if (result?.buttonData != ButtonBar.ButtonData.YES)
            {
                return@EventHandler
            }

            editor.currentResourceBundle!!.deleteResource(view)
            editor.updateResourceView()

            QuickAlert
                    .create()
                    .information()
                    .header("Przedmiot ${view.id} [{${view.name}] został usunięty poprawnie!")
                    .showAndWait()
        }

        this.fieldSearchItem.textProperty().addListener { _, _, _ -> itemEditor.updateItemsView() }
    }

}