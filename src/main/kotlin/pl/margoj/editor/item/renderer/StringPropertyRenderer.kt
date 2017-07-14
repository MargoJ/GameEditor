package pl.margoj.editor.item.renderer

import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import pl.margoj.mrf.item.properties.StringProperty

class StringPropertyRenderer : ItemPropertyRenderer<String, StringProperty, TextInputControl>()
{
    override fun getPropertyType(): Class<StringProperty> = StringProperty::class.java

    override fun createNode(property: StringProperty): TextInputControl
    {
        val control: TextInputControl

        if (property.long)
        {
            control = TextArea()
            control.prefHeight = 100.0
            control.minHeight = 100.0
        }
        else
        {
            control = TextField()
        }

        control.text = property.default

        return control
    }

    override fun update(property: StringProperty, node: TextInputControl, value: String)
    {
        node.text = value
    }

    override fun validate(property: StringProperty, name: String, string: String): Boolean
    {
        if (property.regexp == null)
        {
            return true
        }

        if (!property.regexp!!.matches(name))
        {
            error("Wartość dla '$name' ($string) nie spełnia wymogów (${property.regexp.toString()})")
            return false
        }

        return true
    }
}