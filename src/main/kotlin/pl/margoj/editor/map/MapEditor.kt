package pl.margoj.editor.map

import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.editors.AbstractEditor
import pl.margoj.editor.gui.objects.MapCursorBox
import pl.margoj.editor.gui.objects.WithImageCellFactory
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.editor.map.cursor.Cursor
import pl.margoj.editor.map.objects.MapObjectTool
import pl.margoj.editor.utils.FileUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.fragment.MapFragment
import pl.margoj.mrf.map.objects.MapObject
import pl.margoj.mrf.map.serialization.MapDeserializer
import pl.margoj.mrf.map.serialization.MapSerializer
import pl.margoj.mrf.map.tileset.AutoTileset
import pl.margoj.mrf.map.tileset.Tileset
import pl.margoj.mrf.map.tileset.TilesetFile
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

private val COLLISION = javafx.scene.paint.Color(1.0, 1.0, 1.0, 0.8)

class MapEditor(editor: MargoJEditor) : AbstractEditor<MapEditor, MargoMap>(editor, FileChooser.ExtensionFilter("Mapa formatu MargoJ (*.mjm)", "*.mjm"), ".mjm")
{
    private val logger = LogManager.getLogger(MapEditor::class.java)
    private var mapFragmentBuffer: Array<Array<Array<MapFragment?>?>?>? = null
    private var mapImageBuffer: Array<Array<BufferedImage?>?>? = null

    val tilesetFiles = HashMap<String, TilesetFile>()
    val tilesets = HashMap<String, Tileset>()
    val autoTileset: AutoTileset? get() = this.tilesets[AutoTileset.AUTO] as AutoTileset?
    val mapSerializer = MapSerializer()
    val mapDeserializer = MapDeserializer(this.tilesets.values)
    val mapObjectTools: MutableList<MapObjectTool<*>> = ArrayList(5)

    var selectedTileset: Tileset? = null
        set(value)
        {
            logger.trace("selectedTileset = $value")
            field = value
            this.tilesetSelection = RectangleSelection(0, 0, 1, 1)
            this.redrawTileset()
        }

    var tilesetSelection: RectangleSelection? = null
        set(value)
        {
            logger.trace("tilesetSelection = $value")

            field = value
            this.redrawTileset()
        }

    var currentMap: MargoMap? = null
        set(value)
        {
            logger.trace("currentMap = $currentMap")

            if (value != null && !value.validate())
            {
                field = null
                return
            }
            if (field != value)
            {
                field = value
                this.clearUndoRedoHistory()
            }

            this.resetImageCacheBuffers()
            this.redrawMap()
            this.redrawObjects()
            this.updateMapInfo()
        }

    var currentLayer = 0
        set(value)
        {
            logger.trace("currentLayer = $currentLayer")

            val redrawObjects = this.currentLayer == MargoMap.COLLISION_LAYER || value == MargoMap.COLLISION_LAYER ||
                    this.currentLayer == MargoMap.OBJECT_LAYER || value == MargoMap.OBJECT_LAYER


            if (value == MargoMap.OBJECT_LAYER)
            {
                this.selectObjectsOnLeftMenu()
            }
            else if (field == MargoMap.OBJECT_LAYER) // update if last one was objects, don't update when tileset layer -> tileset layer change
            {
                this.selectTilesetsOnLeftMenu()
            }

            field = value

            if (redrawObjects)
            {
                this.redrawObjects()
            }
        }

    var showGrid: Boolean = false
        set(value)
        {
            field = value
            this.redrawObjects()
        }

    var cursor: Cursor = Cursor.DEFAULT
        set(value)
        {
            logger.trace("cursor = $value")
            field = value
            this.mapCursorBox?.changeSelection(cursor)
        }

    val tilesetListSelected: Boolean get() = !this.objectsListSelected
    val objectsListSelected: Boolean get() = this.currentLayer == MargoMap.OBJECT_LAYER

    var mapSelection: Selection? = null
    var mapCursorBox: MapCursorBox? = null
    var lastTilesetnameSelected: String = AutoTileset.AUTO

    val objectCanvas: Canvas? get() = this.workspaceController.objectCanvas
    override val currentEditingObject: MargoMap? get() = this.currentMap

