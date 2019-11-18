package marksman;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.WindowConstants;


public class MainWindow extends JFrame implements KeyListener, WindowListener {
  private Board board;
  
  public MainWindow() {
    super("Marksman 2.1");
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {}
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setResizable(false);
    getContentPane().setBackground(Color.black);
    setSize(800, 600);
    setLocationRelativeTo(null);
//    setUndecorated(true);
//    DisplayMode dm = new DisplayMode(800, 600, 16, DisplayMode.REFRESH_RATE_UNKNOWN);
//    GraphicsDevice vc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//    vc.setFullScreenWindow(this);
//    if(dm != null && vc.isDisplayChangeSupported()) {
//      try{
//        vc.setDisplayMode(dm);
//      }catch(Exception ex){}
//    }
    board = new Board();
    setContentPane(board);
    addKeyListener(this);
    addWindowListener(this);
    if(true) {
      BufferedImage blankCursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankCursorImg, new Point(0, 0), null);
      this.setCursor(blankCursor);
    }
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {}
    show();
  }
  
  public static void main(String[] args) {
    new MainWindow();
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    board.keyPressed(e);
  }

  @Override
  public void keyReleased(KeyEvent e) {}

  @Override
  public void windowOpened(WindowEvent e) {}

  @Override
  public void windowClosing(WindowEvent e) {
    board.setBestPoints();
    System.exit(0);
  }

  @Override
  public void windowClosed(WindowEvent e) {}

  @Override
  public void windowIconified(WindowEvent e) {}

  @Override
  public void windowDeiconified(WindowEvent e) {}

  @Override
  public void windowActivated(WindowEvent e) {}

  @Override
  public void windowDeactivated(WindowEvent e) {}
}
