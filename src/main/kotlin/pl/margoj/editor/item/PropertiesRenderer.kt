package pl.margoj.editor.item

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.gui.controllers.ItemPropertyController
import pl.margoj.editor.gui.utils.FXMLJarLoader
import pl.margoj.editor.item.renderer.*
import pl.margoj.mrf.item.ItemProperty
import java.util.TreeSet

class PropertiesRenderer(val renderers: Collection<ItemPropertyRenderer<*, *, *>>)
{
    private val logger = LogManager.getLogger(PropertiesRenderer::class.java)

    lateinit var nodes: Collection<Node>
        private set

    lateinit var actualNodes: Map<ItemProperty<*>, Node>
        private set

    fun calculate()
    {
        logger.trace("calculate()")
        logger.debug("available renderers: ${this.renderers}")

        val nodes = mutableListOf<Node>()
        val actualNodes = hashMapOf<ItemProperty<*>, Node>()

        for (property in TreeSet<ItemProperty<*>>(ItemProperty.properties))
        {
            if(!property.editable)
            {
                continue
            }

            logger.debug("=========== Creating new property")
            logger.debug("property = $property")
            val availableRenderer = this.getRendererOf(property)
            logger.debug("availableRenderer = $availableRenderer")

            if (availableRenderer != null)
            {
                val loader = FXMLJarLoader("parts/item_property")
                loader.load()

                val controller = loader.controller as ItemPropertyController
                controller.propLabelName.text = property.name

                val actualNode = availableRenderer.createNode(property)
                actualNodes.put(property, actualNode)
                controller.propPaneValueHolder.children.add(actualNode)

                nodes.add(loader.node)
            }
        }

        this.nodes = nodes
        this.actualNodes = actualNodes
    }

    companion object
    {
        val DEFAULT_PROPERTIES_RENDERERS = mutableListOf<ItemPropertyRenderer<*, *, *>>(
                StringPropertyRenderer(),
                IntPropertyRenderer(),
                IntRangePropertyRenderer(),
                DoublePropertyRenderer(),
                LongPropertyRenderer(),
                BooleanPropertyRenderer(),
                CategoryPropertyRenderer(),
                RarityPropertyRenderer(),
                IconPropertyRenderer(),
                ProfessionRequirementPropertyRenderer()
        )
    }

    fun render(container: VBox, search: String)
    {
        logger.trace("render(container = $container, search = $search)")
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

    @Suppress("UNCHECKED_CAST")
    fun getRendererOf(property: ItemProperty<*>): ItemPropertyRenderer<*, ItemProperty<*>, *>?
    {
        return this.renderers.lastOrNull { it.propertyType.isAssignableFrom(property.javaClass) } as? ItemPropertyRenderer<*, ItemProperty<*>, *>
    }
}