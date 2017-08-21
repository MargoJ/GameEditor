package pl.margoj.editor.gui.controllers.dialog.map

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.map.actions.MapObjectUndoRedo
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.npc.NpcMapObject
import java.net.URL
import java.util.ArrayList
import java.util.ResourceBundle

class NpcObjectDialogController : CustomController
{
    @FXML
    lateinit var fieldNpcId: TextField

    @FXML
    lateinit var fieldNpcGraphics: TextField

    @FXML
    lateinit var fieldNpcName: TextField

    @FXML
    lateinit var fieldNpcLevel: TextField

    @FXML
    lateinit var buttonNpcConirm: Button

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

        val current = this.map.getObject(this.position) as? NpcMapObject

        if (current != null)
        {
            this.fieldNpcId.text = current.script
            this.fieldNpcGraphics.text = current.graphics ?: ""
            this.fieldNpcName.text = current.name ?: ""
            this.fieldNpcLevel.text = current.level?.toString() ?: ""
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldNpcLevel, false, true)

        this.buttonNpcConirm.onAction = EventHandler {
            val errors = ArrayList<String>()
            val id = this.fieldNpcId.text
            val graphics = if(this.fieldNpcGraphics.text.isNotEmpty()) this.fieldNpcGraphics.text else null
            val name = if(this.fieldNpcName.text.isNotEmpty()) this.fieldNpcName.text else null
            val level = if(this.fieldNpcLevel.text.isNotEmpty()) this.fieldNpcLevel.text.toInt() else null

            if (id.isEmpty() || !MargoResource.ID_PATTERN.matcher(id).matches())
            {
                errors.add("ID npc nie jest poprawne")
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas edytowania NPC", errors)
                return@EventHandler
            }

            val old = this.map.getObject(position)
            val new = NpcMapObject(this.position, id, graphics, name, level)

            if(new != old)
            {
                val mapEditor = MargoJEditor.INSTANCE.mapEditor
                mapEditor.currentMap!!.addObject(new)

                if (old != null)
                {
                    mapEditor.redrawObject(old)
                }

                mapEditor.redrawObject(new)

                mapEditor.addUndoAction(MapObjectUndoRedo(this.position, old, new, "Dodaj/edytuj npc"))
            }

            this.scene.stage.close()
        }
    }
}