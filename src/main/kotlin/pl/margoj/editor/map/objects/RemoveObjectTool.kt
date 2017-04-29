package pl.margoj.editor.map.objects

import javafx.scene.canvas.GraphicsContext
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.actions.MapObjectUndoRedo
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.MapObject

class RemoveObjectTool : MapObjectTool<MapObject<*>>("Usuń obiekt", "icons/objects/remove.png", MapObject::class.java)
{
    override fun handle(map: MargoMap, point: Point)
    {
        val editor = MargoJEditor.INSTANCE.mapEditor
        val current = map.getObject(point) ?: return
        map.deleteObject(point)
        editor.redrawObjects(Selection(editor.getToolForObject(current)!!.getPoints(current)))
        editor.addUndoAction(MapObjectUndoRedo(point, current, null, "Usunięcie obiektu"))
    }

    override fun render(g: GraphicsContext, mapObject: MapObject<*>)
    {
    }
}