import javax.swing.*;
import java.awt.*;

/**
 * A JFrame application that renders a television test pattern:
 * - Displays 10 horizontal grayscale bands from white to black.
 * - Displays 6 horizontal bands of primary and secondary colors (Red, Green, Blue, Yellow, Cyan, Magenta).
 * The test pattern fills the entire panel area and scales with the window size.
 */
public class TVTestPatternApp extends JFrame {

    /**
     * Constructor: sets up the frame and adds the drawing panel.
     */
    public TVTestPatternApp() {
        super("TV Test Pattern");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Add the custom panel that draws the test pattern
        add(new TestPatternPanel());
        setSize(600, 400);              // Initial window size
        setLocationRelativeTo(null);    // Center on screen
        setVisible(true);               // Show the window
    }

    /**
     * Custom JPanel that draws the grayscale and color bands.
     */
    static class TestPatternPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();        // Panel width
            int height = getHeight();      // Panel height
            // Total stripes: 10 grayscale + 6 color
            int totalStrips = 10 + 6;
            int stripHeight = height / totalStrips;

            // Draw 10 grayscale stripes (from white to black)
            for (int i = 0; i < 10; i++) {
                int level = (int) (255 - (i * (255.0 / 9)));  // Compute gray intensity
                g.setColor(new Color(level, level, level));  // Set gray shade
                g.fillRect(0, i * stripHeight, width, stripHeight);
            }

            // Define the 6 basic colors to draw in order
            Color[] baseColors = {
                Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.CYAN, Color.MAGENTA
            };
            // Draw each color stripe below the grayscale
            for (int j = 0; j < baseColors.length; j++) {
                g.setColor(baseColors[j]);
                int y = (10 + j) * stripHeight;                // Vertical offset
                g.fillRect(0, y, width, stripHeight);
            }
        }
    }

    /**
     * Entry point: launches the UI on the Swing Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TVTestPatternApp::new);
    }
}
