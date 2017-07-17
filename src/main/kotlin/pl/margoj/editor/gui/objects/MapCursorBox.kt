package pl.margoj.editor.gui.objects

import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.gui.listener.LayerListener
import pl.margoj.editor.gui.utils.IconUtils
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.map.cursor.Cursor
import java.util.Collections
import java.util.LinkedHashMap

class MapCursorBox(private val container: HBox, private val editor: MapEditor)
{
    private val logger = LogManager.getLogger(LayerListener::class.java)
    private val toggleGroup = ToggleGroup()
    private val cursors = LinkedHashMap<Cursor, RadioButton>()

    fun addButton(cursor: Cursor, iconName: String, tooltip: String): Boolean
    {
        logger.trace("addButton(cursor = $cursor, iconName = $iconName, tooltip = $tooltip)")
        if (this.cursors.containsKey(cursor))
        {
            logger.warn("Duplicate cursor button for $cursor")
            return false
        }

        val button = RadioButton()
        button.toggleGroup = this.toggleGroup
        IconUtils.removeDefaultClass(button, "radio-button")
        IconUtils.createBinding(button.graphicProperty(), button.selectedProperty(), "cursor/" + iconName)
        button.tooltip = Tooltip(tooltip)

        button.selectedProperty().addListener { _, _, new ->
            if (!new)
            {
                return@addListener
            }
            editor.cursor = cursor
        }

        if (cursor === Cursor.DEFAULT)
        {
            button.isSelected = true
        }

        this.container.children.add(button)
        this.cursors.put(cursor, button)

        return true
    }

    fun changeSelection(cursor: Cursor)
    {
        logger.trace("changeSelection($cursor)")
        val button = this.cursors[cursor]
        if (button != null)
        {
            this.toggleGroup.selectToggle(button)
        }
    }

    fun getCursors(): Collection<Cursor>
    {
        return Collections.unmodifiableCollection(this.cursors.keys)
    }
}
