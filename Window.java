import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Window extends JPanel implements ActionListener, KeyListener {
  private static final int WIDTH = 960;
  private static final int HEIGHT = 640;
  private final BufferedImage bufferedImage;
  private final JLabel jLabel = new JLabel();
  private final Timer timer = new Timer(10, this);
  private int playerX = 150;
  private int playerY = 400;
  private int mapX = 8;
  private int mapY = 8;
  private int mapSize = 64;
  private Scanner textureColorFile;
  private int[] textureColors = new int[27648];
  private Scanner skyColorFile;
  private int[] skyColors = new int[28800];
  private ArrayList<Integer> keys = new ArrayList<Integer>();
  private ArrayList<ArrayList<Integer>> sprites = new ArrayList<ArrayList<Integer>>();

  private int[][] wallMap = {
    {1, 1, 1, 1, 1, 1, 2, 1},
    {1, 0, 1, 0, 0, 0, 2, 1},
    {1, 0, 1, 0, 0, 0, 4, 1},
    {1, 0, 1, 0, 0, 0, 3, 1},
    {1, 0, 0, 0, 0, 0, 0, 1},
    {1, 0, 0, 0, 0, 0, 0, 1},
    {1, 0, 0, 0, 0, 1, 0, 1},
    {1, 1, 1, 1, 1, 1, 1, 1}
  };

  private int[][] floorMap = {
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 1, 1, 0, 0},
    {0, 0, 0, 0, 2, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 2, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 1, 1, 1, 1, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0}
  };

  private int[][] ceilingMap = {
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 1, 0},
    {0, 1, 3, 1, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0}
  };
  private double playerAngle = 90;
  private double playerDeltaX = Math.cos(Math.toRadians(playerAngle));
  private double playerDeltaY = -Math.sin(Math.toRadians(playerAngle));

  private long lastTime = System.nanoTime();
  private int fps;
  private int frames;

  private int[] depth = new int[120];

  public Window() {
      super(true);
      this.setLayout(new GridLayout());
      this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
      bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
      jLabel.setIcon(new ImageIcon(bufferedImage));
      this.add(jLabel);
      appendTextures();
      sprites.add(new ArrayList<Integer>(Arrays.asList(1, 1, 0, (int)(1.5 * 64), 5 * 64, 20)));
      timer.start();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    movePlayer();
    clear();
    drawSky();
    drawScene();
    drawSprites();
    jLabel.repaint();
    frames++;
    if (System.nanoTime() > lastTime + 1000000000) {
      lastTime = System.nanoTime();
      fps = frames;
      frames = 0;
      System.out.println(fps);
    }
  }

  private void appendTextures() {
    int line;
    try {
      textureColorFile = new Scanner(new File("textures.txt"));
    }
    catch (FileNotFoundException error) {
      error.printStackTrace();
    }
    line = 0;
    while (textureColorFile.hasNextInt()) {
      textureColors[line++] = textureColorFile.nextInt();
    }
    
    try {
      skyColorFile = new Scanner(new File("sky.txt"));
    }
    catch (FileNotFoundException error) {
      error.printStackTrace();
    }
    line = 0;
    while (skyColorFile.hasNextInt()) {
      skyColors[line++] = skyColorFile.nextInt();
    }
    skyColorFile.close();
  }
  
  private void clear() {
    Graphics2D g = bufferedImage.createGraphics();
    g.setColor(new Color(50, 50, 50));
    g.fillRect(0, 0, WIDTH, HEIGHT);
    g.dispose();
  }

  private double fixAngle(double angle) {
    if (angle >= 360) {
      angle -= 360;
    }
    else if (angle < 0) {
      angle += 360;
    }
    return angle;
  }

  private void movePlayer() {
    if (keys.contains(KeyEvent.VK_A)) {
      playerAngle += 5;
      playerAngle = fixAngle(playerAngle);
      playerDeltaX = Math.cos(Math.toRadians(playerAngle));
      playerDeltaY = -Math.sin(Math.toRadians(playerAngle));
    }
    if (keys.contains(KeyEvent.VK_D)) {
      playerAngle -= 5;
      playerAngle = fixAngle(playerAngle);
      playerDeltaX = Math.cos(Math.toRadians(playerAngle));
      playerDeltaY = -Math.sin(Math.toRadians(playerAngle));
    }

    int xOffset = 0;
    int yOffset = 0;
    if (playerDeltaX < 0) {
      xOffset = -20;
    }
    else {
      xOffset = 20;
    }
    if (playerDeltaY < 0) {
      yOffset = -20;
    }
    else {
      yOffset = 20;
    }
    if (keys.contains(KeyEvent.VK_W)) {
      if (wallMap[(playerY / 64)][(playerX + xOffset) / 64] == 0) {
        playerX += playerDeltaX * 5;
      }
      if (wallMap[((playerY + yOffset) / 64)][(playerX / 64)] == 0) {
        playerY += playerDeltaY * 5;
      }
    }
    if (keys.contains(KeyEvent.VK_S)) {
      if (wallMap[(playerY / 64)][((playerX - xOffset) / 64)] == 0) {
        playerX -= playerDeltaX * 5;
      }
      if (wallMap[((playerY - yOffset) / 64)][(playerX / 64)] == 0) {
        playerY -= playerDeltaY * 5;
      }
    }
    if (keys.contains(KeyEvent.VK_E)) {
      if (wallMap[(playerY + yOffset) / 64][(playerX + xOffset) / 64] == 4) {
        wallMap[(playerY + yOffset) / 64][(playerX + xOffset) / 64] = 0;
      }
    }
  }

  public void keyPressed(KeyEvent event) {
    if (!keys.contains(event.getKeyCode())) {
      keys.add(event.getKeyCode());
    }
  }
  public void keyReleased(KeyEvent event) {
    if (keys.contains(event.getKeyCode())) {
      keys.remove(Integer.valueOf(event.getKeyCode()));
    }
  }
  public void keyTyped(KeyEvent event) {}

  private void drawScene() {
    Graphics2D g = bufferedImage.createGraphics();
    int mapArrayX;
    int mapArrayY;
    int mapPosition;
    int depthOfField;
    double rayX = 0;
    double rayY = 0;
    double rayAngle = fixAngle(playerAngle + 30);
    double xOffset = 0;
    double yOffset = 0;
    double verticalX = 0;
    double verticalY = 0;
    for (byte rays = 0; rays < 120; rays++) {
      int verticalMapTexture = 0;
      int horizontalMapTexture = 0;
      depthOfField = 0;
      double verticalDistance = 1000000;
      double tan = Math.tan(Math.toRadians(rayAngle));
      if (Math.cos(Math.toRadians(rayAngle)) > 0.001) {
        rayX = (((int)playerX >> 6) << 6) + 64;
        rayY = (playerX - rayX) * tan + playerY;
        xOffset = 64;
        yOffset = -xOffset * tan;
      }
      else if (Math.cos(Math.toRadians(rayAngle)) < -0.001) {
        rayX = (((int)playerX >> 6) << 6) - 0.0001;
        rayY = (playerX - rayX) * tan + playerY;
        xOffset = -64;
        yOffset = -xOffset * tan;
      }
      else {
        rayX = playerX;
        rayY = playerY;
        depthOfField = 8;
      }
      while (depthOfField < 8) {
        mapArrayX = (int)(rayX) >> 6;
        mapArrayY = (int)(rayY) >> 6;
        if (mapArrayY > 0 && mapArrayY < mapY && wallMap[mapArrayY][mapArrayX] > 0) {
          verticalMapTexture = wallMap[mapArrayY][mapArrayX] - 1;
          depthOfField = 8;
          verticalDistance = Math.cos(Math.toRadians(rayAngle)) * (rayX - playerX) - Math.sin(Math.toRadians(rayAngle)) * (rayY - playerY);
        }
        else {
          rayX += xOffset;
          rayY += yOffset;
          depthOfField++;
        }
        verticalX = rayX;
        verticalY = rayY;
      }

      depthOfField = 0;
      double horizontalDistance = 1000000;
      tan = 1 / tan;
      if (Math.sin(Math.toRadians(rayAngle)) > 0.001) {
        rayY = (((int)playerY >> 6) << 6) - 0.0001;
        rayX = (playerY - rayY) * tan + playerX;
        yOffset = -64;
        xOffset = -yOffset * tan;
      }
      else if (Math.sin(Math.toRadians(rayAngle)) < -0.001) {
        rayY = (((int)playerY >> 6) << 6) + 64;
        rayX = (playerY - rayY) * tan + playerX;
        yOffset = 64;
        xOffset = -yOffset * tan;
      }
      else {
        rayX = playerX;
        rayY = playerY;
        depthOfField = 8;
      }
      while (depthOfField < 8) {
        mapArrayX = (int)(rayX) >> 6;
        mapArrayY = (int)(rayY) >> 6;
        if (mapArrayX > 0 && mapArrayX < mapX && wallMap[mapArrayY][mapArrayX] > 0) {
          horizontalMapTexture = wallMap[mapArrayY][mapArrayX] - 1;
          depthOfField = 8;
          horizontalDistance = Math.cos(Math.toRadians(rayAngle)) * (rayX - playerX) - Math.sin(Math.toRadians(rayAngle)) * (rayY - playerY);
        }
        else {
          rayX += xOffset;
          rayY += yOffset;
          depthOfField++;
        }
      }
      

      float shade = 1;
      if (verticalDistance < horizontalDistance) {
        horizontalMapTexture = verticalMapTexture;
        rayX = verticalX;
        rayY = verticalY;
        horizontalDistance = verticalDistance;
        shade = 0.5f;
      }
      
      double cameraAngle = fixAngle(playerAngle - rayAngle);
      horizontalDistance = horizontalDistance * Math.cos(Math.toRadians(cameraAngle));
      int lineHeight = (int)((mapSize * 640) / (horizontalDistance));
      double textureYStep = 32.0 / lineHeight;
      double textureYOffset = 0;
      if (lineHeight > 640) {
        textureYOffset = (lineHeight - 640) / 2;
        lineHeight = 640;
      }
      int lineOffset = 320 - (lineHeight >> 1);

      depth[rays] = (int)horizontalDistance;

      double textureY = textureYOffset * textureYStep;
      double textureX;
      if (shade == 1) {
        textureX = (int)(rayX / 2) % 32;
        if (rayAngle > 180) {
          if (rayAngle > 180) {
            textureX = 31 - textureX;
          }
        }
      }
      else {
        textureX = (int)(rayY / 2) % 32;
        if (rayAngle > 90 && rayAngle < 270) {
          textureX = 31 - textureX;
        }
      }
      for (int y = 0; y < lineHeight; y++) {
        int pixel = ((int)textureY * 32 + (int)textureX) * 3 + (horizontalMapTexture * 32 * 32 * 3);
        double red = textureColors[pixel] * shade;
        double green = textureColors[pixel + 1] * shade;
        double blue = textureColors[pixel + 2] * shade;
        g.setColor(new Color((int)red, (int)green, (int)blue));
        g.fillRect(rays * 8, y + lineOffset, 8, 8);
        textureY += textureYStep;
      }
      for (int y = lineOffset + lineHeight; y < 640; y++) {
        double yDegrees = y - (640 / 2);
        double degrees = Math.toRadians(rayAngle);
        double rayAngleFix = Math.cos(Math.toRadians(fixAngle(playerAngle - rayAngle)));
        textureX = playerX / 2 + Math.cos(degrees) * 158 * 2 * 32 / yDegrees / rayAngleFix;
        textureY = playerY / 2 - Math.sin(degrees) * 158 * 2 * 32 / yDegrees / rayAngleFix;
        mapPosition = floorMap[(int)(textureY / 32)][(int)(textureX / 32)] * 32 * 32;
        int pixel = (((int)textureY & 31) * 32 + ((int)textureX & 31)) * 3 + mapPosition * 3;
        double red = textureColors[pixel] * 0.7;
        double green = textureColors[pixel + 1] * 0.7;
        double blue = textureColors[pixel + 2] * 0.7;
        g.setColor(new Color((int)red, (int)green, (int)blue));
        g.fillRect(rays * 8, y, 8, 1);
        textureY += textureYStep;
        
        mapPosition = ceilingMap[(int)(textureY / 32)][(int)(textureX / 32)] * 32 * 32;
        if (mapPosition > 0) {
          pixel = (((int)textureY & 31) * 32 + ((int)textureX & 31)) * 3 + mapPosition * 3;
          red = textureColors[pixel];
          green = textureColors[pixel + 1];
          blue = textureColors[pixel + 2];
          g.setColor(new Color((int)red, (int)green, (int)blue));
          g.fillRect(rays * 8, 640 - y, 8, 1);
        }
      }
      rayAngle = fixAngle(rayAngle - 0.5);
    }
    g.dispose();
  }

  private void drawSky() {
    Graphics2D g = bufferedImage.createGraphics();
    for (byte y = 0; y < 40; y++) {
      for (byte x = 0; x < 120; x++) {
        int xOffset = (int)playerAngle * 2 - x;
        if (xOffset < 0) {
          xOffset += 120;
        }
        xOffset = xOffset % 120;
        int pixel = (y * 120 + xOffset) * 3;
        int red = skyColors[pixel];
        int green = skyColors[pixel + 1];
        int blue = skyColors[pixel + 2];
        g.setColor(new Color((int)red, (int)green, (int)blue));
        g.fillRect(x * 8, y * 8, 8, 8);
      }
    }
    g.dispose();
  }

  private void drawSprites() {
    Graphics2D g = bufferedImage.createGraphics();
    double spriteX = sprites.get(0).get(3) - playerX;
    double spriteY = sprites.get(0).get(4) - playerY;
    double spriteZ = sprites.get(0).get(5);

    double cos = Math.cos(Math.toRadians(playerAngle));
    double sin = Math.sin(Math.toRadians(playerAngle));

    double a = spriteY * cos + spriteX * sin;
    double b = spriteX * cos - spriteY * sin;
    spriteX = a;
    spriteY = b;

    spriteX = (spriteX * 108.0 / spriteY) + (120 / 2);
    spriteY = (spriteZ * 108.0 / spriteY) + (80 / 2);

    int scale = (int)(32 * 80 / b);

    for (int x = (int)spriteX - scale / 2; x < (int)spriteX + scale / 2; x++) {
      for (int y = 0; y < scale; y++) {
        if (x > 0 && x < 120 && b < depth[x]) {
          g.setColor(Color.yellow);
          g.fill(new Rectangle2D.Double(x * 8, (spriteY - y) * 8, 8, 8));
        }
      }
    }
    
    g.dispose();
  }
}