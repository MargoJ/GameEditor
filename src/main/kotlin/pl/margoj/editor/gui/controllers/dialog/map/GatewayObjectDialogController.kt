package pl.margoj.editor.gui.controllers.dialog.map

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.map.actions.MapObjectUndoRedo
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import java.net.URL
import java.util.ArrayList
import java.util.ResourceBundle

class GatewayObjectDialogController : CustomController
{
    @FXML
    lateinit var fieldGatewayTargetX: TextField

    @FXML
    lateinit var fieldGatewayTargetY: TextField

    @FXML
    lateinit var fieldGatewayTargetName: TextField

    @FXML
    lateinit var toggleKeyNeeded: ToggleButton

    @FXML
    lateinit var fieldGatewayKeyName: TextField

    @FXML
    lateinit var toggleLevelRestriction: ToggleButton

    @FXML
    lateinit var fieldGatewayLevelMin: TextField

    @FXML
    lateinit var fieldGatewayLevelMax: TextField

    @FXML
    lateinit var buttonGatewayConfirm: Button

    lateinit var scene: CustomScene<*>

    lateinit var map: MargoMap

    lateinit var position: Point

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any)
    {
        data as Pair<MargoMap, Point>

        this.map = data.first
        this.position = data.second

        val current = this.map.getObject(this.position) as? GatewayObject

        if (current != null)
        {
            this.fieldGatewayTargetX.text = current.target.x.toString()
            this.fieldGatewayTargetY.text = current.target.y.toString()
            this.fieldGatewayTargetName.text = current.targetMap

            this.toggleKeyNeeded.isSelected = current.keyId != null
            this.fieldGatewayKeyName.text = current.keyId ?: ""

            this.toggleLevelRestriction.isSelected = current.levelRestriction.enabled
            this.fieldGatewayLevelMin.text = if(current.levelRestriction.enabled) current.levelRestriction.minLevel.toString() else ""
            this.fieldGatewayLevelMax.text = if(current.levelRestriction.enabled) current.levelRestriction.maxLevel.toString() else ""
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldGatewayTargetX, false)
        FXUtils.makeNumberField(this.fieldGatewayTargetY, false)
        FXUtils.makeNumberField(this.fieldGatewayLevelMin, false, true)
        FXUtils.makeNumberField(this.fieldGatewayLevelMax, false, true)

        this.fieldGatewayLevelMin.disableProperty().bind(Bindings.`when`(this.toggleLevelRestriction.selectedProperty()).then(false).otherwise(true))
        this.fieldGatewayLevelMax.disableProperty().bind(Bindings.`when`(this.toggleLevelRestriction.selectedProperty()).then(false).otherwise(true))
        this.toggleKeyNeeded.textProperty().bind(Bindings.`when`(this.toggleKeyNeeded.selectedProperty()).then("Tak").otherwise("Nie"))
        this.toggleLevelRestriction.textProperty().bind(Bindings.`when`(this.toggleLevelRestriction.selectedProperty()).then("Właczone").otherwise("Wyłączone"))

        this.buttonGatewayConfirm.onAction = EventHandler {
            val errors = ArrayList<String>()
            val id = this.fieldGatewayTargetName.text
            val x = this.fieldGatewayTargetX.text.toInt()
            val y = this.fieldGatewayTargetY.text.toInt()

            if (!MargoResource.ID_PATTERN.matcher(id).matches())
            {
                errors.add("ID mapy nie jest poprawne")
            }

            if (x > 127 || y > 127)
            {
                errors.add("Koordynaty X i Y nie mogą przekraczać 127")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas edytowania przejścia", errors)
                return@EventHandler
            }

            val old = this.map.getObject(position)
            val new = GatewayObject(
                    position = this.position,
                    target = Point(x, y),
                    targetMap = id,
                    levelRestriction = GatewayObject.LevelRestriction(
                            enabled = this.toggleLevelRestriction.isSelected,
                            minLevel = this.fieldGatewayLevelMin.text.toIntOrNull() ?: 0,
                            maxLevel = this.fieldGatewayLevelMax.text.toIntOrNull() ?: 0
                    ), keyId = if (this.toggleKeyNeeded.isSelected) this.fieldGatewayKeyName.text else null
            )

            if (new != old)
            {
                val mapEditor = MargoJEditor.INSTANCE.mapEditor
                mapEditor.currentMap!!.addObject(new)

                if (old != null)
                {
                    mapEditor.redrawObject(old)
                }
                mapEditor.redrawObject(new)

                mapEditor.addUndoAction(MapObjectUndoRedo(this.position, old, new, "Dodaj/edytuj przejście"))
            }

            this.scene.stage.close()
        }
    }
}