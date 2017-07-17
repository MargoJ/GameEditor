package pl.margoj.editor.gui.controllers.dialog.map

import java.net.URL
import java.util.ArrayList
import java.util.ResourceBundle

import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.utils.FXUtils

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.editor.map.actions.WholeMapReplacement

class EditMapDialogController : CustomController
{
    private lateinit var scene: CustomScene<*>

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

        val editor = MargoJEditor.INSTANCE.mapEditor
        val map = editor.currentMap!!
        this.fieldMapName.text = map.name

        this.fieldMapWidth.text = Integer.toString(map.width)
        this.fieldMapHeight.text = Integer.toString(map.height)

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

            if (width > 128 || height > 128)
            {
                errors.add("Wysokość i szerokość nie mogą przekraczać 128")
            }

            if (this.fieldMapName.text.length > 127)
            {
                errors.add("Nazwa mapy nie moze przekraczac 127 znakow")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas zmiany rozmiaru mapy", errors)
                return@setOnAction
            }

            val oldFragments = map.fragments
            editor.resizeMap(width, height)

            val undoAction = WholeMapReplacement(
                    oldFragments,
                    map.fragments,
                    map.name,
                    this.fieldMapName.text
            )

            map.name = this.fieldMapName.text
            editor.updateMapInfo()
            editor.addUndoAction(undoAction)

            scene.stage.close()
        }
    }
}