    fun init()
    {
        logger.trace("init()")
        this.updateUndoRedoMenu()
    }


    private fun addTilesetFile(file: File)
    {
        logger.trace("addTilesetFile($file)")
        val filename = file.name

        if (filename.endsWith(".png"))
        {
            val name = filename.substring(0, filename.lastIndexOf('.'))
            this.tilesetFiles.put(name, TilesetFile(file, name, filename.startsWith("auto-")))
        }
    }

    fun reloadTilesets()
    {
        logger.trace("reloadTilesets()")

        this.tilesetFiles.clear()

        for (file in File(FileUtils.TILESETS_DIRECTORY).listFiles()!!)
        {
            this.addTilesetFile(file)
        }

        if (this.editor.currentResourceBundle != null)
        {
            val bundle = this.editor.currentResourceBundle!!
            val tilesets = bundle.getResourcesByCategory(MargoResource.Category.TILESETS)

            for (tileset in tilesets)
            {
                bundle.loadResource(tileset)
                this.addTilesetFile(bundle.getLocalFile(tileset))
            }
        }

        this.tilesets.clear()

        val autos = ArrayList<TilesetFile>()

        for (tilesetFile in this.tilesetFiles.values)
        {
            if (tilesetFile.auto)
            {
                autos.add(tilesetFile)
            }
            else
            {
                this.tilesets.put(tilesetFile.name, Tileset(tilesetFile.name, tilesetFile.image, Collections.singletonList(tilesetFile)))
            }
        }

        this.tilesets.put(AutoTileset.AUTO, AutoTileset(AutoTileset.AUTO, autos))

        if (this.tilesetListSelected)
        {
            this.selectTilesetsOnLeftMenu()
        }
    }

    fun getTilesetFile(filename: String): TilesetFile?
    {
        return this.tilesetFiles[filename]
    }

    fun getTileset(name: String): Tileset?
    {
        return this.tilesets[name]
    }

    private fun resizeMapCanvas(width: Int, height: Int)
    {
        logger.trace("resizeMapCanvas(width = $width, height = $height)")
        this.workspaceController.mapCanvas.width = (width * 32).toDouble()
        this.workspaceController.mapCanvas.height = (height * 32).toDouble()
        this.workspaceController.objectCanvas.width = (width * 32).toDouble()
        this.workspaceController.objectCanvas.height = (height * 32).toDouble()
        this.workspaceController.canvasHolder.setPrefSize((width * 32).toDouble(), (height * 32).toDouble())
    }

    fun redrawTileset()
    {
        val tilesetCanvas = this.workspaceController.mapTilesetCanvas

        tilesetCanvas.width = this.selectedTileset!!.wholeTilesetWidth.toDouble()
        tilesetCanvas.height = this.selectedTileset!!.wholeTilesetHeight.toDouble()

        val context = tilesetCanvas.graphicsContext2D
        context.fill = Color.BLACK
        context.fillRect(0.0, 0.0, this.selectedTileset!!.wholeTilesetWidth.toDouble(), this.selectedTileset!!.wholeTilesetHeight.toDouble())

        context.drawImage(SwingFXUtils.toFXImage(this.selectedTileset!!.image, null), 0.0, 0.0)

        if (this.tilesetSelection != null)
        {
            context.stroke = Color.PALEVIOLETRED
            context.lineWidth = 1.0
            this.tilesetSelection!!.draw(context, 32)
        }
    }

    fun resetImageCacheBuffers()
    {
        logger.trace("resetImageCacheBuffers()")

        this.mapFragmentBuffer = null
        this.mapImageBuffer = null
    }

    fun resizeMap(width: Int, height: Int): Boolean
    {
        logger.trace("resizeMap($width, $height)")

        if (this.currentMap == null)
        {
            return false
        }
        this.currentMap!!.resize(width, height)
        this.resetImageCacheBuffers()
        this.redrawMap()
        this.redrawObjects()
        return true
    }

    fun updateMapInfo()
    {
        logger.trace("updateMapInfo()")

        this.workspaceController.labelMapId.text = if (this.currentMap == null) "-" else this.currentMap!!.id
        this.workspaceController.labelMapName.text = if (this.currentMap == null) "-" else this.currentMap!!.name
    }

