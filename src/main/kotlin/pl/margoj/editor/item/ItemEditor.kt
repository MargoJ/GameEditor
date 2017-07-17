package pl.margoj.editor.item

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.text.Text
import javafx.stage.FileChooser
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.editors.AbstractEditor
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.editor.item.renderer.ItemPropertyRenderer
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.item.ItemProperty
import pl.margoj.mrf.item.MargoItem
import pl.margoj.mrf.item.serialization.ItemDeserializer
import pl.margoj.mrf.item.serialization.ItemSerializer
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.TreeSet

class ItemEditor(editor: MargoJEditor) : AbstractEditor<ItemEditor, MargoItem>(editor, FileChooser.ExtensionFilter("Przedmiot formatu MargoJ (*.mjm)", "*.mjm"), ".mjm")
{
    private val logger = LogManager.getLogger(ItemEditor::class.java)
    private var allItems: Collection<Text>? = null

    val serializer = ItemSerializer()
    val deserializer = ItemDeserializer()

    var currentItem: MargoItem? = null
        set(value)
        {
            logger.trace("currentItem = $value")
            if (value != field)
            {
                this.clearUndoRedoHistory()
            }

            field = value

            this.propertiesRenderer.actualNodes.values.forEach { it.isDisable = value == null }

            if (value != null)
            {
                this.updatePropertiesFromItem(value)
                this.workspaceController.titledPaneItemEditorTitle.text = "Edytor przedmiotów: ${value.id}.mjm"
            }
        }

    override val currentEditingObject: MargoItem? get() = this.currentItem

    lateinit var propertiesRenderer: PropertiesRenderer
        private set

    fun init()
    {
        logger.trace("init")
        this.propertiesRenderer = PropertiesRenderer(PropertiesRenderer.DEFAULT_PROPERTIES_RENDERERS)
        this.propertiesRenderer.calculate()
        this.propertiesRenderer.render(this.workspaceController.paneItemPropertiesContainer, "")

        this.propertiesRenderer.actualNodes.values.forEach { it.isDisable = true }
    }

    fun applyPropertiesOnItem(item: MargoItem): Boolean
    {
        logger.trace("applyPropertiesOnItem($item)")
        @Suppress("UNCHECKED_CAST")
        for (property in ItemProperty.properties)
        {
            val renderer = this.propertiesRenderer.getRendererOf(property)!! as ItemPropertyRenderer<Any, ItemProperty<Any>, Node>
            val node = this.propertiesRenderer.actualNodes[property]!!

            val returned = renderer.convert(property as ItemProperty<Any>, node) ?: return false

            if (returned == property.default)
            {
                continue
            }

            item[property] = returned
        }

        return true
    }

    fun updatePropertiesFromItem(item: MargoItem)
    {
        logger.trace("updatePropertiesFromItem($item)")

        @Suppress("UNCHECKED_CAST")
        for (property in ItemProperty.properties)
        {
            val renderer = this.propertiesRenderer.getRendererOf(property)!! as ItemPropertyRenderer<Any, ItemProperty<Any>, Node>
            val node = this.propertiesRenderer.actualNodes[property]!!

            renderer.update(property as ItemProperty<Any>, node, item[property])
        }
    }

    fun updateItemsView()
    {
        logger.trace("updateItemsView()")

        if (this.editor.currentResourceBundle == null)
        {
            this.allItems = null
            this.updateItemsViewElement()
            return
        }

        val sortedSet = TreeSet<Text> { o1, o2 -> o1.text.compareTo(o2.text); }
        this.editor.currentResourceBundle!!.getResourcesByCategory(MargoResource.Category.ITEMS).forEach { view ->
            val text = Text("${view.id} [${view.name}]")

            text.onMouseClicked = EventHandler {
                if(it.clickCount == 2)
                {
                    this.loadFromBundle(this.editor.currentResourceBundle!!.loadResource(view)!!)
                }
            }

            sortedSet.add(text)
        }

        this.allItems = sortedSet
        this.updateItemsViewElement()
    }

    fun updateItemsViewElement()
    {
        logger.trace("updateItemsViewElement()")

        if (this.allItems == null)
        {
            this.workspaceController.listItemList.items = FXCollections.singletonObservableList(Text("Nie załadowano żadnego zasobu"))
            return
        }

        val visibleItems = FXCollections.observableList(ArrayList<Text>(this.allItems!!.size))

        this.allItems!!.filterTo(visibleItems) { StringUtils.containsIgnoreCase(it.text, this.workspaceController.fieldSearchItem.text) }

        this.workspaceController.listItemList.items = visibleItems
    }

    fun loadFromBundle(input: InputStream)
    {
        logger.trace("loadFromBundle($input)")

        if (this.currentItem != null && this.touched && !this.askForSave())
        {
            return
        }

        this.currentItem = this.deserializer.deserialize(input)
        this.save = { this.editor.addItemToBundle(this.currentItem!!) }

        QuickAlert.create().information().header("Załadowano zasób").content("Zasób został załadowany poprawnie").showAndWait()
        this.workspaceController.tabPane.selectionModel.select(this.workspaceController.tabItemEditor)
    }

    override fun doSave(): ByteArray?
    {
        logger.trace("doSave()")

        val item = this.currentItem ?: return null

        if (!this.applyPropertiesOnItem(item))
        {
            return null
        }

        return this.serializer.serialize(item)
    }

    override fun openFile(file: File)
    {
        logger.trace("openFile(${file.absolutePath})")

        FileInputStream(file).use { input ->
            this.currentItem = this.deserializer.deserialize(input)
            this.saveFile = file
            this.save = this::saveWithDialog
        }
    }

    override fun updateUndoRedoMenu()
    {
        this.defaultUndoRedoUpdate(this.workspaceController.menuItemUndo, this.workspaceController.menuItemRedo)
    }
}
