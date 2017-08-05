package pl.margoj.editor.gui.controllers

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import javafx.stage.FileChooser
import org.apache.commons.lang3.StringUtils
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.objects.GraphicCellFactory
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicDeserializer
import pl.margoj.mrf.graphics.GraphicSerializer
import java.net.URL
import java.util.ResourceBundle
import java.util.TreeSet

class GraphicsManagerController : CustomController
{
    companion object
    {
        val graphicSerializer = GraphicSerializer()
        val graphicDeserializer = GraphicDeserializer()
    }

    private var items: MutableSet<Text> = TreeSet({ o1, o2 -> o1.text.compareTo(o2.text) })
    lateinit var scene: CustomScene<*>
    var fileChooser = FileChooser()

    init
    {
        fileChooser.extensionFilters.setAll(
                FileChooser.ExtensionFilter("Wspierane formaty grafik", "*.gif", "*.png")
        )
    }

    @FXML
    lateinit var listGraphics: ListView<Text>

    @FXML
    lateinit var fieldSearchGraphics: TextField

    @FXML
    lateinit var buttonAddGraphic: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.reloadGraphics()

        this.listGraphics.cellFactory = GraphicCellFactory(this)

        this.fieldSearchGraphics.textProperty().addListener { _, _, _ -> this.reloadGraphicsView() }

        this.buttonAddGraphic.onAction = EventHandler {
            FXUtils.loadDialog("graphics/new", "Dodaj nową grafike", this.scene.stage, this)
        }
    }

    fun reloadGraphics()
    {
        this.items.clear()

        val graphics = MargoJEditor.INSTANCE.currentResourceBundle!!.getResourcesByCategory(MargoResource.Category.GRAPHIC).sortedBy { it.id }

        for (graphic in graphics)
        {
            this.items.add(GraphicText(graphic.name, graphic.id))
        }

        this.reloadGraphicsView()
    }

    fun reloadGraphicsView()
    {
        if (this.items.isEmpty())
        {
            val text = Text(" - Brak - ")
            text.style = "-fx-fill: red"
            this.listGraphics.items = FXCollections.singletonObservableList(text)
            return
        }

        val out = FXCollections.observableList(ArrayList<Text>(this.items.size))

        for (element in this.items)
        {
            if (StringUtils.containsIgnoreCase(element.text, this.fieldSearchGraphics.text))
            {
                out.add(element)
            }
        }

        this.listGraphics.items = out
    }
}

class GraphicText(text: String, val graphicId: String) : Text(text)