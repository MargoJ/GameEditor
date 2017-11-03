package pl.margoj.editor.item

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
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

    lateinit var nodes: Map<String, Collection<Node>>
        private set

    lateinit var actualNodes: Map<ItemProperty<*>, Node>
        private set

    fun calculate()
    {
        logger.trace("calculate()")
        logger.debug("available renderers: ${this.renderers}")

        val nodes = LinkedHashMap<String, MutableList<Node>>()
        val actualNodes = hashMapOf<ItemProperty<*>, Node>()

        for (property in TreeSet<ItemProperty<*>>(ItemProperty.properties))
        {
            if (!property.editable)
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

                nodes.computeIfAbsent(property.category ?: "Inne", { ArrayList() }).add(loader.node)
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
                ProfessionRequirementPropertyRenderer(),
                CooldownPropertyRenderer()
        )
    }

    fun render(container: VBox, search: String)
    {
        logger.trace("render(container = $container, search = $search)")
        val items = arrayListOf<Node>()

        for ((category, children) in this.nodes)
        {
            val title = Label(category)
            title.setMaxSize(Double.MAX_VALUE, 30.0)
            title.setPrefSize(Double.MAX_VALUE, 30.0)
            title.setMinSize(0.0, 30.0)
            title.style += "; -fx-border-color: black; -fx-border-style: hidden hidden solid hidden; -fx-background-color: #D3D3D3;"
            title.alignment = Pos.CENTER
            title.font = Font.font("System", FontWeight.BOLD, 15.0)

            items.add(title)

            for (child in children)
            {
                child as HBox
                val text = (child.children[0] as Label).text

                if (StringUtils.containsIgnoreCase(text, search))
                {
                    items.add(child)
                }
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