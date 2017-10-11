package pl.margoj.editor.gui.controllers.dialog.map

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.map.actions.MapObjectUndoRedo
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.text.TextMapObject
import java.net.URL
import java.util.ResourceBundle

class TextObjectDialogController : CustomController
{
    @FXML
    lateinit var fieldText: TextField

    @FXML
    lateinit var buttonTextConfirm: Button

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

        val current = this.map.getObject(this.position) as? TextMapObject

        if (current != null)
        {
            this.fieldText.text = current.text ?: ""
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.buttonTextConfirm.onAction = EventHandler {
            val text = this.fieldText.text

            val old = this.map.getObject(position)
            val new = TextMapObject(this.position, text)

            if (new != old)
            {
                val mapEditor = MargoJEditor.INSTANCE.mapEditor
                mapEditor.currentMap!!.addObject(new)

                if (old != null)
                {
                    mapEditor.redrawObject(old)
                }

                mapEditor.redrawObject(new)

                mapEditor.addUndoAction(MapObjectUndoRedo(this.position, old, new, "Dodaj/edytuj tekst"))
            }

            this.scene.stage.close()
        }
    }
}