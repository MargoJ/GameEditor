package pl.margoj.editor.gui.objects

import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.Callback
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.graphic.GraphicEditor
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.MargoResource
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class GraphicCellFactory(private val editor: GraphicEditor) : Callback<ListView<Text>, ListCell<Text>>
{
    override fun call(param: ListView<Text>): ListCell<Text>
    {
        return Cell()
    }

    private inner class Cell : ListCell<Text>()
    {
        override fun updateItem(item: Text?, empty: Boolean)
        {
            super.updateItem(item, empty)
            graphic = item

            if (item is GraphicEditor.GraphicText)
            {
                val id = item.graphicId
                val menu = ContextMenu()
                val showGraphic = MenuItem("Podejrzyj grafike")
                val deleteGraphic = MenuItem("Usuń grafike")

                showGraphic.onAction = EventHandler {
                    val bundle = MargoJEditor.INSTANCE.currentResourceBundle!!
                    val view = bundle.getResource(MargoResource.Category.GRAPHIC, id)!!
                    val resource = bundle.loadResource(view)!!

                    val icon = GraphicEditor.graphicDeserializer.deserialize(resource)
                    val image = ImageIO.read(ByteArrayInputStream(icon.icon.image))
                    val fxImage = SwingFXUtils.toFXImage(image, null)

                    val parent = HBox()
                    parent.alignment = Pos.CENTER
                    val imageView = ImageView(fxImage)
                    parent.setMinSize(200.0, 200.0)
                    parent.setPrefSize(image.width.toDouble(), image.height.toDouble())
                    parent.children.setAll(imageView)

                    val stage = Stage()
                    stage.isResizable = false
                    stage.title = view.name
                    stage.scene = Scene(parent)
                    stage.initOwner(editor.workspaceController.scene.stage)
                    stage.sizeToScene()
                    FXUtils.setStageIcon(stage, "icon.png")
                    stage.showAndWait()
                }

                deleteGraphic.setOnAction {
                    MargoJEditor.INSTANCE.deleteResource(MargoJEditor.INSTANCE.currentResourceBundle!!.getResource(MargoResource.Category.GRAPHIC, id)!!)
                }

                menu.items.addAll(showGraphic, deleteGraphic)

                this.contextMenu = menu

                this.setOnMouseClicked { event ->
                    if (event.clickCount == 2)
                    {
                        showGraphic.fire()
                    }
                }
            }
            else
            {
                contextMenu = null
                onMouseClicked = null
            }
        }
    }
}
