package pl.margoj.editor.item.renderer

import javafx.scene.Node
import javafx.scene.control.ComboBox
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.item.PropertyChangeUndoRedo
import pl.margoj.mrf.item.ItemProperty
import java.util.Collections
import java.util.WeakHashMap

abstract class ListPropertyRenderer<O, P : ItemProperty<O>> : ItemPropertyRenderer<O, P, ComboBox<String>>()
{
    private val programaticllyChangedNodes = Collections.newSetFromMap(WeakHashMap<Node, Boolean>())

    abstract fun getAllValues(): Array<O>

    abstract fun getStringRepresentation(value: O): String

    override fun createNode(property: P): ComboBox<String>
    {
        val box = ComboBox<String>()
        box.maxWidth = Double.POSITIVE_INFINITY

        for (value in this.getAllValues())
        {
            box.items.add(this.getStringRepresentation(value))
        }

        box.selectionModel.select(0)

        box.selectionModel.selectedItemProperty().addListener { _, oldValue, newValue ->
            if (!this.programaticllyChangedNodes.contains(box))
            {
                val itemEditor = MargoJEditor.INSTANCE.itemEditor
                val newValue = this.convertFromString(newValue)!!
                itemEditor.currentItem!![property] = newValue
                itemEditor.addUndoAction(PropertyChangeUndoRedo(property, this.convertFromString(oldValue)!!, newValue))
            }
        }

        return box
    }

    override fun update(property: P, node: ComboBox<String>, value: O)
    {
        this.programaticllyChangedNodes.add(node)
        node.selectionModel.select(this.getStringRepresentation(value))
        this.programaticllyChangedNodes.remove(node)
    }

    override fun convert(property: P, node: ComboBox<String>): O?
    {
        return convertFromString(node.selectionModel.selectedItem)
    }

    fun convertFromString(string: String): O?
    {
        return this.getAllValues().find { this.getStringRepresentation(it) == string }
    }
}