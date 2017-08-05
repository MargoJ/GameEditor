package pl.margoj.editor.gui.controllers.dialog

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import org.apache.commons.io.IOUtils
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.controllers.GraphicsManagerController
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.mrf.MRFIcon
import pl.margoj.mrf.MRFIconFormat
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicResource
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.net.URL
import java.util.ArrayList
import java.util.ResourceBundle
import javax.imageio.ImageIO

class NewGraphicDialogContainer : CustomController
{
    private lateinit var scene: CustomScene<*>
    private lateinit var graphicsManagerController: GraphicsManagerController

    @FXML
    lateinit var fieldGraphicId: TextField

    @FXML
    lateinit var buttonGraphicConfirm: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun loadData(data: Any)
    {
        this.graphicsManagerController = data as GraphicsManagerController
    }

    override fun initialize(location: URL, resources: ResourceBundle?)
    {
        buttonGraphicConfirm.setOnAction {
            val errors = ArrayList<String>()

            if (this.fieldGraphicId.text.isEmpty())
            {
                errors.add("ID grafiki nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldGraphicId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldGraphicId.text.length > 127)
            {
                errors.add("ID grafiki nie moze przekraczac 127 znakow")
            }

            if (MargoJEditor.INSTANCE.currentResourceBundle!!.getResource(MargoResource.Category.GRAPHIC, this.fieldGraphicId.text) != null)
            {
                errors.add("Grafika z podanym ID juz istnieje")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas tworzenia nowego skryptu", errors)
                return@setOnAction
            }

            val result = this.graphicsManagerController.fileChooser.showOpenDialog(scene.stage)

            if (result == null)
            {
                return@setOnAction close()
            }

            val iis = ImageIO.createImageInputStream(result)
            val iterator = ImageIO.getImageReaders(iis)
            val format = if (iterator.hasNext()) MRFIconFormat.getByFormat(iterator.next().formatName) else null

            if (format == null)
            {
                QuickAlert.create().warning().header("Wspierane formaty to gif i png!").showAndWait()
                return@setOnAction close()
            }

            val image = ImageIO.read(iis)
            var fileBytes: ByteArray? = null

            FileInputStream(result).use {
                fileBytes = IOUtils.toByteArray(it)
            }

            val icon = MRFIcon(fileBytes!!, format, image)
            val resource = GraphicResource(this.fieldGraphicId.text, icon)

            val serialized = GraphicsManagerController.graphicSerializer.serialize(resource)
            val editor = MargoJEditor.INSTANCE
            editor.currentResourceBundle!!.saveResource(resource, ByteArrayInputStream(serialized))
            editor.updateResourceView()
            this.graphicsManagerController.reloadGraphics()
            close()
        }
    }

    private fun close()
    {
        scene.stage.close()
    }
}
