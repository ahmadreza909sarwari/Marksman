 package marksman;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class Board extends JComponent implements KeyListener, MouseMotionListener, MouseListener {
  public enum Guns {Blow,DoubleBlow,Icy,Empyreal,Racket}
  public void keyTyped(KeyEvent e) {}
  public void keyPressed(KeyEvent e ) {
//    System.out.println(";" + e.getKeyCode());
    switch(e.getKeyCode()) {
      case 32:
      case 10:
        if(isGameOver || gameFinished) {
          if(e.getKeyCode() == 32 || e.getKeyCode() == 10) {
            if(gameFinished) gameFinished = !gameFinished;
            restart();
          } else if(e.getKeyCode() == 27) {
            setBestPoints();
            System.exit(0);
          }
        } else if(isAlowFire) {
          playFire();
          shot.isVisible = !shot.isVisible;
          shot.x = blowGun.x;
          shot.y = blowGun.y + 5;
          isAlowFire = !shot.isVisible;
        }
        break;
      case 27:
        setBestPoints();
        System.exit(0);
        break;
      case 65:
        startNextLevel();
        break;
    }
  }
  public void keyReleased(KeyEvent e) {}

  @Override
  public void mouseDragged(MouseEvent e) {
    y = e.getY();
    if(y > minYGun && y < maxYGun)
      blowGun.y = e.getY();
  }
  int y;
  @Override
  public void mouseMoved(MouseEvent e) {
    y = e.getY();
    if(y > minYGun && y < maxYGun)
      blowGun.y = e.getY();
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {
    if(isAlowFire && chanceCount > 0) {
      playFire();
      shot.isVisible = !shot.isVisible;
      shot.x = blowGun.x;
      shot.y = blowGun.y + 5;
      isAlowFire = !shot.isVisible;
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}
  
  public enum TypeScreen { Running, Paused, GameOver };
  private TypeScreen type;
  
  public TypeScreen getType() {
    return type;
  }
  
  int bestPoints;
  boolean gameFinished;
  int minYGun,maxYGun;
  int minYTarget,maxYTarget;
  int minXSnag,maxXSnag,minYSnag,maxYSnag;
  final int MAXGUNS = 5;
  int widthSnag = 25,heightSnag = 25;
  int requestPoints = 100;
  Clip fireClip,hitClip,reloadClip;
  Sequencer sequencerLose;
  Font textFont,boldFont;
  Timer timer;
  GameEntry gameBoard,target,blowGun,shot,chance,points,topImage;
  GameEntry showNewPoints;
  GameEntry gameNotRun,GameRun;
  ArrayList<GameEntry> snags;
  int level = 1;
  boolean isAlowFire,isGameOver;
  int chanceCount;
  int intGuns;
  Color textColor,rectColor,backColor,pointsColor,topColor,mColor;
  String strM = "", strM2 = "";
  int gamePoints,chanceRange;
  int pointsAlpha;
  int timerSnags,snagTime,timeAddSnag;
  Random rnd;
  boolean hasSnags;
  readPoints readPoints;
  boolean isChecked;
  boolean isNewState;
  private Guns guns;
  boolean newGun;
  
  public Board() {
    setFocusable(true);
    addKeyListener(this);
    addMouseMotionListener(this);
    addMouseListener(this);
    
    rnd = new Random();
    
    readPoints = new readPoints();
    bestPoints = readPoints.getPoints();
    
    Sequence sequence = null;
    Sequencer sequencer = null;
    try {
      sequence = MidiSystem.getSequence(getClass().getResource("music.mid"));
      sequencer = MidiSystem.getSequencer();
      sequencer.setLoopCount(-1);
      sequencer.open();
      sequencer.setSequence(sequence);
    } catch (Exception ex) { }
    sequencer.start();
    
    try {
      AudioInputStream audio = AudioSystem.getAudioInputStream(getClass().getResource("fire.wav"));
      fireClip = AudioSystem.getClip();
      fireClip.open(audio);
      audio = AudioSystem.getAudioInputStream(getClass().getResource("hit.wav"));
      hitClip = AudioSystem.getClip();
      hitClip.open(audio);
      audio = AudioSystem.getAudioInputStream(getClass().getResource("reload.wav"));
      reloadClip = AudioSystem.getClip();
      reloadClip.open(audio);
    } catch(Exception ex) {}
    
    try {
      sequence = MidiSystem.getSequence(getClass().getResource("lose.mid"));
      sequencerLose = MidiSystem.getSequencer();
      sequencerLose.open();
      sequencerLose.setSequence(sequence);
    } catch (Exception ex) { }
    
    snagTime = 100;
    isAlowFire = true;
    chanceCount = 3;
    backColor = Color.darkGray;
    textColor = Color.orange;
    rectColor = new Color(27, 7, 10);
    topColor = new Color(0, 75, 128, 220);
    mColor = new Color(0, 62, 93, 170);
    pointsColor = Color.red;
    
    textFont = new Font("Arial", Font.BOLD, 24);
    boldFont = new Font("Arial", Font.BOLD, 11);
    chanceRange = 50;
    

    showNewPoints = new GameEntry(0,0,50,50);
    showNewPoints.isVisible = false;
    showNewPoints.speed = 2;
    
    gameBoard = new GameEntry();
    gameBoard.img = new ImageIcon(getClass().getResource("bglevel1.jpg")).getImage();
    gameBoard.x = 5;
    gameBoard.y = 130;
    gameBoard.width = 785;
    gameBoard.height = 465;
    minYGun = gameBoard.y + 55;
    maxYGun = gameBoard.y + gameBoard.height - 35;
    minYTarget = gameBoard.y + 75;
    maxYTarget = gameBoard.y + gameBoard.height - 105;
    minXSnag = gameBoard.x + 170;
    minYSnag = gameBoard.y - 10;
    maxXSnag = gameBoard.x + 600;
    maxYSnag = gameBoard.y + gameBoard.height + 100;
    
    snags = new ArrayList();
    
    gameNotRun = new GameEntry((gameBoard.x + gameBoard.width / 2) - (294 / 2),(gameBoard.y + gameBoard.height / 2) - (250 / 2),294,250);
    gameNotRun.img = new ImageIcon(getClass().getResource("overpause.png")).getImage();
    
    topImage = new GameEntry(5,5,gameBoard.width,120);
    topImage.img = new ImageIcon(getClass().getResource("top.png")).getImage();
    
    chance = new GameEntry();
    chance.img = new ImageIcon(getClass().getResource("placewood.png")).getImage();
    chance.width = 155;
    chance.height = 42;
    chance.x = gameBoard.x + gameBoard.width - chance.width;
    chance.y = gameBoard.y + 5;
    
    points = new GameEntry();
    points.img = new ImageIcon(getClass().getResource("placewood.png")).getImage();
    points.width = chance.width;
    points.height = chance.height;
    points.x = gameBoard.x + 5;
    points.y = chance.y;
    
    blowGun = new GameEntry();
    blowGun.img = new ImageIcon(getClass().getResource("btnFire.png")).getImage();
    blowGun.speed = 2;
    blowGun.width = 70;
    blowGun.height = 30;
    blowGun.x = gameBoard.width - blowGun.width;
    blowGun.y = minYGun;
    
    target = new GameEntry();
    target.img = new ImageIcon(getClass().getResource("target.png")).getImage();
    target.speed = 1;
    target.width = 35;
    target.height = 55;
    target.x = gameBoard.x + 5;
    target.y = minYTarget;
    
    shot = new GameEntry(0,0,21,4);
    shot.img = new ImageIcon(getClass().getResource("shot.png")).getImage();
    shot.speed = -5;
    shot.isVisible = false;
    
    timeAddSnag = 15;
    
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {}
    
    timer = new Timer();
    timer.scheduleAtFixedRate(new ScheduleTask(), 100, 15);
  }
  
  public void setBestPoints() {
    readPoints.setPoints(bestPoints);
  }
  
  public void restart() {
    if(gameFinished) {
      gameFinished = false;
      isChecked = false;
    }
    if(bestPoints < gamePoints) bestPoints = gamePoints;
    chanceCount = 3;
    isGameOver = false;
    level = 1;
    target.speed = 1;
    if(checkedLevel == 4 || checkedLevel == 7) isChecked = true;
    else {
      gamePoints = 0;
      requestPoints = 100;
    }
    shot.speed = -5;
    hasSnags = false;
    timeAddSnag = 15;
    gameBoard.img = new ImageIcon(getClass().getResource("bglevel1.jpg")).getImage();
    snags.clear();
    widthSnag = 25;
    heightSnag = 25;
    chanceRange = 50;
    pointsColor = Color.red;
    
    if(isChecked) {
      for (int i = 0; i < checkedLevel - 1; i++) {
        startNextLevel();
        try {
          Thread.sleep(25);
        } catch (Exception ex) {}
      }
    }
  }
  
  private void startNextLevel() {
    level++;
    if(level > 7) {
      isNewState = true;
      pointsColor = Color.red;
    } else {
      if(level > 2) {
        timeAddSnag--;
        hasSnags = true;
        if(level > 3) {
          widthSnag = 35;
          heightSnag = 35;
        }
      } else if(level == 8) {
        hasSnags = false;
        
      }
      
      if(level == 2)
        pointsColor = Color.black;
      else if(level == 3)
        pointsColor = Color.cyan;
      else if(level == 4)
        pointsColor = Color.white;
      else if(level == 5)
        pointsColor = Color.white;
      else if(level == 6)
        pointsColor = Color.red;
      else if(level == 7)
        pointsColor = Color.black;
      
      gameBoard.img = new ImageIcon(getClass().getResource("bglevel"+level+".jpg")).getImage();
      target.speed = level;
      chanceCount++;
      chanceRange -= (level * 25) + (chanceRange % 50) * 2;
      shot.speed -= 2;
    }
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(backColor);
    g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
    g.setFont(boldFont);
    g.setColor(Color.black);
    g.drawImage(gameBoard.img, gameBoard.x, gameBoard.y, gameBoard.width, gameBoard.height, this);
    if(hasSnags) {
      for(int i = 0; i < snags.size(); i++) {
        g.drawImage(snags.get(i).img, snags.get(i).x, snags.get(i).y, snags.get(i).width, snags.get(i).height, this);
      }
    }
    g.drawImage(topImage.img, topImage.x, topImage.y, topImage.width, topImage.height, this);
    g.setColor(topColor);
    g.fillRect(gameBoard.x, gameBoard.y, gameBoard.width, 55);
    g.setFont(textFont);
    g.setColor(textColor);
    g.drawString("Level : " + level, 170, gameBoard.y + 35);
    g.drawString("Request : " + requestPoints, 310, gameBoard.y + 35);
    g.drawString("[" + bestPoints + "]", 530, gameBoard.y + 35);
    g.drawImage(points.img, points.x, points.y, points.width, points.height, this);
    g.setColor(textColor);
    g.drawString("" + gamePoints, points.x + 20, points.y + 28);
    g.drawImage(chance.img, chance.x, chance.y, chance.width, chance.height, this);
    for(int i = 0; i < chanceCount; i++)
      g.drawImage(target.img, chance.x + (i * (chance.width / chanceCount)) + 5, chance.y + 5, 18, 32, this);
    g.setColor(rectColor);
    g.fillRect(blowGun.x + blowGun.width - 10, minYGun, 15, maxYGun - 150);
    g.fillRect(target.x - 5, minYGun, 15, maxYGun - 150);
    g.drawImage(blowGun.img, blowGun.x, blowGun.y, blowGun.width, blowGun.height, this);
    g.drawImage(target.img, target.x, target.y, target.width, target.height, this);
    if(shot.isVisible)
      g.drawImage(shot.img, shot.x, shot.y, shot.width, shot.height, this);
    if(showNewPoints.isVisible) {
      g.setColor(pointsColor);
      g.drawString(showNewPoints.text,  showNewPoints.x, showNewPoints.y);
    }
    if(isGameOver || gameFinished || isNewState) {
      g.setColor(mColor);
      g.fillRect(gameBoard.x, gameBoard.y, gameBoard.width, gameBoard.height);
      g.setColor(textColor);
      g.drawString(strM, gameBoard.x + gameBoard.width / 2 - (strM.length() * 5), gameBoard.y + 140);
      g.drawString(strM2, gameBoard.x + gameBoard.width / 2 - (strM2.length() * 5), gameBoard.y + 180);
    }
    
  }
  
  public JComponent getparent() {
    return this;
  }
  
  private int checkedLevel;
  private void addPoints(int posY1, int posY2) {
    int distance = 0,p = 0;
    
    if(posY1 > posY2 + (target.height / 2)) {
      distance = posY1 - (posY2 + (target.height / 2));
    } else {
      distance = (posY2 + (target.height / 2)) - posY1;
    }
    
    p = ((-1 * distance) + 29) - (level - 3) * 2;
    gamePoints += p;
    
    if(gamePoints >= chanceRange) {
      chanceCount++;
      chanceRange += 50;
    }
    
    if(gamePoints >= requestPoints) {
      requestPoints += 120;
      startNextLevel();
      if(level == 4 || level == 7) checkedLevel = level;
    }
    
    if(target.speed > 0)
      showNewPoints.speed = 1;
    else
      showNewPoints.speed = 2;
    pointsAlpha = 255;
    showNewPoints.text = "" + p;
    shot.isVisible = !isAlowFire;
    showNewPoints.isVisible = !showNewPoints.isVisible;
  }
  
  private void playFire() {
    fireClip.setFramePosition(0);
    fireClip.start();
  }
  
  private void playReload() {
    reloadClip.setFramePosition(0);
    reloadClip.start();
  }
  
  private void playHit() {
    hitClip.setFramePosition(0);
    hitClip.start();
  }
  
  private void playMid() {
    sequencerLose.setTickPosition(0);
    sequencerLose.start();
  }
  
  private class ScheduleTask extends TimerTask {
    @Override
    public void run() {
      if(gameFinished) {
        strM = "You are now in new state";
        isNewState = true;
        restart();
        return;
      } else if(chanceCount < 1) {
        if(!isGameOver) playMid();
        isGameOver = true;
        strM = "Do you want to play?";
        strM2 = "Press Enter to restart     Press Esc to exit";
      } else {
        if(hasSnags) {
          if(timerSnags >= snagTime) {
            snagTime = 400 - (level * 75);
            if(snagTime < 0)
              snagTime = 100;
            snagTime = rnd.nextInt(snagTime);
            if(snagTime < 75) {
              snagTime += 100;
            }
            timerSnags = 0;
            int xSnag = rnd.nextInt(maxXSnag);
            if(xSnag < minXSnag)
              xSnag = xSnag + (minXSnag - xSnag) + rnd.nextInt(100);
            int ySnag = rnd.nextInt(minYSnag);
            int imgSnag = rnd.nextInt(5);
            if(imgSnag < 1) imgSnag = 1;
            else if(imgSnag > 4) imgSnag = 4;
            if(newGun&&rnd.nextInt(minYSnag) == minYSnag-1) {
              intGuns ++;
              if(intGuns > MAXGUNS) intGuns = rnd.nextInt(MAXGUNS);
            }
            if(level < 8) {
              Image img = new ImageIcon(getClass().getResource("snaglevel" + level + imgSnag + ".png")).getImage();
              int speedSnag = rnd.nextInt(level - 1);
              if(speedSnag < 1) speedSnag = 1;
              snags.add(new GameEntry(xSnag, ySnag, widthSnag, heightSnag, speedSnag, img));
            }
          } else {
            for(int i = 0; i < snags.size(); i++) {
              if(snags.get(i).y >= maxYSnag) {
                snags.remove(i);
                continue;
              } else if(shot.isVisible) {
                if(shot.x + shot.speed < snags.get(i).x && shot.x + shot.width > snags.get(i).x) {
                  if(shot.y < snags.get(i).y + snags.get(i).height && shot.y + shot.height > snags.get(i).y) {
                    isAlowFire = true;
                    shot.isVisible = !isAlowFire;
                    chanceCount--;
                    playReload();
                    snags.remove(i);
                    continue;
                  }
                }
              }
              snags.get(i).y += snags.get(i).speed;
            }
          }
        timerSnags++;
        }
        if(!isNewState && (blowGun.y < minYGun || blowGun.y > maxYGun))
          blowGun.speed = blowGun.speed * -1;
        if(target.y < minYTarget || target.y > maxYTarget)
          target.speed = target.speed * -1;
        if(!isAlowFire) {
          if((shot.x + shot.speed < target.x + target.width / 2 + 7 &&
                  shot.x > (target.x + 10) + target.width / 2 - 20) &&
                  (shot.y + shot.height / 2 >= target.y &&
                  shot.y + shot.height / 2 <= target.y + target.height)) {
            isAlowFire = true;
            showNewPoints.x = shot.x;
            showNewPoints.y = target.y;
            addPoints(shot.y + shot.height / 2, target.y);
            playHit();
          } else if(shot.x < -100) {
            isAlowFire = true;
            shot.isVisible = !isAlowFire;
            chanceCount--;
            playReload();
          }
          else
            shot.x += shot.speed;
        }
        if(!isNewState) blowGun.y += blowGun.speed;
        target.y += target.speed;

        if(showNewPoints.isVisible) {
          if(pointsAlpha < 1) {
            showNewPoints.isVisible = !showNewPoints.isVisible;
          } else {
            pointsColor = new Color(pointsColor.getRed(), pointsColor.getGreen(), pointsColor.getBlue(), pointsAlpha);
            showNewPoints.y -= showNewPoints.speed;
            pointsAlpha -= 7;
          }
        }
      }
      repaint();
    }
  }
  
  private class readPoints {
    private int[] codes = {101,202,501,240,332,42,111,555,444,511,55};
//    private int[] codes = {0,0,0};

    private int index;
    private String f;
    
    public readPoints() {
      f = "spm.dat";
    }
    
    public int getPoints() {
      int intReturn = 0;
      try {
        int read = 0;
        String str = "";
        FileReader fr = new FileReader(f);
        while((read = fr.read()) != -1)
          str += read;
        for(int i = 0; i < str.length(); i++) {
          if(index >= codes.length) index = 0;
          intReturn += (int)(str.charAt(i) + codes[index]);
          index++;
        }
        index = 0;
        fr.close();
      } catch (Exception ex) {}
      
      return intReturn;
    }
    
    public void setPoints(int value) {
      try {
        if(value < bestPoints) return;
        FileWriter fw = new FileWriter(f);
        String strValue = "" + value;
        int b = 0;
        for(int i = 0; i < strValue.length(); i++) {
          if(index > codes.length) index++;
          b = (int) (strValue.charAt(i) - codes[index]);
          fw.write(b);
          index++;
        }
        index = 0;
        fw.close();
      } catch (Exception ex) {}
      
    }
  }
  
  private class GameEntry {
    Image img;
    int x,y,width,height,speed;
    boolean isVisible,isGun;
    String text;
    
    public GameEntry() { isVisible = true; }
    public GameEntry(int x,int y,int width,int height) {
      isVisible = true;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      isVisible = true;
    }
    public GameEntry(int x,int y,int width,int height,Image img) {
      isVisible = true;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.img = img;
      isVisible = true;
    }
    public GameEntry(int x,int y,int width,int height,int speed,Image img) {
      isVisible = true;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.speed = speed;
      this.img = img;
      isVisible = true;
      isGun = newGun;
      newGun = false;
    }
  }
}
