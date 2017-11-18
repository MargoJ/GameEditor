package pl.margoj.editor.graphic

import javafx.collections.FXCollections
import javafx.scene.text.Text
import javafx.stage.FileChooser
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.editors.AbstractEditor
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicDeserializer
import pl.margoj.mrf.graphics.GraphicResource
import pl.margoj.mrf.graphics.GraphicSerializer
import java.io.File
import java.util.TreeSet

class GraphicEditor(editor: MargoJEditor) : AbstractEditor<GraphicEditor, GraphicResource>(editor, FileChooser.ExtensionFilter("Grafika formatu MargoJ (*.mjg)", "*.mjg"), ".mjg")
{
    companion object
    {
        val graphicSerializer = GraphicSerializer()
        val graphicDeserializer = GraphicDeserializer()
    }

    private val logger = LogManager.getLogger(GraphicEditor::class.java)
    private var items: MutableMap<GraphicResource.GraphicCategory, MutableSet<Text>> = HashMap(GraphicResource.GraphicCategory.values().size)

    var fileChooser = FileChooser()

    override val currentEditingObject: GraphicResource? = null

    init
    {
        GraphicResource.GraphicCategory.values().forEach { this.items.put(it, TreeSet({ o1, o2 -> o1.text.compareTo(o2.text) })) }

        fileChooser.extensionFilters.setAll(
                FileChooser.ExtensionFilter("Wspierane formaty grafik", "*.gif", "*.png")
        )
    }

    fun updateGraphicsView()
    {
        this.logger.trace("updateGraphicsView()")

        this.items.values.forEach { it.clear() }

        if (MargoJEditor.INSTANCE.currentResourceBundle == null)
        {
            return
        }

        val graphics = MargoJEditor.INSTANCE.currentResourceBundle!!.getResourcesByCategory(MargoResource.Category.GRAPHIC).sortedBy { it.id }

        for (graphic in graphics)
        {
            val category = GraphicResource.GraphicCategory.findById(graphic.meta!!.get("cat").asByte)!!
            this.items.get(category)!!.add(GraphicText("\t${graphic.name}", graphic.id))
        }

        this.updateGraphicsViewElement()
    }

    fun updateGraphicsViewElement()
    {
        this.logger.trace("updateGraphicsViewElement()")

        if (this.items.isEmpty())
        {
            val text = Text(" - Brak - ")
            text.style = "-fx-fill: red"
            this.workspaceController.listGraphics.items = FXCollections.singletonObservableList(text)
            return
        }

        val out = FXCollections.observableList(ArrayList<Text>(this.items.size))

        for ((category, elements) in this.items)
        {
            out.add(Text(category.displayName).also { it.style = "-fx-font-weight: bold" })

            if (elements.isNotEmpty())
            {
                for (element in elements)
                {
                    if (StringUtils.containsIgnoreCase(element.text, this.workspaceController.fieldSearchGraphics.text))
                    {
                        out.add(element)
                    }
                }
            }
            else
            {
                out.add(Text("\tBrak zasobów w tej kategorii").also { it.style = "-fx-fill: red" })
                continue
            }
        }

        this.workspaceController.listGraphics.items = out
    }

    override fun openFile(file: File)
    {
        throw UnsupportedOperationException()
    }

    override fun doSave(): ByteArray?
    {
        throw UnsupportedOperationException()
    }

    override fun updateUndoRedoMenu()
    {
    }

    class GraphicText(text: String, val graphicId: String) : Text(text)
}