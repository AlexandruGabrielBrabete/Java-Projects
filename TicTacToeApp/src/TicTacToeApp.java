/*Beta version:
Currently, we have the following features:

1. A graphical interface;
2. A menu to select the number of players;
3. A menu for the type of board;
4. Functional game modes, with animations and appropriate messages;
5. A basic AI that gradually learns.

Currently, we do not have the following features:

1. Advanced security algorithms (SQL injection, etc.);
2. "Established" AI algorithms: a linear regression approach seems more appropriate;
3. Validation algorithms;
4. Data encryption algorithms for AI and game data in separate files (we have a separate AES-256 algorithm model, to be implemented);
5. Login methods (both admin and user with email generator, email alerts);
6. A more user-friendly interface (score tables, users, extra buttons, player selection, AI with difficulty levels);
7. Creating databases to store information (probably using Sqlite).*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Entry point: start the game mode selection menu first (depending on how many players play: 1 or 2)
public class TicTacToeApp {
public static void main(String[] args) {
SwingUtilities.invokeLater(() -> new GameModeMenu());
}
}

// Game Mode Selection Menu: Single Player or Contra AI
class GameModeMenu extends JFrame {
public GameModeMenu() {
setTitle("Selectați tipul de joc");
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
JButton btnSingle = new JButton("Single Player");
JButton btnAI = new JButton("Versus AI");
panel.add(btnSingle);
panel.add(btnAI);
add(panel);
pack();
setLocationRelativeTo(null);
setVisible(true);
    btnSingle.addActionListener(e -> {
        new MainMenu("HUMAN");
        dispose();
    });
    
    btnAI.addActionListener(e -> {
        new MainMenu("AI");
        dispose();
    });
}


}

/*/ Menu for selecting the type of board (3x3, 5x5, 7x7, 9x9, and Ultimate) 
GameMode is passed on to let you know in the GameWindow if it's playing with AI 
Ultimate X and 0 currently don't work against AI (the button isn't removed in that mode because I'll add the mode) */
class MainMenu extends JFrame {
private String gameMode;


public MainMenu(String gameMode) {
    this.gameMode = gameMode;
    setTitle("Selectați tipul de tablă");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
    JButton btn3x3 = new JButton("3 x 3 table");
    JButton btn5x5 = new JButton("5 x 5 table");
    JButton btn7x7 = new JButton("7 x 7 table");
    JButton btn9x9 = new JButton("9 x 9 table");
    JButton btnUltimate = new JButton("Ultimate TicTacToe");
    
    panel.add(btn3x3);
    panel.add(btn5x5);
    panel.add(btn7x7);
    panel.add(btn9x9);
    panel.add(btnUltimate);
    
    add(panel);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
    
    btn3x3.addActionListener(e -> {
        new GameWindow(3, gameMode);
        dispose();
    });
    btn5x5.addActionListener(e -> {
        new GameWindow(5, gameMode);
        dispose();
    });
    btn7x7.addActionListener(e -> {
        new GameWindow(7, gameMode);
        dispose();
    });
    btn9x9.addActionListener(e -> {
        new GameWindow(9, gameMode);
        dispose();
    });
    btnUltimate.addActionListener(e -> {
        new UltimateTicTacToeWindow(gameMode);
        dispose();
    });
}


}

/*Classic game window (variable size board) If gameMode is "AI", 
the AI move logic is activated (the AI player plays with "O", automatically) */
class GameWindow extends JFrame {
private int boardSize;
private JButton[][] buttons;
private boolean playerXTurn = true; // In AI mode, the human player plays with "X"
private int movesCount = 0;
private String winType = "";
private int winIndex = -1;
private boolean aiMode;


public GameWindow(int boardSize, String gameMode) {
    this.boardSize = boardSize;
    this.aiMode = gameMode.equals("AI");
    setTitle("TicTacToeApp - Table " + boardSize + " x " + boardSize);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 600);
    setLocationRelativeTo(null);
    
    buttons = new JButton[boardSize][boardSize];
    setLayout(new GridLayout(boardSize, boardSize));
    
