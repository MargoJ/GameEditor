package pl.margoj.editor.gui.controllers.dialog.item

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.item.MargoItem
import java.net.URL
import java.util.ArrayList
import java.util.ResourceBundle

class NewItemDialogController : CustomController
{
    private lateinit var scene: CustomScene<*>

    @FXML
    lateinit var fieldItemId: TextField

    @FXML
    lateinit var buttonItemConfirm: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun initialize(location: URL, resources: ResourceBundle?)
    {
        buttonItemConfirm.setOnAction {
            val errors = ArrayList<String>()

            if (this.fieldItemId.text.isEmpty())
            {
                errors.add("ID mapy nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldItemId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldItemId.text.length > 127)
            {
                errors.add("ID mapy nie moze przekraczac 127 znakow")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas tworzenia nowej mapy", errors)
                return@setOnAction
            }

            val item = MargoItem(this.fieldItemId.text, "")
            val editor = MargoJEditor.INSTANCE.itemEditor
            editor.currentItem = item
            editor.saveFile = null
            editor.touch()

            scene.stage.close()
        }
    }
}
