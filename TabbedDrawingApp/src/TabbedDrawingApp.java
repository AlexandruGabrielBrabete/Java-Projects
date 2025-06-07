import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.*;

/**
 * A JFrame-based application featuring a tabbed drawing interface.
 * Each tab hosts a canvas area for rendering text and shapes,
 * along with controls to select font, size, text color,
 * geometric shape (Circle, Square, Triangle), and its dimensions.
 */
public class TabbedDrawingApp extends JFrame {
    /**
     * Constructs the main application window with two drawing tabs.
     */
    public TabbedDrawingApp() {
        super("Tabbed Drawing Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // center on screen

        // Create tabbed pane and add two DrawingTab panels
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tab 1", new DrawingTab());
        tabbedPane.addTab("Tab 2", new DrawingTab());
        add(tabbedPane);

        setVisible(true);
    }

    public static void main(String[] args) {
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(TabbedDrawingApp::new);
    }
}

/**
 * A panel combining a drawing canvas with controls
 * for text and shape customization.
 */
class DrawingTab extends JPanel {
    private final CanvasPanel canvas;
    private final JComboBox<String> fontComboBox;
    private final JComboBox<Integer> fontSizeComboBox;
    private final JButton textColorButton;
    private final JComboBox<String> shapeComboBox;
    private final JSpinner shapeSizeSpinner;

    private Color currentTextColor = Color.BLACK;
    private Color currentShapeColor = Color.BLUE;

    /**
     * Initializes layout, canvas, and control components.
     */
    DrawingTab() {
        setLayout(new BorderLayout(5, 5));

        // Canvas for drawing text and shapes
        canvas = new CanvasPanel();
        add(canvas, BorderLayout.CENTER);

        // Control panel with flow layout on the bottom
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Font family selection populated with system fonts
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fontComboBox = new JComboBox<>(ge.getAvailableFontFamilyNames());
        fontComboBox.setSelectedItem("Arial");

        // Font size selection options
        fontSizeComboBox = new JComboBox<>(new Integer[]{8, 12, 16, 20, 24, 32, 48});
        fontSizeComboBox.setSelectedItem(16);

        // Button to choose text color
        textColorButton = new JButton("Text Color");

        // Shape type selection
        shapeComboBox = new JComboBox<>(new String[]{"Circle", "Square", "Triangle"});

        // Spinner for shape dimension in pixels
        shapeSizeSpinner = new JSpinner(new SpinnerNumberModel(50, 10, 300, 10));

        // Add labeled controls to the panel
        controlPanel.add(new JLabel("Font:"));
        controlPanel.add(fontComboBox);
        controlPanel.add(new JLabel("Font Size:"));
        controlPanel.add(fontSizeComboBox);
        controlPanel.add(textColorButton);
        controlPanel.add(new JLabel("Shape:"));
        controlPanel.add(shapeComboBox);
        controlPanel.add(new JLabel("Size:"));
        controlPanel.add(shapeSizeSpinner);

        add(controlPanel, BorderLayout.SOUTH);

        // Shared listener to update canvas when selections change
        ActionListener updateTrigger = e -> refreshCanvas();
        fontComboBox.addActionListener(updateTrigger);
        fontSizeComboBox.addActionListener(updateTrigger);
        shapeComboBox.addActionListener(updateTrigger);
        shapeSizeSpinner.addChangeListener(e -> refreshCanvas());

        // Open color chooser dialog and update text color
        textColorButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Select Text Color", currentTextColor);
            if (chosen != null) {
                currentTextColor = chosen;
                refreshCanvas();
            }
        });
    }

    /**
     * Applies selected attributes to the canvas and repaints.
     */
    private void refreshCanvas() {
        String fontName = (String) fontComboBox.getSelectedItem();
        int fontSize = (Integer) fontSizeComboBox.getSelectedItem();
        canvas.setTextAttributes(fontName, fontSize, currentTextColor);

        String shapeType = (String) shapeComboBox.getSelectedItem();
        int shapeSize = (Integer) shapeSizeSpinner.getValue();
        canvas.setShapeAttributes(shapeType, shapeSize, currentShapeColor);

        canvas.repaint();
    }
}

/**
 * A panel responsible for rendering text and a geometric shape,
 * centered within its bounds, based on provided attributes.
 */
class CanvasPanel extends JPanel {
    private String fontName = "Arial";
    private int fontSize = 16;
    private Color textColor = Color.BLACK;
    private String shapeType = "Circle";
    private int shapeSize = 50;
    private Color shapeColor = Color.BLUE;

    CanvasPanel() {
        setBackground(Color.WHITE); // blank drawing surface
    }

    /**
     * Update text drawing parameters.
     */
    void setTextAttributes(String fontName, int size, Color color) {
        this.fontName = fontName;
        this.fontSize = size;
        this.textColor = color;
    }

    /**
     * Update shape drawing parameters.
     */
    void setShapeAttributes(String shapeType, int size, Color color) {
        this.shapeType = shapeType;
        this.shapeSize = size;
        this.shapeColor = color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // Draw sample text centered near top
        g2.setColor(textColor);
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        g2.setFont(font);
        String text = "Sample Text";
        FontMetrics fm = g2.getFontMetrics();
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = fm.getAscent() + 10;
        g2.drawString(text, textX, textY);

        // Draw the selected shape centered
        g2.setColor(shapeColor);
        int centerX = width / 2;
        int centerY = height / 2;
        int half = shapeSize / 2;

        switch (shapeType) {
            case "Circle":
                g2.fillOval(centerX - half, centerY - half, shapeSize, shapeSize);
                break;
            case "Square":
                g2.fillRect(centerX - half, centerY - half, shapeSize, shapeSize);
                break;
            case "Triangle":
                Polygon triangle = new Polygon();
                triangle.addPoint(centerX, centerY - half);
                triangle.addPoint(centerX - half, centerY + half);
                triangle.addPoint(centerX + half, centerY + half);
                g2.fillPolygon(triangle);
                break;
        }
    }
}