    for (int i = 0; i < boardSize; i++) {
        for (int j = 0; j < boardSize; j++) {
            buttons[i][j] = new JButton("");
            buttons[i][j].setFont(new Font("Arial", Font.BOLD, 32));
            int row = i, col = j;
            buttons[i][j].addActionListener(e -> {
                // If the cell is occupied, ignore
                if (!buttons[row][col].getText().equals("")) return;
                // In AI mode, only the human player (X) clicks
                if (aiMode && !playerXTurn) return;
                buttonClicked(row, col);
            });
            add(buttons[i][j]);
        }
    }
    setVisible(true);
}

private void buttonClicked(int row, int col) {
    if (!buttons[row][col].getText().equals("")) return;
    buttons[row][col].setText(playerXTurn ? "X" : "O");
    movesCount++;
    
    if (checkForWinner(row, col)) {
        animateWinningLine(playerXTurn ? "X" : "O");
    } else if (movesCount == boardSize * boardSize) {
        JOptionPane.showMessageDialog(this, "Draw! There is no winner.");
        new MainMenu(aiMode ? "AI" : "HUMAN");
        dispose();
    } else {
        playerXTurn = !playerXTurn;
        // If it plays with AI and now it's AI's turn ("O"), the AI move is triggered
        if (aiMode && !playerXTurn) {
            Timer timer = new Timer(500, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    makeAIMove();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
}

// The method that checks if the player who just moved has won
private boolean checkForWinner(int row, int col) {
    String current = playerXTurn ? "X" : "O";
    boolean win = true;
    // Check Row
    for (int j = 0; j < boardSize; j++) {
        if (!buttons[row][j].getText().equals(current)) {
            win = false;
            break;
        }
    }
    if (win) { winType = "ROW"; winIndex = row; return true; }
    
    // Check column
    win = true;
    for (int i = 0; i < boardSize; i++) {
        if (!buttons[i][col].getText().equals(current)) {
            win = false;
            break;
        }
    }
    if (win) { winType = "COL"; winIndex = col; return true; }
    
    // Main Diagonal Check
    if (row == col) {
        win = true;
        for (int i = 0; i < boardSize; i++) {
            if (!buttons[i][i].getText().equals(current)) {
                win = false;
                break;
            }
        }
        if (win) { winType = "DIAG_MAIN"; return true; }
    }
    
    // Check Secondary Diagonal
    if (row + col == boardSize - 1) {
        win = true;
        for (int i = 0; i < boardSize; i++) {
            if (!buttons[i][boardSize - 1 - i].getText().equals(current)) {
                win = false;
                break;
            }
        }
        if (win) { winType = "DIAG_SEC"; return true; }
    }
    return false;
}

// Method for the winning line animation (at the end, the menu is resumed)
private void animateWinningLine(String winningSymbol) {
    WinningLineGlassPane glassPane = new WinningLineGlassPane();
    setGlassPane(glassPane);
    glassPane.setVisible(true);
    glassPane.startAnimation(() -> {
        JOptionPane.showMessageDialog(this, "Player " + winningSymbol + " won the game!");
        new MainMenu(aiMode ? "AI" : "HUMAN");
        dispose();
    });
}

// AI Moving Method
private void makeAIMove() {
    int[] move = AIModule.chooseMove(buttons, boardSize, "O", "X");
    if (move != null) {
        buttonClicked(move[0], move[1]);
    }
}

// Inner class for winning line animation
private class WinningLineGlassPane extends JComponent {
    private double progress = 0;
    private Timer timer;
    
    public void startAnimation(Runnable onFinish) {
        int delay = 30;
        int duration = 1000;
        int steps = duration / delay;
        timer = new Timer(delay, new ActionListener() {
            int step = 0;
            public void actionPerformed(ActionEvent e) {
                step++;
                progress = (double) step / steps;
                repaint();
                if (step >= steps) {
                    timer.stop();
                    onFinish.run();
                }
            }
        });
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (progress <= 0) return;
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(4));
        Point start = null;
        Point end = null;
        if (winType.equals("ROW")) {
            start = SwingUtilities.convertPoint(buttons[winIndex][0],
                    buttons[winIndex][0].getWidth()/2, buttons[winIndex][0].getHeight()/2, this);
            end = SwingUtilities.convertPoint(buttons[winIndex][boardSize-1],
                    buttons[winIndex][boardSize-1].getWidth()/2, buttons[winIndex][boardSize-1].getHeight()/2, this);
        } else if (winType.equals("COL")) {
            start = SwingUtilities.convertPoint(buttons[0][winIndex],
                    buttons[0][winIndex].getWidth()/2, buttons[0][winIndex].getHeight()/2, this);
            end = SwingUtilities.convertPoint(buttons[boardSize-1][winIndex],
                    buttons[boardSize-1][winIndex].getWidth()/2, buttons[boardSize-1][winIndex].getHeight()/2, this);
        } else if (winType.equals("DIAG_MAIN")) {
            start = SwingUtilities.convertPoint(buttons[0][0],
                    buttons[0][0].getWidth()/2, buttons[0][0].getHeight()/2, this);
            end = SwingUtilities.convertPoint(buttons[boardSize-1][boardSize-1],
                    buttons[boardSize-1][boardSize-1].getWidth()/2, buttons[boardSize-1][boardSize-1].getHeight()/2, this);
        } else if (winType.equals("DIAG_SEC")) {
            start = SwingUtilities.convertPoint(buttons[0][boardSize-1],
                    buttons[0][boardSize-1].getWidth()/2, buttons[0][boardSize-1].getHeight()/2, this);
            end = SwingUtilities.convertPoint(buttons[boardSize-1][0],
                    buttons[boardSize-1][0].getWidth()/2, buttons[boardSize-1][0].getHeight()/2, this);
        }
        if (start != null && end != null) {
            int x = start.x;
            int y = start.y;
            int x2 = (int)(start.x + (end.x - start.x) * progress);
            int y2 = (int)(start.y + (end.y - start.y) * progress);
            g2d.drawLine(x, y, x2, y2);
        }
        g2d.dispose();
    }
}


}

// Example: Window for Ultimate TicTacToe (I've also modified it here to get gameMode)
class UltimateTicTacToeWindow extends JFrame {
private MiniBoard[][] boards = new MiniBoard[3][3];
private String currentPlayer = "X";
private int activeBoardRow = -1, activeBoardCol = -1;
private boolean aiMode;


public UltimateTicTacToeWindow(String gameMode) {
    this.aiMode = gameMode.equals("AI");
    setTitle("Ultimate TicTacToe");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 800);
    setLocationRelativeTo(null);
    setLayout(new GridLayout(3, 3, 5, 5));
    
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            boards[i][j] = new MiniBoard(i, j, this);
            add(boards[i][j]);
        }
    }
    updateActiveBoardHighlights();
    pack();
    setVisible(true);
}

