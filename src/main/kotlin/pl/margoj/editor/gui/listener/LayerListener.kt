package pl.margoj.editor.gui.listener

import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCharacterCombination
import javafx.scene.input.ScrollEvent
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.gui.utils.IconUtils
import pl.margoj.editor.map.MapEditor
import pl.margoj.mrf.map.MargoMap

class LayerListener(private val editor: MapEditor, vararg val buttons: RadioButton) : EventHandler<Event>
{
    private val logger = LogManager.getLogger(LayerListener::class.java)

    fun autoRegister(scene: Scene)
    {
        logger.trace("autoRegister($scene)")
        val group = ToggleGroup()

        for (i in buttons.indices)
        {
            val button = buttons[i]
            button.toggleGroup = group
            button.addEventHandler(ActionEvent.ACTION, this)
            IconUtils.removeDefaultClass(button, "radio-button")

            when (i)
            {
                MargoMap.COLLISION_LAYER ->
                {
                    scene.accelerators.put(KeyCharacterCombination("Q"), Runnable { button.fire() })
                    IconUtils.createBinding(button.graphicProperty(), button.selectedProperty(), "layer_c")
                    IconUtils.addTooltip(button, "Warstwa kolizji")
                }
                MargoMap.OBJECT_LAYER ->
                {
                    scene.accelerators.put(KeyCharacterCombination("W"), Runnable { button.fire() })
                    IconUtils.createBinding(button.graphicProperty(), button.selectedProperty(), "layer_o")
                    IconUtils.addTooltip(button, "Warstwa obiektów")
                }
                else ->
                {
                    scene.accelerators.put(KeyCharacterCombination(if (i == 9) "0" else Integer.toString(i + 1)), Runnable { button.fire() })
                    IconUtils.createBinding(button.graphicProperty(), button.selectedProperty(), "layer_" + (i + 1))
                    IconUtils.addTooltip(button, "Warstwa " + (i + 1))
                }
            }
        }

        buttons[0].fire()

        editor.objectCanvas!!.addEventFilter(ScrollEvent.ANY, this)
    }

    override fun handle(event: Event)
    {
        if (event is ActionEvent)
        {
            for (i in buttons.indices)
            {
                if (this.buttons[i] === event.getSource())
                {
                    this.editor.currentLayer = i
                    break
                }
            }
        }
        else if (event is ScrollEvent)
        {
            val change = if (event.deltaY.toInt() > 0) -1 else 1
            var newLayer = this.editor.currentLayer + change

            if (newLayer < 0)
            {
                newLayer = MargoMap.OBJECT_LAYER
            }
            else if (newLayer > MargoMap.OBJECT_LAYER)
            {
                newLayer = 0
            }
            buttons[newLayer].fire()
            event.consume()
        }
    }
}
