import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;

/**
 * Aplicatie Frame care permite:
 * 1. Alegerea unei imagini de pe sistem si afisarea ei scalate pentru a se incadra in zona de vizualizare.
 * 2. Alegerea fontului (familie si marime) si a culorii textului pentru descrierea imaginii (numele fisierului).
 */
public class Main extends JFrame {
    private BufferedImage img;
    private final ImagePanel imagePanel;
    private final JLabel filenameLabel;
    private Font labelFont = new Font("Arial", Font.BOLD, 16);
    private Color textColor = new Color(30,144,255);

    public Main() {
        super("Viewer Imagine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5,5));

        // Control Panel
        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadBtn = new JButton("Alege imagine");
        JButton fontBtn = new JButton("Alege font si culoare");
        control.add(loadBtn);
        control.add(fontBtn);
        add(control, BorderLayout.NORTH);

        // Image display panel
        imagePanel = new ImagePanel();
        add(imagePanel, BorderLayout.CENTER);

        // Filename label
        filenameLabel = new JLabel("Nicio imagine incarcata");
        filenameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        filenameLabel.setFont(labelFont);
        filenameLabel.setForeground(textColor);
        add(filenameLabel, BorderLayout.SOUTH);

        // Load image action
        loadBtn.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    img = ImageIO.read(file);
                    imagePanel.setImage(img);
                    filenameLabel.setText(file.getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Eroare incarcare imagine: " + ex.getMessage(),
                        "Eroare", JOptionPane.ERROR_MESSAGE);
                }
                pack(); // adjust to preferred sizes
            }
        });

        // Font and color chooser
        fontBtn.addActionListener(ae -> showFontDialog());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showFontDialog() {
        JDialog dialog = new JDialog(this, "Alege Font si Culoare", true);
        dialog.setLayout(new BorderLayout(5,5));

        // Font chooser panel
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] families = ge.getAvailableFontFamilyNames();
        JComboBox<String> fontBox = new JComboBox<>(families);
        fontBox.setSelectedItem(labelFont.getFamily());
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(labelFont.getSize(), 8, 72, 1));
        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Font: ")); top.add(fontBox);
        top.add(new JLabel("Marime: ")); top.add(sizeSpinner);
        dialog.add(top, BorderLayout.NORTH);

        // Color chooser
        JColorChooser colorChooser = new JColorChooser(textColor);
        dialog.add(colorChooser, BorderLayout.CENTER);

        // Buttons
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        btnP.add(okBtn); btnP.add(cancelBtn);
        dialog.add(btnP, BorderLayout.SOUTH);

        okBtn.addActionListener(ae -> {
            String family = (String) fontBox.getSelectedItem();
            int size = (Integer) sizeSpinner.getValue();
            labelFont = new Font(family, Font.PLAIN, size);
            textColor = colorChooser.getColor();
            filenameLabel.setFont(labelFont);
            filenameLabel.setForeground(textColor);
            dialog.dispose();
        });
        cancelBtn.addActionListener(ae -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Panel custom care afiseaza imaginea scalata pentru a se incadra in dimensiuni.
     */
    static class ImagePanel extends JPanel {
        private BufferedImage img;

        public void setImage(BufferedImage img) {
            this.img = img;
            // set preferred size to image original or limit
            setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                int panelW = getWidth();
                int panelH = getHeight();
                double imgW = img.getWidth();
                double imgH = img.getHeight();
                double scale = Math.min(panelW/imgW, panelH/imgH);
                int drawW = (int)(imgW * scale);
                int drawH = (int)(imgH * scale);
                int x = (panelW - drawW)/2;
                int y = (panelH - drawH)/2;
                g.drawImage(img, x, y, drawW, drawH, null);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
