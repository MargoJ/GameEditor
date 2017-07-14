package pl.margoj.editor.item.renderer

import javafx.scene.control.TextField
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.item.properties.IntProperty

class IntPropertyRenderer : ItemPropertyRenderer<Int, IntProperty, TextField>()
{
    override fun getPropertyType(): Class<IntProperty> = IntProperty::class.java

    override fun createNode(property: IntProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()
        return textField
    }

    override fun update(property: IntProperty, node: TextField, value: Int)
    {
        node.text = value.toString()
    }

    override fun validate(property: IntProperty, name: String, string: String): Boolean
    {
        val intValue: Int
        try
        {
            intValue = string.toInt()
        }
        catch(e: NumberFormatException)
        {
            error("Wartość dla '$name' ($string) nie jest liczbą całkowitą")
            return false
        }

        if (intValue < property.minimum)
        {
            error("Wartość dla '$name' ($intValue) jest mniejsza od wartośći minimalnej (${property.minimum})")
            return false
        }

        return true
    }
}