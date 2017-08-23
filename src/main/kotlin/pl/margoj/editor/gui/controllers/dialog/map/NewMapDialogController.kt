package pl.margoj.editor.gui.controllers.dialog.map


import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.serialization.MapSerializer
import java.net.URL
import java.util.ArrayList
import java.util.ResourceBundle

class NewMapDialogController : CustomController
{
    private lateinit var scene: CustomScene<*>

    @FXML
    lateinit var fieldMapId: TextField

    @FXML
    lateinit var fieldMapName: TextField

    @FXML
    lateinit var fieldMapWidth: TextField

    @FXML
    lateinit var fieldMapHeight: TextField

    @FXML
    lateinit var buttonMapConfirm: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun initialize(location: URL, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldMapWidth, false)
        FXUtils.makeNumberField(this.fieldMapHeight, false)

        buttonMapConfirm.setOnAction {
            val errors = ArrayList<String>()

            var width: Int
            var height: Int

            try
            {
                width = Integer.parseInt(this.fieldMapWidth.text)
                height = Integer.parseInt(this.fieldMapHeight.text)
            }
            catch (e: NumberFormatException)
            {
                width = Integer.MAX_VALUE
                height = Integer.MAX_VALUE
            }

            if (width < 16 || height < 16)
            {
                errors.add("Wysokość i szerokosc nie moga byc mniejsze od 16")
            }

            if (width > 128 || height > 128)
            {
                errors.add("Wysokość i szerokość nie mogą przekraczać 128")
            }

            if (this.fieldMapName.text.length > 127)
            {
                errors.add("Nazwa mapy nie moze przekraczac 127 znakow")
            }

            if (this.fieldMapId.text.isEmpty())
            {
                errors.add("ID mapy nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldMapId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldMapId.text.length > 127)
            {
                errors.add("ID mapy nie moze przekraczac 127 znakow")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas tworzenia nowej mapy", errors)
                return@setOnAction
            }

            val map = MargoMap(MapSerializer.CURRENT_VERSION.toByte(), fieldMapId.text, fieldMapName.text, width, height)
            val editor = MargoJEditor.INSTANCE.mapEditor
            editor.currentMap = map
            editor.saveFile = null
            editor.touch()

            scene.stage.close()
        }
    }
}
