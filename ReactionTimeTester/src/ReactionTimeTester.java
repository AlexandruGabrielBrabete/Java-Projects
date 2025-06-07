import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A GUI application to measure user reaction time to colored circles.
 * Randomly displays 10 circles in red or black. The user clicks the
 * corresponding button, and at the end, the app shows the number
 * of correct responses and the average reaction time.
 */
public class ReactionTimeTester extends JFrame {
    // Panel to display the colored circle
    private final CirclePanel circlePanel;
    // Buttons for user response
    private final JButton redButton;
    private final JButton blackButton;
    // Label to show instructions and progress
    private final JLabel infoLabel;

    // Sequence of rounds: true = red, false = black
    private final List<Boolean> expectedSequence;
    // Recorded reaction times for correct answers
    private final List<Long> reactionTimes;

    private int currentRound;
    private int correctResponses;
    private long circleShownTimestamp;

    /**
     * Constructor: sets up UI, initializes test sequence and starts the first round.
     */
    public ReactionTimeTester() {
        super("Reaction Time Test");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Circle display panel configuration
        circlePanel = new CirclePanel();
        circlePanel.setPreferredSize(new Dimension(200, 200));
        add(circlePanel, BorderLayout.CENTER);

        // Buttons panel layout
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        redButton = new JButton("RED");
        blackButton = new JButton("BLACK");
        buttonsPanel.add(redButton);
        buttonsPanel.add(blackButton);
        add(buttonsPanel, BorderLayout.SOUTH);

        // Info label configuration
        infoLabel = new JLabel("Click the button matching the circle color", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(infoLabel, BorderLayout.NORTH);

        // Initialize sequences and statistics
        expectedSequence = new ArrayList<>();
        reactionTimes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            expectedSequence.add(random.nextBoolean()); // true = red, false = black
        }
        currentRound = 0;
        correctResponses = 0;

        // Register button listeners
        redButton.addActionListener(this::handleUserResponse);
        blackButton.addActionListener(this::handleUserResponse);

        pack();
        setLocationRelativeTo(null); // center window
        setVisible(true);

        // Start the first round
        SwingUtilities.invokeLater(this::showNextCircle);
    }

    /**
     * Handles button clicks, measures reaction time, and advances rounds.
     */
    private void handleUserResponse(ActionEvent e) {
        long responseTime = System.currentTimeMillis() - circleShownTimestamp;
        boolean expectedRed = expectedSequence.get(currentRound);
        boolean clickedRed = (e.getSource() == redButton);
        if (clickedRed == expectedRed) {
            correctResponses++;
            reactionTimes.add(responseTime);
        }
        currentRound++;
        if (currentRound < expectedSequence.size()) {
            showNextCircle();
        } else {
            finishTest();
        }
    }

    /**
     * Displays the next circle in the sequence and updates the info label.
     */
    private void showNextCircle() {
        boolean isRed = expectedSequence.get(currentRound);
        circlePanel.updateColor(isRed ? Color.RED : Color.BLACK);
        infoLabel.setText(String.format("Round %d of %d", currentRound + 1, expectedSequence.size()));
        circleShownTimestamp = System.currentTimeMillis();
    }

    /**
     * Computes final statistics and shows results in a dialog.
     */
    private void finishTest() {
        // Disable buttons to prevent further input
        redButton.setEnabled(false);
        blackButton.setEnabled(false);

        // Calculate average reaction time
        long total = 0;
        for (long t : reactionTimes) {
            total += t;
        }
        long average = reactionTimes.isEmpty() ? 0 : total / reactionTimes.size();

        // Display final results
        infoLabel.setText("Test Completed");
        String message = String.format(
            "Correct Responses: %d/%d, Average Time: %d ms",
            correctResponses, expectedSequence.size(), average
        );
        JOptionPane.showMessageDialog(this, message, "Results", JOptionPane.INFORMATION_MESSAGE);

        // Clear the circle
        circlePanel.updateColor(getBackground());
    }

    /**
     * Inner class: a panel that draws a filled circle of the current color.
     */
    private static class CirclePanel extends JPanel {
        private Color currentColor = Color.LIGHT_GRAY;

        /**
         * Updates the panel's circle color and repaints.
         */
        public void updateColor(Color color) {
            this.currentColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int diameter = Math.min(getWidth(), getHeight()) - 20;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g.setColor(currentColor);
            g.fillOval(x, y, diameter, diameter);
        }
    }

    /**
     * Launches the application on the Swing Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReactionTimeTester::new);
    }
}
