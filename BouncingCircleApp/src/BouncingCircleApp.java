import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A JFrame application featuring a Start/Stop button and an animated canvas.
 * Pressing Start launches a thread that draws a circle bouncing off the panel edges.
 */
public class BouncingCircleApp extends JFrame {
    // Button to start or stop the animation
    private final JButton startStopButton;
    // Custom panel where the circle is drawn and moved
    private final AnimationCanvas animationCanvas;
    // Flag to control animation thread
    private volatile boolean isAnimating = false;
    // Thread responsible for updating animation
    private Thread animationThread;

    /**
     * Constructor: sets up the window, button, and animation canvas.
     */
    public BouncingCircleApp() {
        super("Bouncing Circle Animation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top panel with Start/Stop button
        startStopButton = new JButton("Start");
        startStopButton.addActionListener(this::handleStartStop);
        JPanel controlPanel = new JPanel();
        controlPanel.add(startStopButton);
        add(controlPanel, BorderLayout.NORTH);

        // Center panel for animation
        animationCanvas = new AnimationCanvas();
        animationCanvas.setPreferredSize(new Dimension(400, 300));
        add(animationCanvas, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // center the window
        setVisible(true);
    }

    /**
     * Toggles animation on or off when the button is pressed.
     */
    private void handleStartStop(ActionEvent e) {
        if (!isAnimating) {
            // Start animation
            isAnimating = true;
            startStopButton.setText("Stop");
            animationThread = new Thread(() -> {
                while (isAnimating) {
                    animationCanvas.updateCirclePosition();
                    animationCanvas.repaint();
                    try {
                        Thread.sleep(10); // control frame rate
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            animationThread.start();
        } else {
            // Stop animation
            isAnimating = false;
            startStopButton.setText("Start");
            // Thread will exit naturally
        }
    }

    /**
     * Custom JPanel that moves and draws a bouncing circle.
     */
    private static class AnimationCanvas extends JPanel {
        // Current position of the circle
        private int x = 0, y = 0;
        // Velocity in x and y directions
        private int dx = 2, dy = 2;
        // Diameter of the circle
        private static final int DIAMETER = 30;

        /**
         * Updates the circle's position and reverses direction on boundary collision.
         */
        public void updateCirclePosition() {
            int width = getWidth();
            int height = getHeight();
            x += dx;
            y += dy;
            // Bounce off left/right edges
            if (x < 0 || x + DIAMETER > width) {
                dx = -dx;
                x += dx;
            }
            // Bounce off top/bottom edges
            if (y < 0 || y + DIAMETER > height) {
                dy = -dy;
                y += dy;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw filled circle with a fixed color
            g.setColor(Color.BLUE);
            g.fillOval(x, y, DIAMETER, DIAMETER);
        }
    }

    /**
     * Entry point: launches the GUI on the Swing Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BouncingCircleApp::new);
    }
}
