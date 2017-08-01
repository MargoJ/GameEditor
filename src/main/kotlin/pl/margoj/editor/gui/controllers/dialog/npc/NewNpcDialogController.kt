package pl.margoj.editor.gui.controllers.dialog.npc

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.npc.NpcScript
import java.net.URL
import java.util.ArrayList
import java.util.ResourceBundle

class NewNpcDialogController : CustomController
{
    private lateinit var scene: CustomScene<*>

    @FXML
    lateinit var fieldNpcId: TextField

    @FXML
    lateinit var buttonNpcConfirm: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun initialize(location: URL, resources: ResourceBundle?)
    {
        buttonNpcConfirm.setOnAction {
            val errors = ArrayList<String>()

            if (this.fieldNpcId.text.isEmpty())
            {
                errors.add("ID skryptu nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldNpcId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldNpcId.text.length > 127)
            {
                errors.add("ID skryptu nie moze przekraczac 127 znakow")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas tworzenia nowego skryptu", errors)
                return@setOnAction
            }

            val script = NpcScript(this.fieldNpcId.text)
            val editor = MargoJEditor.INSTANCE.npcEditor
            editor.currentScript = script
            editor.saveFile = null
            editor.touch()

            scene.stage.close()
        }
    }
}
