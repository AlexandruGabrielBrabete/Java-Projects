import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * A GUI application that allows users to adjust Red, Green, and Blue values via sliders,
 * and displays a color preview with corresponding numeric RGB values.
 */
public class RGBSliderDemo extends JFrame {
    // Sliders for red, green, and blue color components
    private JSlider redSlider, greenSlider, blueSlider;
    // Labels displaying current slider values
    private JLabel redValueLabel, greenValueLabel, blueValueLabel;
    // Panel to preview the selected color
    private ColorPreviewPanel previewPanel;
    // Label showing the RGB numeric representation
    private JLabel rgbValueLabel;

    /**
     * Constructs the RGB slider demo window and initializes components.
     */
    public RGBSliderDemo() {
        super("RGB Slider Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // Create panel containing sliders with a titled border
        JPanel slidersPanel = new JPanel(new GridBagLayout());
        slidersPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Adjust Color",
            TitledBorder.LEFT, TitledBorder.TOP));
        slidersPanel.setBackground(Color.WHITE);

        // Constraints for grid layout of sliders
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row for Red slider
        gbc.gridx = 0; gbc.gridy = 0;
        slidersPanel.add(new JLabel("Red:"), gbc);
        redSlider = createColorSlider();
        gbc.gridx = 1;
        slidersPanel.add(redSlider, gbc);
        redValueLabel = new JLabel("0");
        gbc.gridx = 2;
        slidersPanel.add(redValueLabel, gbc);

        // Row for Green slider
        gbc.gridx = 0; gbc.gridy = 1;
        slidersPanel.add(new JLabel("Green:"), gbc);
        greenSlider = createColorSlider();
        gbc.gridx = 1;
        slidersPanel.add(greenSlider, gbc);
        greenValueLabel = new JLabel("0");
        gbc.gridx = 2;
        slidersPanel.add(greenValueLabel, gbc);

        // Row for Blue slider
        gbc.gridx = 0; gbc.gridy = 2;
        slidersPanel.add(new JLabel("Blue:"), gbc);
        blueSlider = createColorSlider();
        gbc.gridx = 1;
        slidersPanel.add(blueSlider, gbc);
        blueValueLabel = new JLabel("0");
        gbc.gridx = 2;
        slidersPanel.add(blueValueLabel, gbc);

        // Add sliders panel to the west side
        add(slidersPanel, BorderLayout.WEST);

        // Create preview panel with titled border
        JPanel previewContainer = new JPanel(new BorderLayout(5, 5));
        previewContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Color Preview",
            TitledBorder.LEFT, TitledBorder.TOP));
        previewContainer.setBackground(Color.WHITE);

        // Panel showing the color swatch
        previewPanel = new ColorPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(150, 150));
        previewContainer.add(previewPanel, BorderLayout.CENTER);

        // Label below showing numeric RGB values
        rgbValueLabel = new JLabel("RGB(0, 0, 0)", SwingConstants.CENTER);
        rgbValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        previewContainer.add(rgbValueLabel, BorderLayout.SOUTH);

        add(previewContainer, BorderLayout.CENTER);

        // Listener to respond to slider changes
        ChangeListener sliderChangeListener = e -> updateColorPreview();
        redSlider.addChangeListener(sliderChangeListener);
        greenSlider.addChangeListener(sliderChangeListener);
        blueSlider.addChangeListener(sliderChangeListener);

        // Finalize window setup
        pack();
        setLocationRelativeTo(null); // center on screen
        setVisible(true);
    }

    /**
     * Factory method to create a JSlider configured for RGB values.
     */
    private JSlider createColorSlider() {
        JSlider slider = new JSlider(0, 255, 0);
        slider.setMajorTickSpacing(51);   // ticks at 0,51,102,153,204,255
        slider.setMinorTickSpacing(17);   // minor ticks
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(Color.WHITE);
        return slider;
    }

    /**
     * Reads slider values, updates labels, preview panel, and RGB text.
     */
    private void updateColorPreview() {
        int r = redSlider.getValue();
        int g = greenSlider.getValue();
        int b = blueSlider.getValue();

        // Update numeric labels
        redValueLabel.setText(String.valueOf(r));
        greenValueLabel.setText(String.valueOf(g));
        blueValueLabel.setText(String.valueOf(b));

        // Update preview swatch and RGB display
        Color selectedColor = new Color(r, g, b);
        previewPanel.setColor(selectedColor);
        rgbValueLabel.setText(String.format("RGB(%d, %d, %d)", r, g, b));
    }

    /**
     * Custom JPanel that fills itself with the specified color.
     */
    private static class ColorPreviewPanel extends JPanel {
        private Color currentColor = Color.BLACK;

        /**
         * Updates the panel's color and repaints.
         */
        void setColor(Color color) {
            this.currentColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(currentColor);
            // Draw a centered square swatch
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g.fillRect(x, y, size, size);
        }
    }

    /**
     * Launches the application on the Event Dispatch Thread for thread safety.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RGBSliderDemo::new);
    }
}
