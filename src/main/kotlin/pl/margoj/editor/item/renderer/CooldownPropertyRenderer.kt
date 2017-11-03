package pl.margoj.editor.item.renderer

import javafx.scene.Node
import javafx.scene.control.TextField
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.item.properties.special.CooldownProperty

class CooldownPropertyRenderer : ItemPropertyRenderer<CooldownProperty.Cooldown, CooldownProperty, TextField>()
{
    private val changeCache = hashMapOf<Node, String>()

    override val propertyType: Class<CooldownProperty> = CooldownProperty::class.java

    override fun createNode(property: CooldownProperty): TextField
    {
        val textField = TextField()
        FXUtils.makeNumberField(textField, true)
        textField.text = property.default.toString()

        addPropertyRenderer(textField, changeCache, property, {
            try
            {
                CooldownProperty.Cooldown(it.toInt(), 0L)
            }
            catch (e: NumberFormatException)
            {
                null
            }
        }, { it.cooldown.toString() })

        return textField
    }

    override fun update(property: CooldownProperty, node: TextField, value: CooldownProperty.Cooldown)
    {
        node.text = value.cooldown.toString()
        changeCache[node] = node.text
    }

    override fun convert(property: CooldownProperty, node: TextField): CooldownProperty.Cooldown?
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

        return CooldownProperty.Cooldown(intValue, 0L)
    }
}