import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A JFrame application featuring a customizable vehicle animation:
 * - Vehicle types: Car, Truck, Motorcycle.
 * - Environments: Hills, Sea, Highway.
 * - Sun and clouds in the sky.
 * - Toggle rain and lightning effects.
 * - User-selectable vehicle color.
 */
public class VehicleAnimationApp extends JFrame {
    public VehicleAnimationApp() {
        super("Customizable Vehicle Animation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Control panel at top for user settings
        ControlPanel controls = new ControlPanel();
        add(controls, BorderLayout.NORTH);

        // Animation canvas in center
        AnimationCanvas canvas = new AnimationCanvas(controls);
        add(canvas, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        canvas.startAnimation();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VehicleAnimationApp::new);
    }
}

/**
 * Panel containing UI controls for animation settings.
 */
class ControlPanel extends JPanel {
    JComboBox<String> vehicleTypeBox;
    JComboBox<String> environmentBox;
    JButton colorChooserButton;
    JCheckBox rainToggle;
    JCheckBox lightningToggle;
    Color vehicleColor = Color.RED;

    ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        vehicleTypeBox = new JComboBox<>(new String[]{"Car", "Truck", "Motorcycle"});
        environmentBox = new JComboBox<>(new String[]{"Hills", "Sea", "Highway"});
        colorChooserButton = new JButton("Choose Vehicle Color");
        rainToggle = new JCheckBox("Rain");
        lightningToggle = new JCheckBox("Lightning");
        lightningToggle.setEnabled(false);

        add(new JLabel("Vehicle:"));
        add(vehicleTypeBox);
        add(new JLabel("Environment:"));
        add(environmentBox);
        add(colorChooserButton);
        add(rainToggle);
        add(lightningToggle);

        // Show color chooser dialog
        colorChooserButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Select Vehicle Color", vehicleColor);
            if (chosen != null) {
                vehicleColor = chosen;
            }
        });

        // Enable lightning only if rain is selected
        rainToggle.addActionListener(e -> lightningToggle.setEnabled(rainToggle.isSelected()));
    }
}

/**
 * Panel that performs the animation drawing.
 */
class AnimationCanvas extends JPanel implements ActionListener {
    private final ControlPanel controls;
    private final Timer timer;
    private float wheelAngle = 0f;
    private int vehicleX = -200;
    private final Random random = new Random();
    private final List<Point> clouds = new ArrayList<>();

    AnimationCanvas(ControlPanel controls) {
        this.controls = controls;
        setPreferredSize(new Dimension(800, 400));
        timer = new Timer(30, this);
        // Initialize cloud positions
        for (int i = 0; i < 5; i++) {
            clouds.add(new Point(random.nextInt(800), random.nextInt(100)));
        }
    }

