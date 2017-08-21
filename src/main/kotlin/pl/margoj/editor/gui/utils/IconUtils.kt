package pl.margoj.editor.gui.utils

import javax.imageio.ImageIO

import java.awt.image.BufferedImage
import java.io.IOException
import java.util.HashMap

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.EditorApplication

@Suppress("LoopToCallChain")
object IconUtils
{
    private val logger = LogManager.getLogger(IconUtils::javaClass)
    private val iconCache = HashMap<String, ImageView>()

    fun createBinding(graphicProperty: ObjectProperty<Node>, selectedProperty: BooleanProperty, layer: String)
    {
        logger.trace("createBinding(graphicProperty = $graphicProperty, selectedProperty = $selectedProperty, layer = $layer)")
        graphicProperty.value = getIcon(layer, selectedProperty.value!!)
        selectedProperty.addListener { _, _, newValue -> graphicProperty.setValue(getIcon(layer, newValue!!)) }
    }

    fun removeDefaultClass(node: Node, defaultClass: String)
    {
        logger.trace("removeDefaultClass(node = $node, selectedProperty = $defaultClass)")

        node.styleClass.remove(defaultClass)
    }

    fun addTooltip(element: Control, text: String)
    {
        element.tooltip = Tooltip(text)
    }

    fun getIcon(layer: String, selected: Boolean, rgb: Int = 0xFF66FFFF.toInt()): ImageView
    {
        val cacheKey = layer + if (selected) "_selected" else ""

        val test = iconCache[cacheKey]
        if (test != null)
        {
            return test
        }

        val image: BufferedImage
        try
        {
            image = ImageIO.read(EditorApplication::class.java.classLoader.getResourceAsStream("icons/$layer.png"))
        }
        catch (e: IOException)
        {
            throw RuntimeException("Couldn't get icon: " + cacheKey)
        }

        if (selected)
        {
            for (x in 0 until image.width)
            {
                for (y in 0 until image.height)
                {
                    if (image.getRGB(x, y) == 0xFFFFFFFF.toInt())
                    {
                        image.setRGB(x, y, rgb)
                    }
                }
            }
        }

        val out = ImageView(SwingFXUtils.toFXImage(image, null))
        iconCache.put(cacheKey, out)
        return out
    }
}
