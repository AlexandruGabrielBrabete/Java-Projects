import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Swing-based calculator application similar to the Windows 10 calculator.
 * Uses a GridLayout for the buttons and a JTextField for the display.
 */
public class Calculator extends JFrame implements ActionListener {
    // Display field for current input or result
    private final JTextField displayField;
    // Buffer collecting digits and decimal point entered by the user
    private StringBuilder inputBuffer = new StringBuilder();
    // Running total or "accumulator" for calculations
    private double accumulator = 0;
    // Operator pending to be applied between accumulator and next input
    private String pendingOperator = "=";

    /**
     * Constructor: sets up window properties, display, and buttons.
     */
    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // Initialize display field
        displayField = new JTextField("0");
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);
        displayField.setEditable(false);
        displayField.setFont(new Font("Arial", Font.BOLD, 24));
        add(displayField, BorderLayout.NORTH);

        // Create panel for buttons using a 5x4 grid
        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        String[] labels = {
            "C", "±", "%", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "=", ""
        };
        for (String label : labels) {
            if (label.isEmpty()) {
                buttonPanel.add(new JLabel()); // empty placeholder
            } else {
                JButton button = new JButton(label);
                button.setFont(new Font("Arial", Font.BOLD, 20));
                button.addActionListener(this);
                buttonPanel.add(button);
            }
        }
        add(buttonPanel, BorderLayout.CENTER);

        setSize(300, 400);
        setLocationRelativeTo(null); // center window
        setVisible(true);
    }

    /**
     * Handles button presses and delegates to appropriate logic.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "C":
                // Clear all state and reset display
                inputBuffer.setLength(0);
                accumulator = 0;
                pendingOperator = "=";
                displayField.setText("0");
                break;
            case "±":
                // Toggle sign of current input
                if (inputBuffer.length() > 0) {
                    if (inputBuffer.charAt(0) == '-') {
                        inputBuffer.deleteCharAt(0);
                    } else {
                        inputBuffer.insert(0, '-');
                    }
                    displayField.setText(inputBuffer.toString());
                }
                break;
            case "%":
                // Percentage operation: treat next operator as percentage
                processPendingOperator("%");
                break;
            case "+": case "-": case "*": case "/": case "=":
                // Arithmetic operations
                processPendingOperator(command);
                break;
            case ".":
                // Decimal point: ensure only one per input
                if (!inputBuffer.toString().contains(".")) {
                    if (inputBuffer.length() == 0) {
                        inputBuffer.append("0");
                    }
                    inputBuffer.append('.');
                    displayField.setText(inputBuffer.toString());
                }
                break;
            default:
                // Digit buttons: append to input buffer
                inputBuffer.append(command);
                displayField.setText(inputBuffer.toString());
                break;
        }
    }

    /**
     * Applies the pending operator to the accumulator and current input,
     * updates the display, and prepares for the next operation.
     *
     * @param operator the operator just pressed
     */
    private void processPendingOperator(String operator) {
        // Parse current input or use accumulator if none
        double inputValue = (inputBuffer.length() > 0)
            ? Double.parseDouble(inputBuffer.toString())
            : accumulator;

        // Perform calculation based on last operator
        switch (pendingOperator) {
            case "=":
                accumulator = inputValue;
                break;
            case "+":
                accumulator += inputValue;
                break;
            case "-":
                accumulator -= inputValue;
                break;
            case "*":
                accumulator *= inputValue;
                break;
            case "/":
                // Avoid division by zero
                accumulator = (inputValue != 0) ? accumulator / inputValue : 0;
                break;
            case "%":
                // Percentage: accumulator times inputValue percent
                accumulator = accumulator * inputValue / 100;
                break;
        }

        // Update display and reset input buffer
        displayField.setText(String.valueOf(accumulator));
        inputBuffer.setLength(0);
        pendingOperator = operator;
    }

    /**
     * Entry point: launches the calculator application on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Calculator::new);
    }
}
