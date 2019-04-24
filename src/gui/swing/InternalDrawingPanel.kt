package gui.swing

import gui.StyleConstants
import java.awt.*
import java.awt.event.*
import java.util.*

import kotlin.collections.ArrayList
import java.util.HashSet
import javax.swing.JPanel
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import javax.imageio.ImageIO


/**
 * Created by HP xw8400
 * Author: Jacob
 * Date: 4/22/2019.
 */

class InternalDrawingPanel : JPanel(), MouseMotionListener, MouseListener, KeyListener {

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
     * The current color of the shape (default black
     */
    private var color = Color.BLACK

    /**
     * The current shape being drawn
     */
    private var currentShape = ConnectedShape(color, ArrayList())

    /**
     * A flag value that signalizes the end of a shape
     * A shape starts when the mouse is dragged,
     * and ends when the mouse is released after being dragged
     */
    private var drawing = false

    private var image: Image? = null

    init {
        addMouseMotionListener(this)
        addMouseListener(this)
        addKeyListener(this)
        preferredSize = StyleConstants.DEFAULT_SIZE
        isFocusable = true
        requestFocusInWindow()

        try {
            val image = ImageIO.read(File("resources/trash.png"))
            this.image = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH)
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    override fun mouseDragged(event: MouseEvent) {
        if (currentShape.points.isNotEmpty() && !shapes.contains(currentShape)) {
            shapes.push(currentShape)
        }
        if (!drawing) {
            currentShape = ConnectedShape(color, ArrayList())
        }
        if (!currentShape.points.contains(event.point)) {
            currentShape.points.add(event.point)
            repaint()
        }
        drawing = true
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

    override fun mouseEntered(event: MouseEvent) {}

    override fun mouseExited(event: MouseEvent) {}

    override fun mousePressed(event: MouseEvent) {}

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2 = g as Graphics2D
        g2.stroke = BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        shapes.forEach { shape ->
            shape.points.forEachIndexed { index, point ->
                run {
                    if (index > 0) {
                        val previous = shape.points[index - 1]
                        g2.color = shape.color
                        g2.drawLine(previous.x, previous.y, point.x, point.y)
                    }
                }
            }
        }
        g2.drawImage(image, 40, 500, null)
        g2.dispose()
    }

    private data class ConnectedShape(val color : Color, val points : ArrayList<Point>)
}