    fun redrawMap()
    {
        logger.trace("redrawMap() - ALL")

        if (this.currentMap != null)
        {
            this.resizeMapCanvas(currentMap!!.width, currentMap!!.height)

            this.redrawMapFragment(RectangleSelection(0, 0, this.currentMap!!.width, this.currentMap!!.height))
        }
    }

    fun redrawObjects()
    {
        logger.trace("redrawObjects() - ALL")

        if (this.currentMap != null)
        {
            this.redrawObjects(RectangleSelection(0, 0, this.currentMap!!.width, this.currentMap!!.height))
        }
    }

    fun redrawObjects(selection: Selection)
    {
        val g = this.workspaceController.objectCanvas.graphicsContext2D ?: return

        for (point in selection.points!!)
        {
            g.fill = Color.TRANSPARENT
            g.clearRect(point.x * 32.0, point.y * 32.0, 32.0, 32.0)

            this.currentMap!!.objects.filter { it.position == point }.forEach { this.getToolForObject(it)?.render(g, it) }

            if (this.currentLayer == MargoMap.COLLISION_LAYER)
            {
                if (this.currentMap!!.getCollisionAt(point))
                {
                    g.fill = COLLISION
                    g.fillRect(point.x * 32.0 + 3, point.y * 32.0 + 3, 26.0, 26.0)
                }
            }

            if (this.showGrid)
            {
                g.stroke = Color.RED
                g.fill = Color.RED
                g.lineWidth = 2.0
                g.strokeRect(point.x * 32.0, point.y * 32.0, 32.0, 32.0)
                g.fillText("X: ${point.x}", point.x * 32.0, point.y * 32.0 + 10)
                g.fillText("Y: ${point.y}", point.x * 32.0, point.y * 32.0 + 23)
            }
        }
    }

    fun redrawMapFragment(fragment: Selection)
    {
        this.redrawMapFragment0(fragment, false, false)
    }

    private fun redrawMapFragment0(fragment: Selection, selection: Boolean, redraw: Boolean)
    {
        if (this.currentMap == null)
        {
            return
        }

        val map = this.currentMap as MargoMap

        @Suppress("UNCHECKED_CAST")
        if (this.mapFragmentBuffer == null)
        {
            this.mapFragmentBuffer = Array(map.width) { Array(map.height) { arrayOfNulls<MapFragment?>(MargoMap.LAYERS) } } as Array<Array<Array<MapFragment?>?>?>
        }

        if (this.mapImageBuffer == null)
        {
            this.mapImageBuffer = Array(map.width) { arrayOfNulls<BufferedImage?>(map.height) }
        }
        this.resizeMapCanvas(map.width, map.height)

        val fragments: Array<Array<Array<MapFragment>>> = map.fragments
        val context = this.workspaceController.mapCanvas.graphicsContext2D
        val redraws = ArrayList<Point>()

        for (x in 0 until map.width)
        {
            for (y in 0 until map.height)
            {
                if (!fragment.points!!.contains(Point(x, y)))
                {
                    continue
                }

                var redrawNeeded = this.mapImageBuffer!![x]!![y] == null || this.mapFragmentBuffer!![x]!![y] == null

                if (!redrawNeeded)
                {
                    for (layer in 0 until MargoMap.LAYERS)
                    {
                        if (this.mapFragmentBuffer!![x]!![y]!![layer] != null || this.mapFragmentBuffer!![x]!![y]!![layer] != fragments[x][y][layer])
                        {
                            redrawNeeded = true
                            break
                        }
                    }
                }

                var image = this.mapImageBuffer!![x]!![y]

                if (redrawNeeded)
                {
                    if (image == null)
                    {
                        image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
                        this.mapImageBuffer!![x]!![y] = image
                    }

                    val g = image.graphics
                    g.color = java.awt.Color.BLACK
                    g.fillRect(0, 0, 32, 32)

                    for (layer in 0 until MargoMap.LAYERS)
                    {
                        val mapFragment = fragments[x][y][layer]
                        this.mapFragmentBuffer!![x]!![y]!![layer] = mapFragment
                        mapFragment.draw(g)
                    }

                    redraws.addAll(Point(x, y).getNeighborhood(true))
                }

                context.drawImage(SwingFXUtils.toFXImage(image, null), 32.0 * x, 32.0 * y, 32.0, 32.0)
            }
        }

        if (!redraw)
        {
            for (point in redraws)
            {
                if (!this.currentMap!!.inBounds(point))
                {
                    continue
                }

                this.mapFragmentBuffer!![point.x]!![point.y] = arrayOfNulls<MapFragment?>(MargoMap.LAYERS)
                this.mapImageBuffer!![point.x]!![point.y] = null
            }

            if (!redraws.isEmpty())
            {
                this.redrawMapFragment0(Selection(redraws), false, true)
            }
        }

        if (!selection)
        {
            if (this.mapSelection != null)
            {
                this.redrawMapFragment0(this.mapSelection!!, true, true)
                context.stroke = Color.PALEVIOLETRED
                context.lineWidth = 1.0
                this.mapSelection!!.draw(context, 32)
            }
        }
    }

