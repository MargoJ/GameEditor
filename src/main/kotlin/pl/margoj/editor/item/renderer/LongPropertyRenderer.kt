package pl.margoj.editor.item.renderer

import javafx.scene.Node
import javafx.scene.control.TextField
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.item.properties.LongProperty

class LongPropertyRenderer : ItemPropertyRenderer<Long, LongProperty, TextField>()
{
    private val changeCache = hashMapOf<Node, String>()

    override val propertyType: Class<LongProperty> = LongProperty::class.java

    override fun createNode(property: LongProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()

        addPropertyRenderer(textField, changeCache, property, {
            try
            {
                it.toLong()
            }
            catch (e: NumberFormatException)
            {
                null
            }
        }, { it.toString() })

        return textField
    }

    override fun update(property: LongProperty, node: TextField, value: Long)
    {
        node.text = value.toString()
        changeCache[node] = node.text
    }

    override fun convert(property: LongProperty, node: TextField): Long?
    {
        val longValue: Long
        try
        {
            longValue = node.text.toLong()
        }
        catch(e: NumberFormatException)
        {
            error("Wartość dla '${property.name}' (${node.text}) nie jest liczbą całkowitą")
            return null
        }

        if (longValue < property.minimum)
        {
            error("Wartość dla '${property.name}' ($longValue) jest mniejsza od wartośći minimalnej (${property.minimum})")
            return null
        }

        return longValue
    }
}