    void startAnimation() {
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Move vehicle
        vehicleX += 3;
        if (vehicleX > getWidth() + 200) {
            vehicleX = -200;
        }
        wheelAngle += 0.2f;
        // Move clouds
        for (Point p : clouds) {
            p.x += 1;
            if (p.x > getWidth()) {
                p.x = -100;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        // Draw sky
        g.setColor(new Color(135, 206, 235));
        g.fillRect(0, 0, w, h);
        drawSun(g, w, h);
        drawClouds(g);
        // Draw selected environment
        String env = (String) controls.environmentBox.getSelectedItem();
        switch (env) {
            case "Hills": drawHills(g, w, h); break;
            case "Sea": drawSea(g, w, h); break;
            case "Highway": drawHighway(g, w, h); break;
        }
        // Rain and lightning effects
        if (controls.rainToggle.isSelected()) {
            drawRain(g, w, h);
        }
        if (controls.rainToggle.isSelected() && controls.lightningToggle.isSelected()
            && random.nextInt(100) < 2) {
            drawLightning(g, w, h);
        }
        // Draw the vehicle
        drawVehicle(g, vehicleX, h - 100);
    }

    private void drawSun(Graphics g, int w, int h) {
        g.setColor(Color.YELLOW);
        g.fillOval(w - 100, 20, 80, 80);
    }

    private void drawClouds(Graphics g) {
        g.setColor(Color.WHITE);
        for (Point p : clouds) {
            g.fillOval(p.x, p.y, 100, 50);
        }
    }

    private void drawHills(Graphics g, int w, int h) {
        g.setColor(new Color(34, 139, 34));
        g.fillOval(-100, h - 150, 400, 200);
        g.fillOval(200, h - 180, 500, 250);
    }

    private void drawSea(Graphics g, int w, int h) {
        g.setColor(new Color(0, 105, 148));
        g.fillRect(0, h / 2, w, h / 2);
    }

    private void drawHighway(Graphics g, int w, int h) {
        g.setColor(Color.GRAY);
        g.fillRect(0, h - 100, w, 100);
        g.setColor(Color.WHITE);
        for (int x = 0; x < w; x += 40) {
            g.fillRect(x, h - 50, 20, 5);
        }
    }

    private void drawRain(Graphics g, int w, int h) {
        g.setColor(Color.CYAN);
        for (int i = 0; i < 200; i++) {
            int x = random.nextInt(w), y = random.nextInt(h);
            g.drawLine(x, y, x, y + 5);
        }
    }

    private void drawLightning(Graphics g, int w, int h) {
        g.setColor(Color.WHITE);
        int x = w / 2;
        g.drawLine(x, 0, x + 10, 80);
        g.drawLine(x + 10, 80, x - 10, 160);
        g.drawLine(x - 10, 160, x + 10, 240);
    }

    /**
     * Draws the selected vehicle at (x, y).
     */
    private void drawVehicle(Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        Color vc = controls.vehicleColor;
        String type = (String) controls.vehicleTypeBox.getSelectedItem();

        if (type.equals("Car")) {
            // Car body
            g2.setColor(vc);
            g2.fillRoundRect(x, y - 30, 120, 30, 15, 15);
            // Car cabin
            g2.setColor(vc.darker());
            g2.fillRect(x + 15, y - 45, 60, 20);
            // Windows
            g2.setColor(new Color(135, 206, 250, 180));
            g2.fillRect(x + 20, y - 40, 25, 15);
            g2.fillRect(x + 55, y - 40, 25, 15);
            // Headlight
            g2.setColor(Color.YELLOW);
            g2.fillOval(x + 110, y - 20, 8, 8);
        } else if (type.equals("Truck")) {
            // Truck bed
            g2.setColor(vc);
            g2.fillRect(x, y - 40, 160, 40);
            // Truck cabin
            g2.setColor(vc.darker());
            g2.fillRoundRect(x + 10, y - 60, 50, 40, 10, 10);
            // Window
            g2.setColor(new Color(135, 206, 250, 180));
            g2.fillRect(x + 15, y - 55, 40, 25);
            // Headlight
            g2.setColor(Color.YELLOW);
            g2.fillOval(x + 150, y - 30, 10, 10);
        } else {
            // Motorcycle frame lines
            g2.setColor(vc);
            g2.setStroke(new BasicStroke(5));
            g2.drawLine(x, y, x + 30, y - 20);
            g2.drawLine(x + 30, y - 20, x + 60, y);
            g2.drawLine(x + 60, y, x + 20, y);
            // Motorcycle seat
            g2.setColor(vc.darker());
            g2.fillRect(x + 25, y - 25, 20, 10);
        }
        // Draw wheels for all vehicle types
        g2.setColor(Color.BLACK);
        int r = 15;
        drawWheel(g2, x + 30, y, r);
        drawWheel(g2, x + 80, y, r);
        if (type.equals("Truck")) {
            drawWheel(g2, x + 130, y, r);
        }
        if (type.equals("Motorcycle")) {
            drawWheel(g2, x + 60, y, r);
        }
    }

    /**
     * Draws a rotating wheel at center (cx, cy) with radius r.
     */
    private void drawWheel(Graphics2D g2, int cx, int cy, int r) {
        g2.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        g2.setColor(Color.GRAY);
        for (int i = 0; i < 6; i++) {
            double angle = wheelAngle + i * Math.PI / 3;
            int x2 = cx + (int) (r * Math.cos(angle));
            int y2 = cy + (int) (r * Math.sin(angle));
            g2.drawLine(cx, cy, x2, y2);
        }
    }
}
