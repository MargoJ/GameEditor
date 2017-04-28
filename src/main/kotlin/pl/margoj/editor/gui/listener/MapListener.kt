package pl.margoj.editor.gui.listener


import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.map.Selection
import pl.margoj.editor.map.cursor.Cursor
import pl.margoj.editor.map.cursor.CursorContext
import pl.margoj.editor.map.objects.MapObjectTool
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.Point

class MapListener(private val editor: MapEditor) : EventHandler<MouseEvent>
{
    private var lastCursorPosition: Point? = null
    private var temporarySelection: Selection? = null
    private var startPoint: Point? = null
    private var lastPoint: Point? = null

    override fun handle(event: MouseEvent)
    {
        if (FXUtils.pannableOnRightclick(event, this.editor.workspaceController.mapCanvasContainer))
        {
            return
        }

        val x = Math.max(Math.min(event.x / 32, (this.editor.currentMap!!.width - 1).toDouble()), 0.0).toInt()
        val y = Math.max(Math.min(event.y / 32, (this.editor.currentMap!!.height - 1).toDouble()), 0.0).toInt()

        if (event.eventType == MouseEvent.MOUSE_MOVED)
        {
            val currentCursorPosition = Point(x, y)
            if (currentCursorPosition == this.lastCursorPosition)
            {
                return
            }
            this.lastCursorPosition = currentCursorPosition

            val current = Point(x, y)
            val context = this.createContext(current, current)

            this.updateTemporarySelection(this.getCursor(context).getSelection(context))
        }

        if (editor.currentLayer < MargoMap.LAYERS || editor.currentLayer == MargoMap.COLLISION_LAYER)
        {
            this.handleNormalLayer(event, x, y)
        }
        else if (editor.currentLayer == MargoMap.OBJECT_LAYER)
        {
            this.handleObjectLayer(event, x, y)
        }
    }

    private fun handleNormalLayer(event: MouseEvent, x: Int, y: Int)
    {
        if (event.eventType == MouseEvent.MOUSE_PRESSED)
        {
            this.lastPoint = Point(x, y)
            this.startPoint = this.lastPoint

            val context = this.createContext(this.startPoint!!, this.lastPoint!!)
            val cursor = this.getCursor(context)
            cursor.mouseClicked(context)
            cursor.mouseDragged(context)
            this.updateTemporarySelection(cursor.getSelection(context))
        }
        else if (event.eventType == MouseEvent.MOUSE_DRAGGED)
        {
            val currentPoint = Point(x, y)

            if (currentPoint == this.lastPoint)
            {
                return
            }

            this.lastPoint = currentPoint

            val context = this.createContext(this.startPoint!!, currentPoint)
            val cursor = this.getCursor(context)
            cursor.mouseDragged(context)

            this.updateTemporarySelection(cursor.getSelection(context))
        }
        else if (event.eventType == MouseEvent.MOUSE_EXITED)
        {
            if (this.temporarySelection == null)
            {
                return
            }

            this.updateTemporarySelection(null)
        }
        else if (event.eventType == MouseEvent.MOUSE_RELEASED)
        {
            val context = this.createContext(Point(x, y), this.lastPoint!!)
            val cursor = this.getCursor(context)
            val modifications = cursor.apply(context)
            cursor.redraw(context, Selection(modifications))
            this.temporarySelection = cursor.getSelection(context)
            this.drawTemporarySelection()
        }
    }


    private fun handleObjectLayer(event: MouseEvent, x: Int, y: Int)
    {
        if(event.eventType != MouseEvent.MOUSE_RELEASED)
        {
            return
        }

        val selectedName = this.editor.workspaceController.objectsList.selectionModel.selectedItem ?: return
        val tool: MapObjectTool<*> = this.editor.mapObjectTools.filter { it.name == selectedName }.firstOrNull() ?: return

        tool.handle(this.editor.currentMap!!, Point(x, y))
    }

    private fun createContext(startPoint: Point, lastPoint: Point): CursorContext
    {
        return CursorContext(
                startPoint,
                lastPoint,
                this.temporarySelection,
                this.editor.selectedTileset!!,
                this.editor.tilesetSelection!!,
                this.editor.currentMap!!,
                this.editor
        )
    }

    private fun getCursor(context: CursorContext): Cursor
    {
        var cursor = this.editor.cursor
        if (context.isCollisionLayerSelected && !cursor.supportsCollisions)
        {
            cursor = Cursor.DEFAULT
        }
        return cursor
    }

    private fun updateTemporarySelection(selection: Selection?)
    {
        if (this.temporarySelection != null)
        {
            this.editor.redrawObjects(Selection(this.temporarySelection!!.changedPoints))
        }

        this.temporarySelection = selection

        this.drawTemporarySelection()
    }

    private fun drawTemporarySelection()
    {
        if (this.temporarySelection != null)
        {
            val graphics = this.editor.workspaceController.objectCanvas.graphicsContext2D
            graphics.stroke = Color.PALEVIOLETRED
            graphics.lineWidth = 1.0
            this.temporarySelection!!.draw(graphics, 32)
        }
    }
}
