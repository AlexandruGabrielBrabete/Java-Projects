import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A graphical simulation of drawing tickets from an urn:
 * - The urn is displayed as a grid of rectangles, one per ticket.
 * - There is 1 winning ticket (ID 0) and 1000 losing tickets.
 * - N players draw tickets simultaneously, either sequentially or with replacement.
 * - Upon drawing, the ticket disappears (or remains if with replacement) in the UI.
 * - The winner is highlighted with a green border and a dialog announcement.
 */
public class UrnSimulationApp extends JFrame {
    private final JTextField playerCountField;
    private final JComboBox<String> modeComboBox;
    private final JButton startButton;
    private final JTextArea logArea;
    private final UrnPanel urnPanel;

    private TicketPool ticketPool;
    private AtomicBoolean winnerFound;
    private List<Thread> playerThreads;

    public UrnSimulationApp() {
        super("Urn Drawing Simulation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // Control panel at top
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("Number of Players:"));
        playerCountField = new JTextField("10", 5);
        controlPanel.add(playerCountField);
        controlPanel.add(new JLabel("Mode:"));
        modeComboBox = new JComboBox<>(new String[]{"Sequential", "With Replacement"});
        controlPanel.add(modeComboBox);
        startButton = new JButton("Start");
        controlPanel.add(startButton);
        add(controlPanel, BorderLayout.NORTH);

        // Urn graphics panel in center
        urnPanel = new UrnPanel(1001);
        add(urnPanel, BorderLayout.CENTER);

        // Log area at bottom
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        startButton.addActionListener(e -> onStart());
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Handles start button click: initializes simulation and spawns player threads.
     */
    private void onStart() {
        logArea.setText("");
        int playerCount;
        try {
            playerCount = Integer.parseInt(playerCountField.getText().trim());
            if (playerCount <= 0) throw new NumberFormatException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid number of players.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean withReplacement = modeComboBox.getSelectedItem().equals("With Replacement");
        ticketPool = new TicketPool(1001, withReplacement);
        urnPanel.reset(1001);
        urnPanel.setReplacementMode(withReplacement);
        winnerFound = new AtomicBoolean(false);
        playerThreads = new ArrayList<>();
        startButton.setEnabled(false);

        // Create and start player threads
        for (int i = 1; i <= playerCount; i++) {
            Thread player = new Thread(new Drawer(i), "Player-" + i);
            playerThreads.add(player);
            player.start();
        }

        // Re-enable start button after all threads finish
        new Thread(() -> {
            for (Thread t : playerThreads) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }
            SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
        }).start();
    }

    /**
     * Runnable for each player: draws tickets until winner found or pool exhausted.
     */
    private class Drawer implements Runnable {
        private final int playerId;
        Drawer(int playerId) { this.playerId = playerId; }
        @Override
        public void run() {
            while (!winnerFound.get()) {
                TicketPool.DrawResult result = ticketPool.draw();
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Player " + playerId + " drew ticket " + result.ticketId
                        + (result.isWinner ? " (WINNER)!" : "") + "\n");
                    urnPanel.updateTicket(result.ticketId, result.isWinner);
                    if (result.isWinner) {
                        winnerFound.set(true);
                        JOptionPane.showMessageDialog(UrnSimulationApp.this,
                            "Player " + playerId + " wins!", "Winner", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                if (result.isWinner || !result.canDrawNext) break;
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Manages ticket pool and drawing logic with optional replacement.
     */
    static class TicketPool {
        static class DrawResult { int ticketId; boolean isWinner; boolean canDrawNext; }
        private final List<Integer> tickets;
        private final boolean withReplacement;
        private final Random random = new Random();

        TicketPool(int totalTickets, boolean withReplacement) {
            this.withReplacement = withReplacement;
            tickets = Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < totalTickets; i++) tickets.add(i);
        }

        DrawResult draw() {
            synchronized (tickets) {
                DrawResult res = new DrawResult();
                if (tickets.isEmpty()) {
                    res.canDrawNext = false;
                    return res;
                }
                int idx = random.nextInt(tickets.size());
                int id = tickets.get(idx);
                res.ticketId = id;
                res.isWinner = (id == 0);
                res.canDrawNext = true;
                if (!withReplacement) tickets.remove(idx);
                return res;
            }
        }
    }

    /**
     * Panel that graphically represents the urn as a grid of tickets.
     */
    static class UrnPanel extends JPanel {
        private boolean[] ticketPresent;
        private int winnerId = -1;
        private boolean withReplacement;
        private final int columns = 35;

        UrnPanel(int totalTickets) {
            reset(totalTickets);
            setPreferredSize(new Dimension(700, 400));
        }

        void reset(int totalTickets) {
            ticketPresent = new boolean[totalTickets];
            Arrays.fill(ticketPresent, true);
            winnerId = -1;
            repaint();
        }

        void setReplacementMode(boolean withReplacement) {
            this.withReplacement = withReplacement;
        }

        void updateTicket(int ticketId, boolean isWinner) {
            if (ticketId >= 0 && ticketId < ticketPresent.length && !withReplacement) {
                ticketPresent[ticketId] = false;
            }
            if (isWinner) winnerId = ticketId;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (ticketPresent == null) return;
            int total = ticketPresent.length;
            int rows = (total + columns - 1) / columns;
            int cellWidth = getWidth() / columns;
            int cellHeight = getHeight() / rows;
            for (int i = 0; i < total; i++) {
                int row = i / columns;
                int col = i % columns;
                int x = col * cellWidth;
                int y = row * cellHeight;
                g.setColor(ticketPresent[i] ? Color.CYAN : Color.LIGHT_GRAY);
                g.fillRect(x, y, cellWidth - 2, cellHeight - 2);
                if (i == winnerId) {
                    g.setColor(Color.GREEN);
                    g.drawRect(x, y, cellWidth - 2, cellHeight - 2);
                } else if (i == 0) {
                    g.setColor(Color.RED);
                    g.drawRect(x, y, cellWidth - 2, cellHeight - 2);
                } else {
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, cellWidth - 2, cellHeight - 2);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UrnSimulationApp().setVisible(true));
    }
}
