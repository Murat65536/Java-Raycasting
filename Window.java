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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Window extends JPanel implements ActionListener, KeyListener {
  private static final short WIDTH = 960;
  private static final short HEIGHT = 640;
  private final BufferedImage bufferedImage;
  private final JLabel jLabel = new JLabel();
  private final Timer timer = new Timer(10, this);
  private short playerX = 150;
  private short playerY = 400;
  private byte mapX = 8;
  private byte mapY = 8;
  private short mapSize = 64;
  private Scanner textureColorFile;
  private short[] textureColors = new short[27648];
  private Scanner skyColorFile;
  private short[] skyColors = new short[28800];
  private ArrayList<Integer> keys = new ArrayList<Integer>();
  private ArrayList<ArrayList<Integer>> sprites = new ArrayList<ArrayList<Integer>>();

  private byte[][] wallMap = {
    {1, 1, 1, 1, 1, 1, 2, 1},
    {1, 0, 1, 0, 0, 0, 2, 1},
    {1, 0, 1, 0, 0, 0, 4, 1},
    {1, 0, 1, 0, 0, 0, 3, 1},
    {1, 0, 0, 0, 0, 0, 0, 1},
    {1, 0, 0, 0, 0, 0, 0, 1},
    {1, 0, 0, 0, 0, 1, 0, 1},
    {1, 1, 1, 1, 1, 1, 1, 1}
  };

  private byte[][] floorMap = {
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 1, 1, 0, 0},
    {0, 0, 0, 0, 2, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 2, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0},
    {0, 1, 1, 1, 1, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0}
  };

  private byte[][] ceilingMap = {
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

  private short[] depth = new short[120];

  public Window() {
      super(true);
      this.setLayout(new GridLayout());
      this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
      bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
      jLabel.setIcon(new ImageIcon(bufferedImage));
      this.add(jLabel);
      textureColors = getTextures("textures.txt");
      skyColors = getTextures("sky.txt");
      sprites.add(new ArrayList<Integer>(Arrays.asList(1, 1, 0, 2, 5, 20)));
      timer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      Graphics2D graphics = bufferedImage.createGraphics();
      movePlayer();
      clear(graphics);
      drawSky(graphics);
      drawScene(graphics);
      drawSprites(graphics);
      graphics.dispose();
      jLabel.repaint();
    }

    private short[] getTextures(String fileName) {
      short line = 0;
      File file = new File(fileName);
      short[] array;
      try {
        array = new short[(short)Files.lines(Paths.get(fileName)).count()];
        try {
          Scanner items = new Scanner(file);
          while (items.hasNextShort()) {
            array[line++] = items.nextShort();
          }
          items.close();
        }
        catch (FileNotFoundException error) {
          error.printStackTrace();
        }
        return array;
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }
  
  private void clear(Graphics2D graphics) {
    graphics.setColor(new Color(50, 50, 50));
    graphics.fillRect(0, 0, WIDTH, HEIGHT);
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
    if (keys.contains(KeyEvent.VK_LEFT)) {
      playerAngle = fixAngle(playerAngle + 1.5);
      playerDeltaX = Math.cos(Math.toRadians(playerAngle));
      playerDeltaY = -Math.sin(Math.toRadians(playerAngle));
    }
    if (keys.contains(KeyEvent.VK_RIGHT)) {
      playerAngle = fixAngle(playerAngle - 1.5);
      playerDeltaX = Math.cos(Math.toRadians(playerAngle));
      playerDeltaY = -Math.sin(Math.toRadians(playerAngle));
    }

    byte xOffset = 0;
    byte yOffset = 0;
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
        playerX += playerDeltaX * 3;
      }
      if (wallMap[((playerY + yOffset) / 64)][(playerX / 64)] == 0) {
        playerY += playerDeltaY * 3;
      }
    }
    if (keys.contains(KeyEvent.VK_S)) {
      if (wallMap[(playerY / 64)][((playerX - xOffset) / 64)] == 0) {
        playerX -= playerDeltaX * 3;
      }
      if (wallMap[((playerY - yOffset) / 64)][(playerX / 64)] == 0) {
        playerY -= playerDeltaY * 3;
      }
    }
    xOffset = 0;
    yOffset = 0;
    if (playerDeltaY < 0) {
      xOffset = -20;
    }
    else {
      xOffset = 20;
    }
    if (playerDeltaX < 0) {
      yOffset = -20;
    }
    else {
      yOffset = 20;
    }
    if (keys.contains(KeyEvent.VK_A)) {
      if (wallMap[(playerY / 64)][((playerX + xOffset) / 64)] == 0) {
        playerX += playerDeltaY * 3;
      }
      if (wallMap[((playerY - yOffset) / 64)][(playerX / 64)] == 0) {
        playerY -= playerDeltaX * 3;
      }
    }
    if (keys.contains(KeyEvent.VK_D)) {
      if (wallMap[(playerY / 64)][(playerX - xOffset) / 64] == 0) {
        playerX -= playerDeltaY * 3;
      }
      if (wallMap[((playerY + yOffset) / 64)][(playerX / 64)] == 0) {
        playerY += playerDeltaX * 3;
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

  private void drawScene(Graphics2D graphics) {
    short mapArrayX;
    short mapArrayY;
    short mapPosition;
    byte depthOfField;
    double rayX = 0;
    double rayY = 0;
    double rayAngle = fixAngle(playerAngle + 30);
    double xOffset = 0;
    double yOffset = 0;
    double verticalX = 0;
    double verticalY = 0;
    for (short rays = 0; rays < 120; rays++) {
      short verticalMapTexture = 0;
      short horizontalMapTexture = 0;
      depthOfField = 0;
      double verticalDistance = 1000000;
      double tan = Math.tan(Math.toRadians(rayAngle));
      if (Math.cos(Math.toRadians(rayAngle)) > 0.001) {
        rayX = ((playerX >> 6) << 6) + 64;
        rayY = (playerX - rayX) * tan + playerY;
        xOffset = 64;
        yOffset = -xOffset * tan;
      }
      else if (Math.cos(Math.toRadians(rayAngle)) < -0.001) {
        rayX = ((playerX >> 6) << 6) - 0.0001;
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
        mapArrayX = (short)((short)(rayX) >> 6);
        mapArrayY = (short)((short)(rayY) >> 6);
        if (mapArrayY > 0 && mapArrayY < mapY && wallMap[mapArrayY][mapArrayX] > 0) {
          verticalMapTexture = (short)(wallMap[mapArrayY][mapArrayX] - 1);
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
        rayY = ((playerY >> 6) << 6) - 0.0001;
        rayX = (playerY - rayY) * tan + playerX;
        yOffset = -64;
        xOffset = -yOffset * tan;
      }
      else if (Math.sin(Math.toRadians(rayAngle)) < -0.001) {
        rayY = ((playerY >> 6) << 6) + 64;
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
        mapArrayX = (short)((short)(rayX) >> 6);
        mapArrayY = (short)((short)(rayY) >> 6);
        if (mapArrayX > 0 && mapArrayX < mapX && wallMap[mapArrayY][mapArrayX] > 0) {
          horizontalMapTexture = (short)(wallMap[mapArrayY][mapArrayX] - 1);
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
      short lineHeight = (short)((mapSize * 640) / (horizontalDistance));
      double textureYStep = 32.0 / lineHeight;
      double textureYOffset = 0;
      if (lineHeight > 640) {
        textureYOffset = (lineHeight - 640) / 2;
        lineHeight = 640;
      }
      short lineOffset = (short)(320 - (lineHeight >> 1));

      depth[rays] = (short)horizontalDistance;

      double textureY = textureYOffset * textureYStep;
      double textureX;
      if (shade == 1) {
        textureX = (rayX / 2) % 32;
        if (rayAngle > 180) {
          if (rayAngle > 180) {
            textureX = 31 - textureX;
          }
        }
      }
      else {
        textureX = (rayY / 2) % 32;
        if (rayAngle > 90 && rayAngle < 270) {
          textureX = 31 - textureX;
        }
      }
      for (short y = 0; y < lineHeight; y++) {
        short pixel = (short)((short)((short)textureY * 32 + textureX) * 3 + ((short)horizontalMapTexture * 32 * 32 * 3));
        double red = textureColors[pixel] * shade;
        double green = textureColors[pixel + 1] * shade;
        double blue = textureColors[pixel + 2] * shade;
        graphics.setColor(new Color((short)red, (short)green, (short)blue));
        graphics.fillRect(rays * 8, y + lineOffset, 8, 8);
        textureY += textureYStep;
      }
      for (short y = (short)(lineOffset + lineHeight); y < 640; y++) {
        double yDegrees = y - (640 / 2);
        double degrees = Math.toRadians(rayAngle);
        double rayAngleFix = Math.cos(Math.toRadians(fixAngle(playerAngle - rayAngle)));
        textureX = playerX / 2 + Math.cos(degrees) * 158 * 2 * 32 / yDegrees / rayAngleFix;
        textureY = playerY / 2 - Math.sin(degrees) * 158 * 2 * 32 / yDegrees / rayAngleFix;
        mapPosition = (short)(floorMap[(short)(textureY / 32)][(short)(textureX / 32)] * 32 * 32);
        short pixel = (short)((((short)textureY & 31) * 32 + ((short)textureX & 31)) * 3 + mapPosition * 3);
        double red = textureColors[pixel] * 0.7;
        double green = textureColors[pixel + 1] * 0.7;
        double blue = textureColors[pixel + 2] * 0.7;
        graphics.setColor(new Color((short)red, (short)green, (short)blue));
        graphics.fillRect(rays * 8, y, 8, 1);
        textureY += textureYStep;
        
        mapPosition = (short)(ceilingMap[(short)(textureY / 32)][(short)(textureX / 32)] * 32 * 32);
        if (mapPosition > 0) {
          pixel = (short)((((short)textureY & 31) * 32 + ((short)textureX & 31)) * 3 + mapPosition * 3);
          red = textureColors[pixel];
          green = textureColors[pixel + 1];
          blue = textureColors[pixel + 2];
          graphics.setColor(new Color((short)red, (short)green, (short)blue));
          graphics.fillRect(rays * 8, 640 - y, 8, 1);
        }
      }
      rayAngle = fixAngle(rayAngle - 0.5);
    }
  }

  private void drawSky(Graphics2D graphics) {
    for (byte y = 0; y < 40; y++) {
      for (byte x = 0; x < 120; x++) {
        short xOffset = (short)(playerAngle * 2 - x);
        if (xOffset < 0) {
          xOffset += 120;
        }
        xOffset = (short)(xOffset % 120);
        short pixel = (short)((y * 120 + xOffset) * 3);
        short red = skyColors[pixel];
        short green = skyColors[pixel + 1];
        short blue = skyColors[pixel + 2];
        graphics.setColor(new Color(red, green, blue));
        graphics.fillRect(x * 8, y * 8, 8, 8);
      }
    }
  }

  private void drawSprites(Graphics2D graphics) {
    double spriteX = sprites.get(0).get(3) * 64 - playerX;
    double spriteY = sprites.get(0).get(4) * 64 - playerY;
    double spriteZ = sprites.get(0).get(5);

    double cos = Math.cos(Math.toRadians(playerAngle));
    double sin = Math.sin(Math.toRadians(playerAngle));
    
    double rotationX = spriteY * cos + spriteX * sin;
    double rotationY = spriteX * cos - spriteY * sin;
    
    double screenX = (rotationX * 108.0 / rotationY) + (120 / 2);
    double screenY = (spriteZ * 108.0 / rotationY) + (80 / 2);
    
    short scale = (short)(32 * 80 / rotationY);
    
    for (short x = (short)(screenX - scale / 2); x < (short)(screenX + scale / 2); x++) {
      for (short y = 0; y < scale; y++) {
        if (x > 0 && x < 120 && rotationY < depth[x]) {
          graphics.setColor(Color.yellow);
          graphics.fill(new Rectangle2D.Double(x * 8, (screenY - y) * 8, 8, 8));
        }
      }
    }
  }
}