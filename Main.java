import java.awt.EventQueue;
import javax.swing.JFrame;

public class Main {

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        JFrame frame = new JFrame();
        Window window = new Window();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(window);
        frame.pack();
        frame.setTitle("Ray Caster");
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addKeyListener(window);
      }
    });
  }
}