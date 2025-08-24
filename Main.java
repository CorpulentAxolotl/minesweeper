import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Main extends JPanel implements MouseListener{
    public int FPS = 30;
    //*****************************
    public static int NWidth = 16;
    public static int NHeight = 15;
    //*****************************
    public static int Swidth = 30;
    public Color lightGreen = new Color(150,255,150);
    public Color darkGreen = new Color(100,255,100);
    public ArrayList<ArrayList<Cell>> grid = new ArrayList<>();
    public ArrayList<int[]> mines = new ArrayList<>();
    public boolean gameStarted = false;
    public boolean gameLose = false;
    public boolean gameWin = false;
    public Font arial = new Font("Arial", Font.BOLD, (int) (3*Swidth/5));

    public static boolean containsCoordinate(ArrayList<int[]> list, int[] coord) {
        for (int[] c : list) {
            if (c[0] == coord[0] && c[1] == coord[1]) {
                return true;
            }
        }
        return false;
    }

    public void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent(); // height above baseline

        int drawX = x - textWidth / 2;
        int drawY = y + textHeight / 2 - fm.getDescent();

        g.drawString(text, drawX, drawY);
    }

    private class Cell {
        public boolean type; //true if bomb
        public int value; //calculated after object instance is created
        public boolean uncovered = false;
        protected boolean flagged = false;
      
        protected int x;
        protected int y;
      
        public Cell(int y, int x, boolean t) {
            this.x = x;
            this.y = y;
            type = t;
        }

        public void draw(Graphics screen) {
            if (!uncovered) {
                if ((x+y) % 2 == 1) {
                    Rect(screen, lightGreen, x*Swidth, y*Swidth, Swidth, Swidth);
                } else {
                    Rect(screen, darkGreen, x*Swidth, y*Swidth, Swidth, Swidth);
                }
                if (flagged) {
                    Rect(screen, Color.RED, x*Swidth+ Swidth/8, y*Swidth + Swidth/8, Swidth/5, 3*Swidth/4);
                    screen.fillPolygon(new int[] {x*Swidth+Swidth/8 +Swidth/5, x*Swidth+7*Swidth/8, x*Swidth+Swidth/8 +Swidth/5}, 
                    new int[] {y*Swidth + Swidth/8, y*Swidth + 5*Swidth/16, y*Swidth + 4*Swidth/8}, 3);
                }
            } else {
                // uncovered
                if (type) {
                    Rect(screen, Color.RED, x*Swidth, y*Swidth, Swidth, Swidth); // draws red square if bomb
                    return;
                }
                Rect(screen, Color.LIGHT_GRAY, x*Swidth, y*Swidth, Swidth, Swidth); // draw empty gray square
                if (value != 0) { //draws number only if the value isn't 0
                    screen.setColor(Color.BLACK);
                    screen.setFont(arial);
                    screen.drawString(String.valueOf(value), x*Swidth + Swidth/5, y*Swidth + 4*Swidth/5);
                }
                
            }
        }
        public void flag() {
            flagged = !flagged;
        }
        public void interact() {
            if (flagged) return; // now your flagged squares are safe to click
            uncovered = true;

            if (type) { // clicked a mine
                gameLose = true;
                return;
            }

            // Uncover neighbors
            if (value == 0) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dy != 0 || dx != 0) {
                            int ny = y + dy;
                            int nx = x + dx;
                            if (ny >= 0 && ny < NHeight && nx >= 0 && nx < NWidth) {
                                Cell neighbor = grid.get(ny+1).get(nx+1); // +1 for border
                                if (!neighbor.uncovered) {
                                    neighbor.uncovered = true;  // uncover neighbor
                                    if (neighbor.value == 0) {
                                        neighbor.interact(); // spread only if neighbor is empty
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    

    public Main() {
        // Add the KeyListener and MouseListener to the panel
        addMouseListener(this);
        setFocusable(true); // Make sure the panel is focusable to receive key events
    }

    
    public static void Rect(Graphics screen, Color color,
    int x, int y, int w, int h) {
        screen.setColor(color);
        screen.fillRect(x,y,w,h);
    }
    // Override the paintComponent method to perform custom drawing
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!gameStarted) { // checkerboard pattern when game hasn't started, the squares are just pixels, no cells yet
            for (int i = 0; i < NWidth; i++) {
                for (int j = 0; j < NHeight; j++) {
                    Rect(g, ((i+j) % 2 == 0)? darkGreen : lightGreen, i*Swidth, j*Swidth, Swidth, Swidth);
                }
            }
        }
        for (int i = 0; i < grid.size(); i++) { // draws the grid
            for (int j = 0; j < grid.get(0).size(); j++) {
                grid.get(i).get(j).draw(g);
            }
        }
        if (gameLose) { // displays losing text
            for (int i = 0; i < mines.size(); i++) {
                grid.get(mines.get(i)[0]+1).get(mines.get(i)[1]+1).uncovered = true;
            }
            for (int i = 0; i < grid.size(); i++) {
                for (int j = 0; j < grid.get(0).size(); j++) {
                    grid.get(i).get(j).draw(g);
                }
            }
            Rect(g, Color.WHITE, Swidth*NWidth/2 - 80, Swidth*NHeight/2 - 23, 160, 40);
            g.setColor(new Color(100, 0, 0));
            g.setFont(new Font("Arial", Font.BOLD, 30));
            drawCenteredString(g,"YOU LOSE", Swidth*NWidth/2, Swidth*NHeight/2);
        }
        if (gameWin) { // displays winning text
            Rect(g, Color.WHITE, Swidth*NWidth/2 - 78, Swidth*NHeight/2 - 23, 156, 40);
            g.setColor(new Color(100, 200, 100));
            g.setFont(new Font("Arial", Font.BOLD, 30));
            drawCenteredString(g, "YOU WIN", Swidth*NWidth/2, Swidth*NHeight/2);
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        long targetTime = 1000000000 / FPS; // time per frame (in nanoseconds)
        while (true) {
            long now = System.nanoTime();
            long elapsedTime = now - lastTime; // not really useful, was for statistical purposes

            repaint();
            if (gameLose || gameWin) { // ends game when you win or lose
                break;
            }
            long sleepTime = targetTime - (System.nanoTime() - now);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000, (int)(sleepTime % 1000000)); // Convert nanoseconds to milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            lastTime = System.nanoTime();
        }
    }


    // Handle mouse click events
    @Override
    public void mouseClicked(MouseEvent e) {
        int clickedRow = e.getY() / Swidth;
        int clickedCol = e.getX() / Swidth;
        if (!gameStarted) {

            // Reset grid
            grid = new ArrayList<>();

            // Create a set of forbidden coordinates (first click + neighbors) for mines
            ArrayList<int[]> forbidden = new ArrayList<>();
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int ny = clickedRow + dy;
                    int nx = clickedCol + dx;
                    if (ny >= 0 && ny < NHeight && nx >= 0 && nx < NWidth) {
                        forbidden.add(new int[]{ny, nx});
                    }
                }
            }

            // Generate mines
            int totalMines = (NWidth * NHeight) / 7;
            while (mines.size() < totalMines) {
                int ny = (int)(Math.random() * NHeight);
                int nx = (int)(Math.random() * NWidth);
                int[] coord = new int[]{ny, nx};
                if (!containsCoordinate(mines, coord) && !containsCoordinate(forbidden, coord)) { //if coordinate is forbidden then don't 
                    mines.add(coord);
                }
            }

            // Build the grid (with border as before)
            for (int i = -1; i < NHeight+1; i++) {
                ArrayList<Cell> row = new ArrayList<>();
                for (int j = -1; j < NWidth+1; j++) {
                    boolean isMine = containsCoordinate(mines, new int[]{i, j});
                    row.add(new Cell(i, j, isMine));
                }
                grid.add(row);
            }

            // Count neighbor mines for each cell
            for (int i = 0; i < NHeight; i++) {
                for (int j = 0; j < NWidth; j++) {
                    if (!grid.get(i+1).get(j+1).type) { // skip border
                        int count = 0;
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dx = -1; dx <= 1; dx++) {
                                if (grid.get(i+1+dy).get(j+1+dx).type) count++;
                            }
                        }
                        grid.get(i+1).get(j+1).value = count;
                    }
                }
            }

            gameStarted = true;

            // Automatically uncover first clicked cell
            grid.get(clickedRow+1).get(clickedCol+1).interact();
        } else {
            if (SwingUtilities.isLeftMouseButton(e)) { // interact on left click
                grid.get(clickedRow+1).get(clickedCol+1).interact();
                int total = 0;
                for (int i = 1; i < grid.size()-1; i++) {
                    for (int j = 1; j < grid.get(0).size()-1; j++) {
                        total += (grid.get(i).get(j).uncovered && !grid.get(i).get(j).type)? 1 : 0;
                    }
                }
                if (total == NWidth*NHeight - (int) ((NWidth * NHeight) / 7)) {
                    gameWin = true;
                }
            } else if (SwingUtilities.isRightMouseButton(e)){ // flag on right click
                grid.get(clickedRow+1).get(clickedCol+1).flag();
            }
        }
    }
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    public static void main(String[] args) {
        // Create a JFrame (window) to display our JPanel
        JFrame frame = new JFrame("MineSweeper");
        Main panel = new Main();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setPreferredSize(new Dimension(Swidth * NWidth, Swidth * NHeight));
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        new Thread(panel::run).start();
    }
}
    
