package gui.panels

import core.PictionaryContext
import gui.StyleConstants
import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.JPanel
import kotlin.collections.ArrayList


/**
 * Created by HP xw8400
 * Author: Jacob
 * Date: 4/22/2019.
 */

class InternalDrawingPanel(val pictionary: PictionaryContext) : JPanel(), MouseMotionListener, MouseListener, KeyListener {

    var thickness = 10

    /**
     * The current color of the shape (default black)
     */
    var color = StyleConstants.PALETTE[15]

    /**
     * A set representing all of the extended key codes that are currently pressed
     */
    private val pressed = HashSet<Int>()

    /**
     * A stack of the shapes that have been drawn
     * LIFO allows the last shape drawn to easily be popped and deleted
     */
    private val shapes = Stack<ConnectedShape>()

    /**
     * The current shape being drawn
     */
    private var currentShape = ConnectedShape(color, ArrayList(), thickness)

    /**
     * A flag value that signalizes the end of a shape
     * A shape starts when the mouse is dragged,
     * and ends when the mouse is released after being dragged
     */
    private var drawing = false

    init {
        addMouseMotionListener(this)
        addMouseListener(this)
        addKeyListener(this)

        preferredSize = StyleConstants.DEFAULT_SIZE
        isFocusable = true
        requestFocusInWindow()
    }

    fun clear() {
        shapes.clear()
        currentShape.points.clear()
        repaint()
    }

    override fun mousePressed(event: MouseEvent) {
        //only start drawing if the left mouse button was pressed
        if (event.button == MouseEvent.BUTTON1) {
            //the mouse is pressed, start drawing
            drawing = true
            //create a new shape
            currentShape = ConnectedShape(color, ArrayList(), thickness)
            //add the current point to the shape's points
            currentShape.points.add(event.point)
            //push the shape into the stack
            shapes.push(currentShape)
            repaint()
        }
    }


    override fun mouseDragged(event: MouseEvent) {
        if (drawing && !currentShape.points.contains(event.point)) {
            currentShape.points.add(event.point)
            repaint()
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        drawing = false
    }

    @Synchronized
    override fun keyPressed(event: KeyEvent) {
        pressed.add(event.extendedKeyCode)
        //if ctrl + z was pressed, pop the last drawn shape off
        if (pressed.contains(17) && event.extendedKeyCode == 90 && shapes.isNotEmpty()) {
            shapes.pop()
            currentShape.points.clear()
            repaint()
        }
    }

    @Synchronized
    override fun keyReleased(event: KeyEvent) {
        pressed.remove(event.extendedKeyCode)
    }

    override fun keyTyped(event: KeyEvent) {}

    override fun mouseMoved(event: MouseEvent) {}

    override fun mouseClicked(event: MouseEvent) {}

    override fun mouseEntered(event: MouseEvent) {
        //a little annoying, but needs to be done
        //since the other panels will take focus
        requestFocusInWindow()
    }

    override fun mouseExited(event: MouseEvent) {}

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)

        val graphics2D = graphics as Graphics2D
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        //there will be only 1 point in a shape if the user performs a click and never drags the mouse
        //if there is only one point in the shape, just draw an oval
        //else, draw the lines connected

        shapes.forEach { shape ->
            graphics2D.color = shape.color
            graphics2D.stroke = BasicStroke(shape.thickness.toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, null, 0.0f)

            when (shape.points.size) {
                1 -> {
                    val point = shape.points[0]
                    graphics2D.fillOval(point.x - shape.thickness / 2, point.y - shape.thickness / 2, shape.thickness, shape.thickness)
                }
                else -> {
                    shape.points.forEachIndexed { index, point ->
                        run {
                            if (index > 0) {
                                val previous = shape.points[index - 1]
                                graphics2D.drawLine(previous.x, previous.y, point.x, point.y)
                            }
                        }
                    }
                }
            }
        }

        graphics2D.dispose()
    }

    private data class ConnectedShape(val color: Color, val points: ArrayList<Point>, val thickness: Int)
}