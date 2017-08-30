package pl.margoj.editor.map.cursor


import javafx.scene.input.MouseButton
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.map.RectangleSelection
import pl.margoj.editor.map.Selection
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.tileset.Tileset

interface Cursor
{
    val supportsCollisions: Boolean

    val supportsWater: Boolean

    fun getSelection(context: CursorContext): Selection

    fun mouseClicked(context: CursorContext)

    fun mouseDragged(context: CursorContext)

    fun apply(context: CursorContext): Collection<Point>

    fun redraw(context: CursorContext, changes: Selection)
    {
        if (context.isCollisionLayerSelected)
        {
            context.editor.redrawObjects(changes)
        }
        else
        {
            context.editor.redrawMapFragment(changes)
        }
    }

    companion object
    {
        val DEFAULT = SingleElementCursor.INSTANCE
    }
}


class CursorContext(val startPoint: Point, val currentPoint: Point, val onMapSelection: Selection?, val tileset: Tileset,
                    val onTilesetSelection: RectangleSelection, val map: MargoMap, val editor: MapEditor, val button: MouseButton?
)
{

    val isCollisionLayerSelected: Boolean get() = this.editor.currentLayer == MargoMap.COLLISION_LAYER

    val isWaterLayerSelected: Boolean get() = this.editor.currentLayer == MargoMap.WATER_LAYER
}
