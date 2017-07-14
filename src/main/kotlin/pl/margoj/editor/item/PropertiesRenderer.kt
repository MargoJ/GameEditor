package pl.margoj.editor.item

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.apache.commons.lang3.StringUtils
import pl.margoj.editor.gui.controllers.ItemPropertyController
import pl.margoj.editor.gui.utils.FXMLJarLoader
import pl.margoj.editor.item.renderer.*
import pl.margoj.mrf.item.ItemProperty
import java.util.TreeSet

class PropertiesRenderer(val renderers: Collection<ItemPropertyRenderer<*, *, *>>)
{
    lateinit var nodes: Collection<Node>
        private set

    fun calculate()
    {
        val nodes = mutableListOf<Node>()

        for (property in TreeSet<ItemProperty<*>>(ItemProperty.properties))
        {
            @Suppress("UNCHECKED_CAST")
            val availableRenderer: ItemPropertyRenderer<*, ItemProperty<*>, *>? =
                    this.renderers.lastOrNull { it.getPropertyType().isAssignableFrom(property.javaClass) } as? ItemPropertyRenderer<*, ItemProperty<*>, *>

            if (availableRenderer != null)
            {
                val loader = FXMLJarLoader("parts/item_property")
                loader.load()

                val controller = loader.controller as ItemPropertyController
                controller.propLabelName.text = property.name
                controller.propPaneValueHolder.children.add(availableRenderer.createNode(property))

                nodes.add(loader.node)
            }
        }

        this.nodes = nodes
    }

    companion object
    {
        val DEFAULT_PROPERTIES_RENDERERS = mutableListOf<ItemPropertyRenderer<*, *, *>>(
                StringPropertyRenderer(),
                IntPropertyRenderer(),
                BooleanPropertyRenderer(),
                CategoryPropertyRenderer(),
                RarityPropertyRenderer()
        )
    }

    fun render(container: VBox, search: String)
    {
        val items = arrayListOf<Node>()

        for (child in this.nodes)
        {
            child as HBox
            val text = (child.children[0] as Label).text

            if (StringUtils.containsIgnoreCase(text, search))
            {
                items.add(child)
            }
        }

        container.children.setAll(items)
    }
}