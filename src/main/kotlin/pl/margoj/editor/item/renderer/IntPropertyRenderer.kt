package pl.margoj.editor.item.renderer

import javafx.scene.Node
import javafx.scene.control.TextField
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.item.properties.IntProperty

class IntPropertyRenderer : ItemPropertyRenderer<Int, IntProperty, TextField>()
{
    private val changeCache = hashMapOf<Node, String>()

    override val propertyType: Class<IntProperty> = IntProperty::class.java

    override fun createNode(property: IntProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()

        addPropertyRenderer(textField, changeCache, property, {
            try
            {
                it.toInt()
            }
            catch (e: NumberFormatException)
            {
                null
            }
        }, { it.toString() })

        return textField
    }

    override fun update(property: IntProperty, node: TextField, value: Int)
    {
        node.text = value.toString()
        changeCache[node] = node.text
    }

    override fun convert(property: IntProperty, node: TextField): Int?
    {
        val intValue: Int
        try
        {
            intValue = node.text.toInt()
        }
        catch (e: NumberFormatException)
        {
            error("Wartość dla '${property.name}' (${node.text}) nie jest liczbą całkowitą")
            return null
        }

        if (intValue < property.minimum)
        {
            error("Wartość dla '${property.name}' ($intValue) jest mniejsza od wartośći minimalnej (${property.minimum})")
            return null
        }

        return intValue
    }
}