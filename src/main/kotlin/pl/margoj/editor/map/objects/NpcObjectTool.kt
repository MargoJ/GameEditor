package pl.margoj.editor.map.objects

import javafx.scene.canvas.GraphicsContext
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.npc.NpcMapObject

class NpcObjectTool : MapObjectTool<NpcMapObject>("NPC", "icons/objects/npc.png", NpcMapObject::class.java)
{
    override fun handle(map: MargoMap, point: Point)
    {
        FXUtils.loadDialog("map/tool/npc", "Edytuj NPC", MargoJEditor.INSTANCE.workspaceController.scene.stage, Pair(map, point))
    }

    override fun render(g: GraphicsContext, mapObject: NpcMapObject)
    {
        g.drawImage(this.icon, mapObject.position.x * 32.0, mapObject.position.y * 32.0)
    }
}