public void cellClicked(int boardRow, int boardCol, int cellRow, int cellCol, JButton btn) {
    MiniBoard board = boards[boardRow][boardCol];
    if (activeBoardRow != -1 && activeBoardCol != -1) {
        if (boardRow != activeBoardRow || boardCol != activeBoardCol) {
            if (!isBoardPlayable(boards[activeBoardRow][activeBoardCol])) {
                // continue
            } else {
                return;
            }
        }
    }
    if (!btn.getText().equals("") || board.isFinished()) return;
    // In AI mode, we assume that the human player is playing with "X" (WE HAVE PROBLEMS WITH THIS MODE IN AI, AT THE MOMENT)
    btn.setText(currentPlayer);
    board.incrementMoves();
    board.checkWinner();
    
    if (checkOverallWinner()) {
        JOptionPane.showMessageDialog(this, "Player " + currentPlayer + "won the Ultimate game!");
        new MainMenu(aiMode ? "AI" : "HUMAN");
        dispose();
        return;
    }
    
    if (!isBoardPlayable(boards[cellRow][cellCol])) {
        activeBoardRow = -1;
        activeBoardCol = -1;
    } else {
        activeBoardRow = cellRow;
        activeBoardCol = cellCol;
    }
    currentPlayer = currentPlayer.equals("X") ? "O" : "X";
    updateActiveBoardHighlights();
    
    // If it's AI mode and it's AI's turn, it triggers the AI move after a little delay
    if (aiMode && currentPlayer.equals("O")) {
        Timer timer = new Timer(500, e -> {
            int[] move = AIModule.chooseMoveInUltimate(boards, currentPlayer);
            if (move != null) {
                // move[0]=boardRow, move[1]=boardCol, move[2]=cellRow, move[3]=cellCol 
                boards[move[0]][move[1]].simulateClick(move[2], move[3]);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}

private boolean isBoardPlayable(MiniBoard board) {
    return !board.isFinished();
}

private void updateActiveBoardHighlights() {
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            boards[i][j].setBorder(
                (activeBoardRow == -1 || (i == activeBoardRow && j == activeBoardCol))
                ? BorderFactory.createLineBorder(Color.GREEN, 4)
                : BorderFactory.createLineBorder(Color.GRAY, 1)
            );
        }
    }
}

private boolean checkOverallWinner() {
    String[][] wins = new String[3][3];
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            wins[i][j] = boards[i][j].getWinner();
        }
    }
    for (int i = 0; i < 3; i++) {
        if (wins[i][0] != null && wins[i][0].equals(wins[i][1]) && wins[i][1].equals(wins[i][2]))
            return true;
        if (wins[0][i] != null && wins[0][i].equals(wins[1][i]) && wins[1][i].equals(wins[2][i]))
            return true;
    }
    if (wins[0][0] != null && wins[0][0].equals(wins[1][1]) && wins[1][1].equals(wins[2][2]))
        return true;
    if (wins[0][2] != null && wins[0][2].equals(wins[1][1]) && wins[1][1].equals(wins[2][0]))
        return true;
    return false;
}


}

