package pl.margoj.editor.item.renderer

import javafx.beans.binding.When
import javafx.event.EventHandler
import javafx.scene.control.ToggleButton
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.item.PropertyChangeUndoRedo
import pl.margoj.mrf.item.properties.BooleanProperty

class BooleanPropertyRenderer : ItemPropertyRenderer<Boolean, BooleanProperty, ToggleButton>()
{
    override val propertyType: Class<BooleanProperty> = BooleanProperty::class.java

    override fun createNode(property: BooleanProperty): ToggleButton
    {
        val button = ToggleButton()
        button.maxWidth = Double.POSITIVE_INFINITY
        button.textProperty().bind(When(button.selectedProperty()).then("Włączone").otherwise("Wyłączone"))
        button.onAction = EventHandler {
            val itemEditor = MargoJEditor.INSTANCE.itemEditor
            itemEditor.currentItem!![property] = button.isSelected
            itemEditor.addUndoAction(PropertyChangeUndoRedo(property, !button.isSelected, button.isSelected))
        }
        button.isFocusTraversable = false
        return button
    }

    override fun update(property: BooleanProperty, node: ToggleButton, value: Boolean)
    {
        node.isSelected = value
    }

    override fun convert(property: BooleanProperty, node: ToggleButton): Boolean?
    {
        return node.isSelected
    }
}