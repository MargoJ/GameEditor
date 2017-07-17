package pl.margoj.editor.item.renderer

import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.item.PropertyChangeUndoRedo
import pl.margoj.mrf.item.ItemProperty
import pl.margoj.mrf.item.properties.StringProperty

fun <T> addPropertyRenderer(textField: TextInputControl, changeCache: HashMap<Node, String>, property: ItemProperty<T>, fromString: (String) -> T?, toString: (T) -> String)
{
    textField.textProperty().addListener { _, oldValue, _ ->
        if (changeCache.containsKey(textField))
        {
            return@addListener
        }

        changeCache.put(textField, oldValue)
    }

    textField.focusedProperty().addListener { _, _, focused ->
        if (focused)
        {
            return@addListener
        }

        val oldValueString = changeCache[textField]
        val oldValue = if (oldValueString == null) property.default else fromString(oldValueString)
        val newValue = fromString(textField.text)

        if(newValue == null)
        {
            textField.text = toString(property.default)
            return@addListener
        }

        if (oldValue == newValue)
        {
            return@addListener
        }

        val editor = MargoJEditor.INSTANCE.itemEditor
        editor.currentItem!![property] = newValue
        editor.addUndoAction(PropertyChangeUndoRedo(property, oldValue!!, newValue))
        changeCache.remove(textField)
    }

}

class StringPropertyRenderer : ItemPropertyRenderer<String, StringProperty, TextInputControl>()
{
    private val changeCache = hashMapOf<Node, String>()

    override val propertyType: Class<StringProperty> = StringProperty::class.java

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

        addPropertyRenderer(control, changeCache, property, { it }, { it })

        return control
    }

    override fun update(property: StringProperty, node: TextInputControl, value: String)
    {
        node.text = value
        changeCache[node] = node.text
    }

    override fun convert(property: StringProperty, node: TextInputControl): String?
    {
        val text = node.text
        if (property.regexp == null)
        {
            return text
        }

        if (!property.regexp!!.matches(text))
        {
            error("Wartość dla '$${property.name}' ($text) nie spełnia wymogów (${property.regexp.toString()})")
            return null
        }

        return text
    }
}