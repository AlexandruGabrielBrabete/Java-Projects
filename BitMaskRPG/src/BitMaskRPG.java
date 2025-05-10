import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class BitMaskRPG {
    private static JTextField inputField1, inputField2;
    private static JTextArea outputArea, dialogueArea;
    private static int player1Health = 100, player2Health = 100;
    private static int player1AttackPower = 20, player2AttackPower = 20;
    private static int player1Defense = 0, player2Defense = 0;
    private static int player1Armor = 0, player2Armor = 0;
    private static String player1Name = "Bitwise Warrior";
    private static String player2Name = "Bitwise Opponent";
    private static boolean isBattleOngoing = false;
    private static JProgressBar player1HealthBar, player2HealthBar;

    private static ArrayList<String> player1Inventory = new ArrayList<>();
    private static ArrayList<String> player2Inventory = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("BitMask RPG Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputField1 = new JTextField(10);
        inputField2 = new JTextField(10);

        inputPanel.add(new JLabel("Player 1 Number:"));
        inputPanel.add(inputField1);
        inputPanel.add(new JLabel("Player 2 Number:"));
        inputPanel.add(inputField2);

        JButton submitBtn = new JButton("Bitwise Ops");
        JButton battleBtn = new JButton("Start Battle");
        JButton resetBtn = new JButton("Reset Game");
        inputPanel.add(submitBtn);
        inputPanel.add(battleBtn);
        inputPanel.add(resetBtn);

        // Output area
        outputArea = new JTextArea(15, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Dialogue / status area
        dialogueArea = new JTextArea(6, 40);
        dialogueArea.setEditable(false);
        JScrollPane dialogueScroll = new JScrollPane(dialogueArea);

        // Health bars
        player1HealthBar = new JProgressBar(0, 100);
        player2HealthBar = new JProgressBar(0, 100);
        player1HealthBar.setValue(player1Health);
        player2HealthBar.setValue(player2Health);
        player1HealthBar.setStringPainted(true);
        player2HealthBar.setStringPainted(true);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(dialogueScroll, BorderLayout.SOUTH);
        frame.add(player1HealthBar, BorderLayout.WEST);
        frame.add(player2HealthBar, BorderLayout.EAST);

        // Action Listeners
        submitBtn.addActionListener(e -> handleBitwiseOperations());
        battleBtn.addActionListener(e -> startBattle());
        resetBtn.addActionListener(e -> resetGame());

        frame.setVisible(true);
    }

    private static void handleBitwiseOperations() {
        String text1 = inputField1.getText();
        String text2 = inputField2.getText();

        try {
            int num1 = Integer.parseInt(text1);
            int num2 = Integer.parseInt(text2);
            StringBuilder sb = new StringBuilder();

            sb.append("Player 1: ").append(num1).append("\n");
            sb.append("Player 2: ").append(num2).append("\n\n");

            sb.append("Bitwise AND: ").append(num1 & num2).append("\n");
            sb.append("Bitwise OR : ").append(num1 | num2).append("\n");
            sb.append("Bitwise XOR: ").append(num1 ^ num2).append("\n");
            sb.append("Bitwise NOT (P1): ").append(~num1).append("\n");

            // Random item reward
            if (new Random().nextBoolean()) {
                addItemToInventory("Healing Potion", 1);
            } else {
                addItemToInventory("Armor", 2);
            }

            outputArea.setText(sb.toString());
            updateDialogue("Bitwise powers awaken! The battle begins...");
        } catch (NumberFormatException e) {
            outputArea.setText("⚠️ Please enter valid integers.");
        }
    }

    private static void addItemToInventory(String item, int playerNum) {
        if (playerNum == 1) {
            player1Inventory.add(item);
            updateDialogue(player1Name + " found: " + item + " (Inventory: " + player1Inventory + ")");
        } else {
            player2Inventory.add(item);
            updateDialogue(player2Name + " found: " + item + " (Inventory: " + player2Inventory + ")");
        }
    }

    private static void startBattle() {
        if (isBattleOngoing) {
            updateDialogue("Battle already in progress!");
            return;
        }

        isBattleOngoing = true;
        updateDialogue("⚔️ Battle starts between " + player1Name + " and " + player2Name + "!");

        Timer battleTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (player1Health > 0 && player2Health > 0) {
                    int damageToP1 = calculateDamage(player2AttackPower, player1Defense);
                    int damageToP2 = calculateDamage(player1AttackPower, player2Defense);

                    player1Health = Math.max(0, player1Health - damageToP1);
                    player2Health = Math.max(0, player2Health - damageToP2);

                    player1HealthBar.setValue(player1Health);
                    player2HealthBar.setValue(player2Health);

                    updateDialogue(player1Name + " loses " + damageToP1 + " HP. Now: " + player1Health);
                    updateDialogue(player2Name + " loses " + damageToP2 + " HP. Now: " + player2Health);

                    if (player1Health == 0 || player2Health == 0) {
                        ((Timer) evt.getSource()).stop();
                        endBattle();
                    }
                }
            }
        });

        battleTimer.start();
    }

    private static int calculateDamage(int attack, int defense) {
        return Math.max(1, attack - defense);
    }

    private static void endBattle() {
        if (player1Health == 0) {
            updateDialogue("💀 " + player1Name + " has fallen!");
            player2Armor += 10;
            updateDialogue(player2Name + " gains +10 armor! Total Armor: " + player2Armor);
        } else {
            updateDialogue("💀 " + player2Name + " has fallen!");
            player1Armor += 10;
            updateDialogue(player1Name + " gains +10 armor! Total Armor: " + player1Armor);
        }

        isBattleOngoing = false;
    }

    private static void resetGame() {
        player1Health = 100;
        player2Health = 100;
        player1AttackPower = 20;
        player2AttackPower = 20;
        player1Defense = 0;
        player2Defense = 0;
        player1Armor = 0;
        player2Armor = 0;
        player1Inventory.clear();
        player2Inventory.clear();

        player1HealthBar.setValue(player1Health);
        player2HealthBar.setValue(player2Health);

        outputArea.setText("");
        updateDialogue("🔄 Game reset. Ready for a new bit-battle!");
        isBattleOngoing = false;
    }

    private static void updateDialogue(String message) {
        String current = dialogueArea.getText();
        dialogueArea.setText(current + message + "\n");
    }
}
