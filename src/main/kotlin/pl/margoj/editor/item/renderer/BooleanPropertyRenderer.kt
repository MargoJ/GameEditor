package pl.margoj.editor.item.renderer

import javafx.beans.binding.When
import javafx.scene.control.ToggleButton
import pl.margoj.mrf.item.properties.BooleanProperty

class BooleanPropertyRenderer : ItemPropertyRenderer<Boolean, BooleanProperty, ToggleButton>()
{
    override fun getPropertyType(): Class<BooleanProperty>
    {
        return BooleanProperty::class.java
    }

    override fun createNode(property: BooleanProperty): ToggleButton
    {
        val button = ToggleButton()
        button.maxWidth = Double.POSITIVE_INFINITY
        button.textProperty().bind(When(button.selectedProperty()).then("Włączone").otherwise("Wyłączone"))
        button.isFocusTraversable = false
        return button
    }

    override fun update(property: BooleanProperty, node: ToggleButton, value: Boolean)
    {
        node.isSelected = value
    }

    override fun validate(property: BooleanProperty, name: String, string: String): Boolean
    {
        return true
    }
}