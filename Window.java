import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Window extends JPanel implements ActionListener, KeyListener {
  private static final int WIDTH = 1024;
  private static final int HEIGHT = 512;
  private final BufferedImage bufferedImage;
  private final JLabel jLabel = new JLabel();
  private final Timer timer = new Timer(10, this);
  private int playerX = 150;
  private int playerY = 400;
  private int mapX = 8;
  private int mapY = 8;
  private int mapSize = 64;
  private int[] map = {
    1, 1, 1, 1, 1, 1, 1, 1,
    1, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 0, 1,
    1, 1, 1, 1, 1, 1, 1, 1
  };
  private double playerAngle = 90;
  private double playerDeltaX = Math.cos(Math.toRadians(playerAngle));
  private double playerDeltaY = -Math.sin(Math.toRadians(playerAngle));

  public Window() {
      super(true);
      this.setLayout(new GridLayout());
      this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
      bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
      jLabel.setIcon(new ImageIcon(bufferedImage));
      this.add(jLabel);
      timer.start();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    clear();
    drawMap2D();
    drawRays3D();
    drawPlayer();
    jLabel.repaint();
  }
  
  private void clear() {
    Graphics2D g = bufferedImage.createGraphics();
    g.setColor(Color.gray);
    g.fillRect(0, 0, WIDTH, HEIGHT);
    g.dispose();
  }
  
  private void drawMap2D() {
    Graphics2D g = bufferedImage.createGraphics();
    for (int y = 0; y < mapY; y++) {
      for (int x = 0; x < mapX; x++) {
        int xOffset = x * mapSize;
        int yOffset = y * mapSize;
        if (map[y * mapX + x] == 1) {
          g.setColor(Color.white);
        }
        else {
          g.setColor(Color.black);
        }
        g.fillRect(xOffset + 1, yOffset + 1, mapSize - 1, mapSize - 1);
      }
    }
    g.dispose();
  }

  private double fixAngle(double angle) {
    if (angle > 359) {
      angle -= 360;
    }
    if (angle < 0) {
      angle += 360;
    }
    return angle;
  }

  private void drawPlayer() {
    Graphics2D g = bufferedImage.createGraphics();
    g.setColor(Color.yellow);
    g.fillRect(playerX - 4, playerY - 4, 8, 8);
    g.draw(new Line2D.Double(playerX, playerY, playerX + playerDeltaX * 20, playerY + playerDeltaY * 20));
    g.dispose();
  }

  private double distance(double aX, double aY, double bX, double bY, double angle) {
    return Math.cos(Math.toRadians(angle)) * (bX - aX) - Math.sin(Math.toRadians(angle)) * (bY - aY);
  }

  public void keyPressed(KeyEvent event) {
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
    int xGridPosition = playerX / 64;
    int xOffsetAddGridPosition = (playerX + xOffset) / 64;
    int xOffsetSubtractGridPosition = (playerX - xOffset) / 64;
    int yGridPosition = playerY / 64;
    int yOffsetAddGridPosition = (playerY + yOffset) / 64;
    int yOffsetSubtractGridPosition = (playerY - yOffset) / 64;
    switch (event.getKeyCode()) {
      case KeyEvent.VK_W:
        if (map[yGridPosition * mapX + xOffsetAddGridPosition] == 0) {
          playerX += playerDeltaX * 5;
        }
        if (map[yOffsetAddGridPosition * mapX + xGridPosition] == 0) {
          playerY += playerDeltaY * 5;
        }
        break;
      case KeyEvent.VK_S:
        if (map[yGridPosition * mapX + xOffsetSubtractGridPosition] == 0) {
          playerX -= playerDeltaX * 5;
        }
        if (map[yOffsetSubtractGridPosition * mapX + xGridPosition] == 0) {
          playerY -= playerDeltaY * 5;
        }
        break;
      case KeyEvent.VK_A:
        playerAngle += 5;
        playerAngle = fixAngle(playerAngle);
        playerDeltaX = Math.cos(Math.toRadians(playerAngle));
        playerDeltaY = -Math.sin(Math.toRadians(playerAngle));
        break;
      case KeyEvent.VK_D:
        playerAngle -= 5;
        playerAngle = fixAngle(playerAngle);
        playerDeltaX = Math.cos(Math.toRadians(playerAngle));
        playerDeltaY = -Math.sin(Math.toRadians(playerAngle));
        break;
    }
  }
  public void keyReleased(KeyEvent event) {}
  public void keyTyped(KeyEvent event) {}

  private void drawRays3D() {
    Graphics2D g = bufferedImage.createGraphics();
    g.setColor(Color.MAGENTA);
    g.drawRect(526, 0, 1006, 160);
    g.drawRect(526, 160, 1006, 320);
    int mapArrayX;
    int mapArrayY;
    int mapPosition;
    int depthOfField;
    int side;
    double rayX = 0;
    double rayY = 0;
    double rayAngle = fixAngle(playerAngle + 30);
    double xOffset = 0;
    double yOffset = 0;
    double verticalX = 0;
    double verticalY = 0;
    for (int rays = 0; rays < 60; rays++) {
      depthOfField = 0;
      side = 0;
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
        mapPosition = mapArrayY * mapX + mapArrayX;
        if (mapPosition > 0 && mapPosition < mapX * mapY && map[mapPosition] == 1) {
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
        mapPosition = mapArrayY * mapX + mapArrayX;
        if (mapPosition > 0 && mapPosition < mapX * mapY && map[mapPosition] == 1) {
          depthOfField = 8;
          horizontalDistance = Math.cos(Math.toRadians(rayAngle)) * (rayX - playerX) - Math.sin(Math.toRadians(rayAngle)) * (rayY - playerY);
        }
        else {
          rayX += xOffset;
          rayY += yOffset;
          depthOfField++;
        }
      }
      
      if (verticalDistance < horizontalDistance) {
        rayX = verticalX;
        rayY = verticalY;
        horizontalDistance = verticalDistance;
        g.setColor(new Color(0, 0, 255));
      }
      if (horizontalDistance < verticalDistance) {
        g.setColor(new Color(0, 0, 150));
      }
      g.draw(new Line2D.Double(playerX, playerY, rayX, rayY));
      
      double cameraAngle = fixAngle(playerAngle - rayAngle);
      horizontalDistance = horizontalDistance * Math.cos(Math.toRadians(cameraAngle));
      int horizontalLine = (int)((mapSize * 320) / (horizontalDistance));
      if (horizontalLine > 320) {
        horizontalLine = 320;
      }
      int lineOff = 160 - (horizontalLine >> 1);
      g.setStroke(new BasicStroke(8));
      g.drawLine(rays * 8 + 530, lineOff, rays * 8 + 530, lineOff+horizontalLine);
      g.setStroke(new BasicStroke(1));
      rayAngle = fixAngle((int)rayAngle - 1);
    }
    g.dispose();
  }
}