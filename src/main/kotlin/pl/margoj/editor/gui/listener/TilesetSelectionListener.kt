package pl.margoj.editor.gui.listener

import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.map.MapEditor
import pl.margoj.editor.map.RectangleSelection
import pl.margoj.mrf.map.Point

import javafx.event.EventHandler
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color

class TilesetSelectionListener(private val canvas: Canvas, private val editor: MapEditor, private val tilesetCanvasContainer: ScrollPane) : EventHandler<MouseEvent>
{
    private var startPoint: Point? = null
    private var lastPoint: Point? = null
    private var temporarySelection: RectangleSelection? = null

    override fun handle(event: MouseEvent)
    {
        if (FXUtils.pannableOnRightclick(event, this.tilesetCanvasContainer))
        {
            return
        }

        val inBounds = event.x <= editor.selectedTileset!!.wholeTilesetWidth && event.y <= editor.selectedTileset!!.wholeTilesetHeight
        val x = (event.x / 32).toInt()
        val y = (event.y / 32).toInt()

        this.doHandle(event, inBounds, x, y)
    }

    private fun doHandle(event: MouseEvent, inBounds: Boolean, x: Int, y: Int)
    {
        if (event.eventType == MouseEvent.MOUSE_PRESSED)
        {
            if (!inBounds)
            {
                return
            }

            this.editor.redrawTileset()
            this.startPoint = Point(x, y)
            this.temporarySelection = RectangleSelection(x, y, 1, 1)

            this.drawTemporarySelection()
        }
        else if (event.eventType == MouseEvent.MOUSE_DRAGGED)
        {
            val currentPoint = Point(x, y)

            if (!inBounds || this.editor.selectedTileset!!.auto || currentPoint == this.lastPoint)
            {
                return
            }

            this.lastPoint = currentPoint
            this.temporarySelection = RectangleSelection(this.startPoint!!, this.lastPoint!!)

            this.editor.redrawTileset()
            this.drawTemporarySelection()
        }
        else if (event.eventType == MouseEvent.MOUSE_MOVED)
        {
            if (this.isDragging || !inBounds)
            {
                return
            }

            this.temporarySelection = RectangleSelection(x, y, 1, 1)
            this.editor.redrawTileset()
            this.drawTemporarySelection()
        }
        else if (event.eventType == MouseEvent.MOUSE_EXITED)
        {
            if (this.isDragging || this.temporarySelection == null)
            {
                return
            }
            this.temporarySelection = null
            this.editor.redrawTileset()
        }
        else if (event.eventType == MouseEvent.MOUSE_RELEASED)
        {
            if (this.temporarySelection == null)
            {
                return
            }
            this.lastPoint = null
            this.startPoint = this.lastPoint

            this.editor.tilesetSelection = this.temporarySelection
            this.editor.redrawTileset()
        }
    }

    private fun drawTemporarySelection()
    {
        if (this.temporarySelection != null)
        {
            val graphics = this.canvas.graphicsContext2D
            graphics.stroke = Color.ORANGERED
            graphics.lineWidth = 1.0
            this.temporarySelection!!.draw(graphics, 32)
        }
    }

    private val isDragging: Boolean
        get() = this.startPoint != null
}
