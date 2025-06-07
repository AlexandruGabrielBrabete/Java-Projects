import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * A JFrame application that fills the panel with non-overlapping circles tangent to each other and the panel edges.
 * Circles have random radii between minRadius and maxRadius, random colors, and are placed to cover the area up to maxCount.
 */
public class TangentCirclesApp extends JFrame {
    public TangentCirclesApp() {
        super("Tangent Circles");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Create drawing panel with radius bounds and max circle count
        CirclePanel panel = new CirclePanel(20, 80, 500);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TangentCirclesApp::new);
    }
}

/**
 * JPanel that generates and draws tangent circles upon first resize.
 */
class CirclePanel extends JPanel {
    /**
     * Internal representation of a circle: center (x,y), radius, and color.
     */
    private static class Circle {
        int x, y, r;
        Color color;
    }

    private final List<Circle> circles = new ArrayList<>();
    private final int minRadius;
    private final int maxRadius;
    private final int maxCount;
    private final Random rnd = new Random();

    /**
     * Constructs the panel with radius bounds and desired number of circles.
     * Circles are generated once when the panel is first resized.
     */
    CirclePanel(int minRadius, int maxRadius, int maxCount) {
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.maxCount = maxCount;
        setPreferredSize(new Dimension(800, 600));
        // Listen for initial resize to trigger circle generation
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                generateCircles();
                // Remove listener so we don't regenerate on every resize
                removeComponentListener(this);
            }
        });
    }

    /**
     * Generates up to maxCount circles, each tangent to a reference circle or edge,
     * using a breadth-first search from a central seed circle.
     */
    private void generateCircles() {
        int w = getWidth();
        int h = getHeight();
        circles.clear();
        Queue<Circle> queue = new LinkedList<>();

        // Create the first central circle
        Circle first = new Circle();
        first.r = rnd.nextInt(maxRadius - minRadius + 1) + minRadius;
        first.x = w / 2;
        first.y = h / 2;
        first.color = randomColor();
        circles.add(first);
        queue.add(first);

        // Directions at hexagonal angles
        double[] angles = {0, Math.PI/3, 2*Math.PI/3, Math.PI, 4*Math.PI/3, 5*Math.PI/3};

        // Expand by placing circles around each in queue
        while (!queue.isEmpty() && circles.size() < maxCount) {
            Circle ref = queue.poll();
            for (double angle : angles) {
                if (circles.size() >= maxCount) break;
                // Try random radii up to 10 attempts
                for (int t = 0; t < 10; t++) {
                    int r = rnd.nextInt(maxRadius - minRadius + 1) + minRadius;
                    int x = ref.x + (int)((ref.r + r) * Math.cos(angle));
                    int y = ref.y + (int)((ref.r + r) * Math.sin(angle));
                    // Skip if out of bounds
                    if (x - r < 0 || x + r > w || y - r < 0 || y + r > h) continue;
                    // Check for overlap with existing circles
                    boolean valid = true;
                    for (Circle c2 : circles) {
                        int dx = c2.x - x;
                        int dy = c2.y - y;
                        int minDist = c2.r + r;
                        if (dx*dx + dy*dy < minDist*minDist) { valid = false; break; }
                    }
                    if (valid) {
                        Circle c = new Circle();
                        c.x = x; c.y = y; c.r = r;
                        c.color = randomColor();
                        circles.add(c);
                        queue.add(c);
                        break;
                    }
                }
            }
        }
        repaint();
    }

    /**
     * Generates a random color.
     */
    private Color randomColor() {
        return new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Circle c : circles) {
            g.setColor(c.color);
            g.fillOval(c.x - c.r, c.y - c.r, 2*c.r, 2*c.r);
        }
    }
}
