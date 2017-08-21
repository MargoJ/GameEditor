package pl.margoj.editor.item.renderer

import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import org.apache.commons.io.IOUtils
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.editor.item.PropertyChangeUndoRedo
import pl.margoj.mrf.MRFIcon
import pl.margoj.mrf.MRFIconFormat
import pl.margoj.mrf.item.properties.IconProperty
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.util.WeakHashMap
import javax.imageio.ImageIO

class IconPropertyRenderer : ItemPropertyRenderer<MRFIcon?, IconProperty, HBox>()
{
    private val fileChooser: FileChooser = FileChooser()
    private val cache = WeakHashMap<HBox, MRFIcon>()

    init
    {
        fileChooser.extensionFilters.setAll(FileChooser.ExtensionFilter("Wspierane pliki graficzne (.png, .gif)", "*.png", "*.gif"))
    }

    override val propertyType: Class<IconProperty> = IconProperty::class.java

    override fun createNode(property: IconProperty): HBox
    {
        val hbox = HBox()
        hbox.spacing = 15.0
        val imageView = ImageView()

        val selectButton = Button("Wybierz obrazek")
        selectButton.maxWidth = Double.POSITIVE_INFINITY

        selectButton.onAction = EventHandler {
            val result = fileChooser.showOpenDialog(MargoJEditor.INSTANCE.workspaceController.scene.scene.window) ?: return@EventHandler

            try
            {
                val iis = ImageIO.createImageInputStream(result)
                val iterator = ImageIO.getImageReaders(iis)
                val format = if (iterator.hasNext()) MRFIconFormat.getByFormat(iterator.next().formatName) else null

                if (format == null)
                {
                    QuickAlert.create().warning().header("Wspierane formaty to gif i png!").showAndWait()
                    return@EventHandler
                }

                val image = ImageIO.read(iis)

                if (image.width != 32 || image.height != 32)
                {
                    QuickAlert.create().warning().header("Ikonka przedmiotu musi miec wymiary 32x32!").showAndWait()
                    return@EventHandler
                }

                var fileBytes: ByteArray? = null

                FileInputStream(result).use {
                    fileBytes = IOUtils.toByteArray(it)
                }

                val icon = MRFIcon(fileBytes!!, format, image)

                val itemEditor = MargoJEditor.INSTANCE.itemEditor
                itemEditor.addUndoAction(PropertyChangeUndoRedo(property, itemEditor.currentItem!![property], icon))
                itemEditor.currentItem!![property] = icon

                this.updateNodeTo(hbox, icon)
            }
            catch (e: Exception)
            {
                QuickAlert.create().exception(e).content("Nie można załadować ikonki").showAndWait()
            }
        }

        hbox.children.addAll(imageView, selectButton)
        HBox.setHgrow(imageView, Priority.NEVER)
        HBox.setHgrow(selectButton, Priority.ALWAYS)

        this.updateNodeToPlaceholder(hbox)

        return hbox
    }

    override fun update(property: IconProperty, node: HBox, value: MRFIcon?)
    {
        if (value == null)
        {
            this.updateNodeToPlaceholder(node)
            return
        }

        this.updateNodeTo(node, value)
    }

    override fun convert(property: IconProperty, node: HBox): MRFIcon?
    {
        return this.cache[node]
    }

    private fun updateNodeToPlaceholder(node: HBox)
    {
        this.updateNodeTo(node, placeholder)
    }

    private fun updateNodeTo(node: HBox, icon: MRFIcon)
    {
        if (icon !== placeholder)
        {
            this.cache.put(node, icon)
        }

        var cachedImage = icon.cachedImage

        if (cachedImage == null)
        {
            cachedImage = ImageIO.read(ByteArrayInputStream(icon.image))
        }

        this.getViewOfNode(node).image = SwingFXUtils.toFXImage(cachedImage, null)
    }

    private fun getViewOfNode(node: HBox): ImageView
    {
        return node.children.find { it is ImageView } as ImageView
    }

    private companion object
    {
        val placeholder = MRFIcon(ByteArray(0), MRFIconFormat.PNG, ImageIO.read(IconPropertyRenderer::class.java.classLoader.getResourceAsStream("icons/placeholder.png")))
    }
}

