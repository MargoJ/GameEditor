package pl.margoj.editor.map.objects

import javafx.scene.canvas.GraphicsContext
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.gateway.GatewayObject

class GatewayObjectTool : MapObjectTool<GatewayObject>("Przejście", "icons/objects/gateway.png", GatewayObject::class.java)
{
    override fun handle(map: MargoMap, point: Point)
    {
        FXUtils.loadDialog("map/tool/gateway", "Edytuj przejście", MargoJEditor.INSTANCE.workspaceController.scene.stage, Pair(map, point))
    }

    override fun render(g: GraphicsContext, mapObject: GatewayObject)
    {
        g.drawImage(this.icon, mapObject.position.x * 32.0, mapObject.position.y * 32.0)
    }
}