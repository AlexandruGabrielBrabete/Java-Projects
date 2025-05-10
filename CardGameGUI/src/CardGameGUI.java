import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CardGameGUI extends JFrame {
    private List<String> deck = new ArrayList<>();
    private List<CardComponent> extractedCards = new ArrayList<>();
    private JPanel cardPanel;
    private JButton drawButton;
    private JLabel statusLabel;
    private JLabel winLabel; // Winning message
    private int drawCount = 0;
    private String deckHash;
    private CardComponent animatedCard = null;
    private int animX = 100, animY = 80;
    private int targetX = 400, targetY = 80;

    public CardGameGUI() {
        setTitle("Animated Card Game");
        setSize(800, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initDeck();
        deckHash = hashDeck(deck);

        cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                // Green background
                setBackground(new Color(0x2e7d32));

                // Draw the deck
                if (!deck.isEmpty()) {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(100, 80, 80, 120, 15, 15);
                    g2.setColor(Color.BLACK);
                    g2.drawRoundRect(100, 80, 80, 120, 15, 15);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    g2.drawString("Deck", 110, 75);
                    g2.drawString("Remaining: " + deck.size(), 95, 220);
                }

                // Draw extracted cards
                int xOffset = 0;
                for (CardComponent card : extractedCards) {
                    card.paint(g2, 400 + xOffset, 80);
                    xOffset += 30;
                }

                // Draw animation
                if (animatedCard != null) {
                    animatedCard.paint(g2, animX, animY);
                }
            }
        };

        // Button to draw a card
        drawButton = new JButton("Draw Card");
        drawButton.addActionListener(e -> drawCard());

        // Label to show the number of extracted cards
        statusLabel = new JLabel("Cards Drawn: 0");

        // Winning message
        winLabel = new JLabel("🎉 You Won! The Game is Over 🎉", SwingConstants.CENTER);
        winLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        winLabel.setForeground(Color.YELLOW);
        winLabel.setBounds(200, 10, 400, 50);
        winLabel.setVisible(false); // Initially, it's hidden

        // Panel for the button and status label
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(drawButton, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        // Add the panel and winning message to the main window
        cardPanel.setLayout(null); // Allows free positioning
        cardPanel.add(winLabel);
        add(cardPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initDeck() {
        String[] suits = {"♠", "♥", "♦", "♣"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        for (String suit : suits) {
            for (String value : values) {
                deck.add(value + suit);
            }
        }
        Collections.shuffle(deck);
    }

    private void drawCard() {
        if (!hashDeck(deck).equals(deckHash)) {
            JOptionPane.showMessageDialog(this, "Deck compromised!", "Security", JOptionPane.ERROR_MESSAGE);
            drawButton.setEnabled(false);
            return;
        }

        if (deck.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The deck is empty!", "Info", JOptionPane.INFORMATION_MESSAGE);
            drawButton.setEnabled(false);
            return;
        }

        String card = deck.remove(new Random().nextInt(deck.size()));
        drawCount++;
        statusLabel.setText("Cards Drawn: " + drawCount);

        animatedCard = new CardComponent(card);
        animX = 100;
        animY = 80;

        targetX = 400 + (extractedCards.size() * 30);
        targetY = 80;

        // Check if it's a ♣ > 8 immediately, but animate first
        boolean isClubOver8 = isClubOverEight(card);
        if (isClubOver8) drawButton.setEnabled(false);

        animateCard(() -> {
            extractedCards.add(animatedCard);
            animatedCard = null;
            cardPanel.repaint();

            // If it's a ♣ with value > 8, show the winning message
            if (isClubOver8) {
                Timer t = new Timer(300, e -> {
                    winLabel.setVisible(true); // Show the winning message
                    cardPanel.repaint();
                });
                t.setRepeats(false);
                t.start();
            }
        });

        deckHash = hashDeck(deck);
    }

    private boolean isClubOverEight(String card) {
        String value = card.substring(0, card.length() - 1);
        String suit = card.substring(card.length() - 1);
        return suit.equals("♣") && getCardNumericValue(value) > 8;
    }

    private void animateCard(Runnable onFinish) {
        Timer timer = new Timer(15, null);
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int dx = (targetX - animX);
                int dy = (targetY - animY);
                if (Math.abs(dx) < 2 && Math.abs(dy) < 2) {
                    ((Timer) e.getSource()).stop();
                    onFinish.run();
                } else {
                    animX += dx / 5;
                    animY += dy / 5;
                    cardPanel.repaint();
                }
            }
        });
        timer.start();
    }

    private int getCardNumericValue(String value) {
        return switch (value) {
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            case "A" -> 14;
            default -> Integer.parseInt(value);
        };
    }

    private String hashDeck(List<String> deck) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String deckString = String.join(",", deck);
            byte[] hash = digest.digest(deckString.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash)
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CardGameGUI().setVisible(true));
    }
}

class CardComponent {
    private final String card;

    public CardComponent(String card) {
        this.card = card;
    }

    public void paint(Graphics2D g2, int x, int y) {
        int width = 80, height = 120;
        String value = card.substring(0, card.length() - 1);
        String suit = card.substring(card.length() - 1);
        boolean red = suit.equals("♥") || suit.equals("♦");

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, width, height, 15, 15);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x, y, width, height, 15, 15);
        g2.setColor(red ? Color.RED : Color.BLACK);
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        g2.drawString(value + suit, x + 20, y + 65);
    }
}
