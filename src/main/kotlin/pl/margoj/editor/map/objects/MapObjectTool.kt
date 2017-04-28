package pl.margoj.editor.map.objects

import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import pl.margoj.editor.EditorApplication
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject
import java.util.Collections
import javax.imageio.ImageIO

abstract class MapObjectTool<T : MapObject<*>>(val name: String, val iconName: String, val objectType: Class<T>)
{
    val icon: Image = SwingFXUtils.toFXImage(ImageIO.read(EditorApplication::class.java.classLoader.getResourceAsStream(this.iconName)), null)

    abstract fun handle(map: MargoMap, point: Point)

    abstract fun render(g: GraphicsContext, mapObject: T)

    open fun getPoints(mapObject: T): Collection<Point>
    {
        return Collections.singletonList(Point(mapObject.position.x, mapObject.position.y))
    }

    open fun isContainedIn(point: Point, mapObject: T): Boolean
    {
        return this.getPoints(mapObject).contains(point)
    }
}