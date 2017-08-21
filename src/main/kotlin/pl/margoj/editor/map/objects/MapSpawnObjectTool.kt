package pl.margoj.editor.map.objects

import javafx.scene.canvas.GraphicsContext
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.actions.MapObjectUndoRedo
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.mapspawn.MapSpawnObject
import java.util.stream.Collectors

class MapSpawnObjectTool : MapObjectTool<MapSpawnObject>("Punkt spawnu", "icons/objects/respawn.png", MapSpawnObject::class.java)
{
    override fun handle(map: MargoMap, point: Point)
    {
        val editor = MargoJEditor.INSTANCE.mapEditor

        val old = map.getObject(point)
        if (old is MapSpawnObject)
        {
            editor.redrawObject(old)
            return
        }

        val toRemove = map.objects
                .stream()
                .filter { it is MapSpawnObject }
                .map { it as MapSpawnObject }
                .collect(Collectors.toList())

        for (mapObject in toRemove)
        {
            map.deleteObject(mapObject.position)
            editor.redrawObjects(Selection(this.getPoints(mapObject)))
            editor.addUndoAction(MapObjectUndoRedo(mapObject.position, mapObject, null, "Usunięcie obiektu"))
        }

        val mapObject = MapSpawnObject(point)
        map.addObject(mapObject)
        editor.redrawObject(mapObject)
        editor.addUndoAction(MapObjectUndoRedo(point, old, mapObject, "Dodaj/edytuj spawnpoint"))
    }

    override fun render(g: GraphicsContext, mapObject: MapSpawnObject)
    {
        g.drawImage(this.icon, mapObject.position.x * 32.0, mapObject.position.y * 32.0)
    }
}