package pl.margoj.editor.map.objects

import javafx.scene.canvas.GraphicsContext
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.text.TextMapObject

class TextObjectTool : MapObjectTool<TextMapObject>("Tekst", "icons/objects/text.png", TextMapObject::class.java)
{
    override fun handle(map: MargoMap, point: Point)
    {
        FXUtils.loadDialog("map/tool/text", "Edytuj tekst", MargoJEditor.INSTANCE.workspaceController.scene.stage, Pair(map, point))
    }

    override fun render(g: GraphicsContext, mapObject: TextMapObject)
    {
        g.drawImage(this.icon, mapObject.position.x * 32.0, mapObject.position.y * 32.0)
    }
}