// Class for the Ultimate TicTacToe Mini-Board
class MiniBoard extends JPanel {
JButton[][] cells = new JButton[3][3];
private String winner = null;
private int movesCount = 0;
private int boardRow, boardCol;
private UltimateTicTacToeWindow parent;


public MiniBoard(int boardRow, int boardCol, UltimateTicTacToeWindow parent) {
    this.boardRow = boardRow;
    this.boardCol = boardCol;
    this.parent = parent;
    setLayout(new GridLayout(3, 3));
    setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            cells[i][j] = new JButton("");
            cells[i][j].setFont(new Font("Arial", Font.BOLD, 20));
            final int r = i, c = j;
            cells[i][j].addActionListener(e -> parent.cellClicked(boardRow, boardCol, r, c, cells[r][c]));
            add(cells[i][j]);
        }
    }
}

public void simulateClick(int cellRow, int cellCol) {
    //Simulates a click on a cell, used by AI
    JButton btn = cells[cellRow][cellCol];
    if(btn.getText().equals("")) {
        btn.doClick();
    }
}

public void incrementMoves() {
    movesCount++;
}

public void checkWinner() {
    if (winner != null) return;
    String[][] board = new String[3][3];
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            board[i][j] = cells[i][j].getText();
        }
    }
    for (int i = 0; i < 3; i++) {
        if (!board[i][0].equals("") && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) {
            winner = board[i][0];
            highlightWinningCells(new int[][]{{i,0}, {i,1}, {i,2}});
            return;
        }
        if (!board[0][i].equals("") && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])) {
            winner = board[0][i];
            highlightWinningCells(new int[][]{{0,i}, {1,i}, {2,i}});
            return;
        }
    }
    if (!board[0][0].equals("") && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) {
        winner = board[0][0];
        highlightWinningCells(new int[][]{{0,0}, {1,1}, {2,2}});
        return;
    }
    if (!board[0][2].equals("") && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
        winner = board[0][2];
        highlightWinningCells(new int[][]{{0,2}, {1,1}, {2,0}});
        return;
    }
}

private void highlightWinningCells(int[][] coords) {
    for (int[] coord : coords) {
        cells[coord[0]][coord[1]].setBackground(Color.YELLOW);
    }
}

public boolean isFinished() {
    return winner != null || movesCount == 9;
}

public String getWinner() {
    return winner;
}


}

