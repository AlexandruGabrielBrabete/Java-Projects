import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// --- POSITION CLASS ---
class Position {
    int x, y;
    Position(int x, int y) { this.x = x; this.y = y; }
    boolean isValid(int size) { return x>=0 && y>=0 && x<size && y<size; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position)o;
        return x==p.x && y==p.y;
    }
    @Override public int hashCode() { return Objects.hash(x,y); }
}

// --- PIECE INTERFACE ---
interface ChessPiece {
    enum Color { WHITE, BLACK }
    Color getColor();
    char getSymbol();
    List<Position> legalMoves(Position from, Map<Position,ChessPiece> board);
}

// --- ABSTRACT BASE ---
abstract class AbstractPiece implements ChessPiece {
    protected final Color color;
    AbstractPiece(Color c){ this.color=c; }
    public Color getColor(){ return color; }
}

// --- PAWN ---
class Pawn extends AbstractPiece {
    Pawn(Color c){ super(c); }
    public char getSymbol(){ return color==Color.WHITE?'P':'p'; }
    public List<Position> legalMoves(Position from, Map<Position,ChessPiece> board){
        List<Position> moves = new ArrayList<>();
        int dir = (color==Color.WHITE ? -1 : +1);
        Position one = new Position(from.x, from.y + dir);
        if (one.isValid(8) && !board.containsKey(one)) {
            moves.add(one);
            int home = (color==Color.WHITE?6:1);
            Position two = new Position(from.x, from.y + 2*dir);
            if (from.y==home && !board.containsKey(two))
                moves.add(two);
        }
        for(int dx : new int[]{-1,1}) {
            Position diag = new Position(from.x+dx, from.y+dir);
            if (diag.isValid(8) && board.containsKey(diag)
             && board.get(diag).getColor()!=color) {
                moves.add(diag);
            }
        }
        return moves;
    }
}

// --- KNIGHT ---
class Knight extends AbstractPiece {
    Knight(Color c){ super(c); }
    public char getSymbol(){ return color==Color.WHITE?'N':'n'; }
    private static final int[][] D={{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}};
    public List<Position> legalMoves(Position from, Map<Position,ChessPiece> board){
        List<Position> m=new ArrayList<>();
        for(var d:D){
            Position p=new Position(from.x+d[0], from.y+d[1]);
            if(p.isValid(8) && (!board.containsKey(p) || board.get(p).getColor()!=color))
                m.add(p);
        }
        return m;
    }
}

// --- SLIDERS: BISHOP, ROOK, QUEEN ---
abstract class Slider extends AbstractPiece {
    protected final int[][] directions;
    Slider(Color c,int[][] dirs){ super(c); directions=dirs; }
    public List<Position> legalMoves(Position from, Map<Position,ChessPiece> board){
        List<Position> m=new ArrayList<>();
        for(var d:directions){
            int nx=from.x, ny=from.y;
            while(true){
                nx+=d[0]; ny+=d[1];
                Position p=new Position(nx,ny);
                if(!p.isValid(8)) break;
                if(!board.containsKey(p)){
                    m.add(p);
                    continue;
                }
                if(board.get(p).getColor()!=color) m.add(p);
                break;
            }
        }
        return m;
    }
}

class Bishop extends Slider {
    private static final int[][] D={{1,1},{1,-1},{-1,1},{-1,-1}};
    Bishop(Color c){ super(c,D); }
    public char getSymbol(){ return color==Color.WHITE?'B':'b'; }
}

class Rook extends Slider {
    private static final int[][] D={{1,0},{-1,0},{0,1},{0,-1}};
    Rook(Color c){ super(c,D); }
    public char getSymbol(){ return color==Color.WHITE?'R':'r'; }
}

class Queen extends Slider {
    private static final int[][] D={{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
    Queen(Color c){ super(c,D); }
    public char getSymbol(){ return color==Color.WHITE?'Q':'q'; }
}

// --- KING ---
class King extends AbstractPiece {
    King(Color c){ super(c); }
    public char getSymbol(){ return color==Color.WHITE?'K':'k'; }
    public List<Position> legalMoves(Position from, Map<Position,ChessPiece> board){
        List<Position> m=new ArrayList<>();
        for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++){
            if(dx==0&&dy==0) continue;
            Position p=new Position(from.x+dx, from.y+dy);
            if(p.isValid(8) && (!board.containsKey(p)||board.get(p).getColor()!=color))
                m.add(p);
        }
        return m;
    }
}

// --- CHESSBOARD PANEL ---
class ChessBoard extends JPanel implements MouseListener {
    private final int size=8, cell=600/size;
    private Map<Position,ChessPiece> board = new HashMap<>();
    private Position selected=null;
    private List<Position> highlights = new ArrayList<>();
    private ChessPiece.Color turn = ChessPiece.Color.WHITE;
    private String mode="Learn";
    private Random rng = new Random();

    // Learn‐mode selections:
    private String learnPieceType = "Pawn";
    private ChessPiece.Color learnPieceColor = ChessPiece.Color.WHITE;

    ChessBoard(){
        setPreferredSize(new Dimension(600,600));
        addMouseListener(this);
    }

    void setMode(String m){
        mode=m;
        board.clear();
        turn=ChessPiece.Color.WHITE;
        selected=null; highlights.clear();
        if(!mode.equals("Learn")) setupInitial();
        repaint();
    }

    public void setLearnPieceType(String t) {
        this.learnPieceType = t;
    }

    public void setLearnPieceColor(String c) {
        this.learnPieceColor = c.equals("White")
            ? ChessPiece.Color.WHITE
            : ChessPiece.Color.BLACK;
    }