    fun selectTilesetsOnLeftMenu()
    {
        logger.trace("selectTilesetsOnLeftMenu()")
        val leftMenuChoices = this.workspaceController.leftMenuChoices

        leftMenuChoices.onAction = EventHandler {
            if (leftMenuChoices.value == null)
            {
                return@EventHandler
            }
            this.selectedTileset = this.getTileset(leftMenuChoices.value)
            this.lastTilesetnameSelected = leftMenuChoices.value
        }

        val mapTilesetChoices = leftMenuChoices.items
        mapTilesetChoices.clear()
        mapTilesetChoices.add(this.lastTilesetnameSelected)
        leftMenuChoices.value = lastTilesetnameSelected

        tilesetFiles.values.stream().filter { !it.auto }.sorted(Comparator.comparing(TilesetFile::name)).forEach { mapTilesetChoices.add(it.name) }

        this.workspaceController.objectsList.isVisible = false
    }

    fun selectObjectsOnLeftMenu()
    {
        logger.trace("selectObjectsOnLeftMenu()")
        val leftMenuChoices = this.workspaceController.leftMenuChoices
        leftMenuChoices.onAction = null

        val mapObjectsChoices = leftMenuChoices.items
        mapObjectsChoices.clear()
        val objects = "Obiekty"
        mapObjectsChoices.add(objects)
        leftMenuChoices.value = objects

        val cellMap = hashMapOf<String, Image>()

        this.workspaceController.objectsList.cellFactory = WithImageCellFactory(cellMap)
        val items = this.workspaceController.objectsList.items
        items.clear()

        this.mapObjectTools.forEach {
            cellMap[it.name] = it.icon
            items.add(it.name)
        }

        this.workspaceController.objectsList.isVisible = true
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : MapObject<*>> getToolForObject(mapObject: T): MapObjectTool<T>?
    {
        return this.mapObjectTools.stream().filter { it.objectType == mapObject.javaClass }.findAny().orElse(null) as? MapObjectTool<T>?
    }

    fun redrawObject(mapObject: MapObject<*>)
    {
        logger.trace("redrawObject($mapObject)")

        if (this.currentMap == null)
        {
            return
        }
        val tool = this.getToolForObject(mapObject) ?: return

        this.redrawObjects(Selection(tool.getPoints(mapObject)))
    }

    override fun openFile(file: File)
    {
        logger.trace("openFile($file)")

        FileInputStream(file).use { input ->
            this.currentMap = this.mapDeserializer.deserialize(input)

            if (this.currentMap == null)
            {
                QuickAlert.create().information().header("Brak wymaganych tilesetow!").showAndWait()
                return
            }

            this.saveFile = file
            this.save = this::saveWithDialog
        }
    }

    override fun doSave(): ByteArray?
    {
        logger.trace("doSave()")

        return if (this.currentMap == null) null else this.mapSerializer.serialize(this.currentMap!!)
    }

    override fun updateUndoRedoMenu()
    {
        this.defaultUndoRedoUpdate(this.workspaceController.menuEditUndo, this.workspaceController.menuEditRedo)
    }
}