/*AI module: a simple strategy that looks for a winning move, then blocking, otherwise it chooses randomly. 
The learningFactor variable is ready for the extension of the "learning" algorithm. (Basal AI, we don't have epsilon values) */
class AIModule {
private static Random rand = new Random();
public static double learningFactor = 0.1;


public static int[] chooseMove(JButton[][] buttons, int boardSize, String aiSymbol, String opponentSymbol) {
    // Step 1. Check for a winning move
    for (int i = 0; i < boardSize; i++) {
        for (int j = 0; j < boardSize; j++) {
            if (buttons[i][j].getText().equals("")) {
                if (isWinningMove(buttons, boardSize, i, j, aiSymbol)) {
                    return new int[]{i, j};
                }
            }
        }
    }
    // Step 2. Check if an opponent's winning move should be blocked
    for (int i = 0; i < boardSize; i++) {
        for (int j = 0; j < boardSize; j++) {
            if (buttons[i][j].getText().equals("")) {
                if (isWinningMove(buttons, boardSize, i, j, opponentSymbol)) {
                    return new int[]{i, j};
                }
            }
        }
    }
    // Step 3. Otherwise, choose a random move from the free ones.
    List<int[]> freeMoves = new ArrayList<>();
    for (int i = 0; i < boardSize; i++) {
        for (int j = 0; j < boardSize; j++) {
            if (buttons[i][j].getText().equals("")) {
                freeMoves.add(new int[]{i, j});
            }
        }
    }
    if (!freeMoves.isEmpty()) {
        return freeMoves.get(rand.nextInt(freeMoves.size()));
    }
    return null;
}

// Helper method to check if placing the symbol in a cell leads to a win
private static boolean isWinningMove(JButton[][] buttons, int boardSize, int row, int col, String symbol) {
    String[][] board = new String[boardSize][boardSize];
    for (int i = 0; i < boardSize; i++) {
        for (int j = 0; j < boardSize; j++) {
            board[i][j] = buttons[i][j].getText();
        }
    }
    board[row][col] = symbol;
    return checkWin(board, boardSize, row, col, symbol);
}

// Check win after placing a symbol (similar logic to GameWindow)
private static boolean checkWin(String[][] board, int boardSize, int row, int col, String symbol) {
    boolean win = true;
    // Row
    for (int j = 0; j < boardSize; j++) {
        if (!board[row][j].equals(symbol)) {
            win = false;
            break;
        }
    }
    if (win) return true;
    // Column
    win = true;
    for (int i = 0; i < boardSize; i++) {
        if (!board[i][col].equals(symbol)) {
            win = false;
            break;
        }
    }
    if (win) return true;
    // Main diagonal
    if (row == col) {
        win = true;
        for (int i = 0; i < boardSize; i++) {
            if (!board[i][i].equals(symbol)) {
                win = false;
                break;
            }
        }
        if (win) return true;
    }
    // Secondary diagonal
    if (row + col == boardSize - 1) {
        win = true;
        for (int i = 0; i < boardSize; i++) {
            if (!board[i][boardSize - 1 - i].equals(symbol)) {
                win = false;
                break;
            }
        }
        if (win) return true;
    }
    return false;
}

/*For Ultimate TicTacToe, a similar method can be implemented that analyzes mini-boards. 
Here, for simplicity, we choose a random move from the free cells of the active mini-boards. */
public static int[] chooseMoveInUltimate(MiniBoard[][] boards, String aiSymbol) {
    List<int[]> freeMoves = new ArrayList<>();
    for (int br = 0; br < 3; br++) {
        for (int bc = 0; bc < 3; bc++) {
            // For each mini-board that is not completed
            if (!boards[br][bc].isFinished()) {
                // Iterate through the cells of the mini-board
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        // If the cell is free, add it
                        if (boards[br][bc].cells[i][j].getText().equals("")) {
                            freeMoves.add(new int[]{br, bc, i, j});
                        }
                    }
                }
            }
        }
    }
    if (!freeMoves.isEmpty()) {
        return freeMoves.get(rand.nextInt(freeMoves.size()));
    }
    return null;
}

} 