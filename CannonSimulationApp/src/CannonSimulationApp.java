import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * A JFrame application simulating a fixed cannon in the bottom-left corner.
 * Users adjust angle and power via sliders, then press "Fire".
 * The projectile bounces elastically off side walls and ceiling,
 * and stops upon hitting the floor. Its trajectory is drawn,
 * and the final vertical velocity is displayed.
 */
public class CannonSimulationApp extends JFrame {
    public CannonSimulationApp() {
        super("Cannon Simulation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        ControlPanel controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        GamePanel gamePanel = new GamePanel(controlPanel);
        add(gamePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CannonSimulationApp::new);
    }
}

/**
 * Panel with controls for setting cannon angle and firing power.
 */
class ControlPanel extends JPanel {
    final JSlider angleSlider;
    final JSlider powerSlider;
    final JButton fireButton;

    ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // Angle slider: 0° to 90°
        angleSlider = new JSlider(0, 90, 45);
        angleSlider.setMajorTickSpacing(15);
        angleSlider.setPaintTicks(true);
        angleSlider.setPaintLabels(true);

        // Power slider: 10 to 1000 units
        powerSlider = new JSlider(10, 1000, 200);
        powerSlider.setMajorTickSpacing(200);
        powerSlider.setMinorTickSpacing(50);
        powerSlider.setPaintTicks(true);
        powerSlider.setPaintLabels(true);
        powerSlider.setLabelTable(powerSlider.createStandardLabels(200));
        powerSlider.setPreferredSize(new Dimension(700, powerSlider.getPreferredSize().height));

        fireButton = new JButton("Fire");

        add(new JLabel("Angle:"));
        add(angleSlider);
        add(new JLabel("Power:"));
        add(powerSlider);
        add(fireButton);
    }
}

/**
 * Panel that runs the physics simulation and renders the cannon, trajectory, and results.
 */
class GamePanel extends JPanel implements ActionListener {
    private final ControlPanel controls;
    private final Timer timer;
    private double x, y;                // Projectile position
    private double vx, vy;              // Projectile velocity components
    private final double dt = 0.05;     // Time step (seconds)
    private boolean active;             // Whether simulation is running
    private final List<Point> trajectory = new ArrayList<>();
    private double finalVy;             // Final vertical velocity at impact

    GamePanel(ControlPanel controls) {
        this.controls = controls;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // Listen for Fire button
        controls.fireButton.addActionListener(e -> launchProjectile());

        // Timer drives the simulation steps
        timer = new Timer((int)(dt * 1000), this);
    }

    /**
     * Initializes projectile parameters and starts the simulation.
     */
    private void launchProjectile() {
        // Start near bottom-left corner
        x = 20;
        y = getHeight() - 20;

        double angleRad = Math.toRadians(controls.angleSlider.getValue());
        double power = controls.powerSlider.getValue();
        double speed = power * (1 + Math.cos(angleRad)); // scale factor

        vx = speed * Math.cos(angleRad);
        vy = -speed * Math.sin(angleRad); // negative for upward

        trajectory.clear();
        active = true;
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!active) return;

        // Euler integration step
        x += vx * dt;
        y += vy * dt;
        vy += 9.81 * dt; // gravity acts downward

        int w = getWidth();
        int h = getHeight();
        double bounceCoef = 0.8; // energy loss factor

        // Bounce off left wall
        if (x < 20) {
            x = 20;
            vx = -vx * bounceCoef;
            vy *= bounceCoef;
        }
        // Bounce off right wall
        if (x > w - 20) {
            x = w - 20;
            vx = -vx * bounceCoef;
            vy *= bounceCoef;
        }
        // Bounce off ceiling
        if (y < 20) {
            y = 20;
            vy = -vy * bounceCoef;
            vx *= bounceCoef;
        }
        // Impact floor: end simulation
        if (y > h - 20) {
            y = h - 20;
            finalVy = vy;
            active = false;
            timer.stop();
        }

        trajectory.add(new Point((int)x, (int)y));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw boundary rectangle
        g2.setColor(Color.RED);
        g2.drawRect(20, 20, getWidth() - 40, getHeight() - 40);

        // Draw cannon
        drawCannon(g2);

        // Draw trajectory
        g2.setColor(Color.BLUE);
        for (Point p : trajectory) {
            g2.fillOval(p.x - 3, p.y - 3, 6, 6);
        }

        // Draw projectile or results
        if (active) {
            g2.setColor(Color.BLACK);
            g2.fillOval((int)x - 5, (int)y - 5, 10, 10);
        } else if (!trajectory.isEmpty()) {
            // Display final vertical speed
            g2.setColor(Color.MAGENTA);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(String.format("Final Vy: %.2f", finalVy), 30, 30);
            // Mark impact point
            Point p = trajectory.get(trajectory.size() - 1);
            g2.setColor(Color.RED);
            int s = 10;
            g2.drawLine(p.x - s, p.y - s, p.x + s, p.y + s);
            g2.drawLine(p.x - s, p.y + s, p.x + s, p.y - s);
        }
    }

    /**
     * Draws the cannon at the fixed base, rotated by the current angle.
     */
    private void drawCannon(Graphics2D g2) {
        double angleRad = Math.toRadians(controls.angleSlider.getValue());
        int baseX = 20;
        int baseY = getHeight() - 20;

        AffineTransform old = g2.getTransform();
        g2.translate(baseX, baseY);
        g2.rotate(-angleRad);

        // Barrel length depends on power setting
        int length = 60 + controls.powerSlider.getValue() / 5;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, -5, length, 10);

        g2.setTransform(old);
        g2.setColor(Color.GRAY);
        g2.fillOval(baseX - 10, baseY - 10, 20, 20); // cannon pivot
    }
}