    private void setupInitial(){
        for(int x=0;x<8;x++){
            board.put(new Position(x,1), new Pawn(ChessPiece.Color.BLACK));
            board.put(new Position(x,6), new Pawn(ChessPiece.Color.WHITE));
        }
        ChessPiece.Color[] cols={ChessPiece.Color.BLACK,ChessPiece.Color.WHITE};
        int[] ranks={0,7};
        for(int i=0;i<2;i++){
            ChessPiece.Color c=cols[i];
            int r=ranks[i];
            board.put(new Position(0,r), new Rook(c));
            board.put(new Position(7,r), new Rook(c));
            board.put(new Position(1,r), new Knight(c));
            board.put(new Position(6,r), new Knight(c));
            board.put(new Position(2,r), new Bishop(c));
            board.put(new Position(5,r), new Bishop(c));
            board.put(new Position(3,r), new Queen(c));
            board.put(new Position(4,r), new King(c));
        }
    }

    @Override protected void paintComponent(Graphics g){
        super.paintComponent(g);
        for(int x=0;x<8;x++)for(int y=0;y<8;y++){
            g.setColor((x+y)%2==0?Color.LIGHT_GRAY:Color.DARK_GRAY);
            g.fillRect(x*cell,y*cell,cell,cell);
        }
        g.setColor(new Color(0,255,0,100));
        for(var p:highlights){
            g.fillRect(p.x*cell,p.y*cell,cell,cell);
        }
        for(var e:board.entrySet()){
            Position p=e.getKey();
            ChessPiece pc=e.getValue();
            int cx=p.x*cell+cell/2, cy=p.y*cell+cell/2;
            g.setColor(pc.getColor()==ChessPiece.Color.WHITE?Color.WHITE:Color.BLACK);
            g.fillOval(cx-20,cy-20,40,40);
            g.setColor(pc.getColor()==ChessPiece.Color.WHITE?Color.BLACK:Color.WHITE);
            g.setFont(new Font("Monospaced",Font.BOLD,24));
            g.drawString(""+pc.getSymbol(),cx-8,cy+8);
        }
    }

    public void mouseClicked(MouseEvent e){
        int x=e.getX()/cell, y=e.getY()/cell;
        Position pos=new Position(x,y);

        if(mode.equals("Learn")){
            // If nothing selected and clicked on piece → select for moving
            if(selected==null && board.containsKey(pos)){
                selected=pos;
                highlights=board.get(pos).legalMoves(pos,board);
            }
            // If selected and clicked on a legal target → move
            else if(selected!=null && highlights.contains(pos)){
                ChessPiece pc=board.remove(selected);
                board.put(pos,pc);
                selected=null;
                highlights.clear();
            }
            // Otherwise, if click on empty and no selection → place
            else if(selected==null){
                ChessPiece p;
                switch(learnPieceType){
                    case "Knight": p=new Knight(learnPieceColor); break;
                    case "Bishop": p=new Bishop(learnPieceColor); break;
                    case "Rook":   p=new Rook(learnPieceColor);   break;
                    case "Queen":  p=new Queen(learnPieceColor);  break;
                    case "King":   p=new King(learnPieceColor);   break;
                    default:       p=new Pawn(learnPieceColor);
                }
                board.put(pos,p);
            }
            // Clicking elsewhere or on invalid → cancel selection
            else {
                selected=null;
                highlights.clear();
            }

            repaint();
            return;
        }

        // --- 1v1 & Versus AI modes unchanged ---
        if(selected==null){
            ChessPiece pc=board.get(pos);
            if(pc!=null && pc.getColor()==turn){
                selected=pos;
                highlights=pc.legalMoves(pos,board);
            }
        } else {
            if(highlights.contains(pos)){
                ChessPiece pc=board.remove(selected);
                board.put(pos,pc);
                turn = (turn==ChessPiece.Color.WHITE?ChessPiece.Color.BLACK:ChessPiece.Color.WHITE);
                if(mode.equals("Versus AI") && turn==ChessPiece.Color.BLACK){
                    aiMove();
                    turn=ChessPiece.Color.WHITE;
                }
            }
            selected=null; highlights.clear();
        }
        repaint();
    }

    private void aiMove(){
        List<Position> pcs=new ArrayList<>();
        for(var e:board.entrySet())
            if(e.getValue().getColor()==ChessPiece.Color.BLACK)
                pcs.add(e.getKey());
        Collections.shuffle(pcs,rng);
        for(var from:pcs){
            var moves=board.get(from).legalMoves(from,board);
            if(!moves.isEmpty()){
                Position to=moves.get(rng.nextInt(moves.size()));
                board.put(to,board.remove(from));
                return;
            }
        }
    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
}

// --- MAIN CLASS ---
public class ChessGame {
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chess Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ChessBoard board = new ChessBoard();

            JComboBox<String> modeBox = new JComboBox<>(new String[]{"Learn","1v1","Versus AI"});
            modeBox.addActionListener(e -> board.setMode((String)modeBox.getSelectedItem()));

            JComboBox<String> typeBox = new JComboBox<>(new String[]{"Pawn","Knight","Bishop","Rook","Queen","King"});
            typeBox.addActionListener(e -> board.setLearnPieceType((String)typeBox.getSelectedItem()));

            JComboBox<String> colorBox = new JComboBox<>(new String[]{"White","Black"});
            colorBox.addActionListener(e -> board.setLearnPieceColor((String)colorBox.getSelectedItem()));

            JPanel top = new JPanel();
            top.add(new JLabel("Mode:"));
            top.add(modeBox);
            top.add(new JLabel(" Piece:"));
            top.add(typeBox);
            top.add(new JLabel(" Color:"));
            top.add(colorBox);

            frame.add(top, BorderLayout.NORTH);
            frame.add(board, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
