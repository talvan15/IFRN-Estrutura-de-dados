package br.edu.ifrn.rotaminima.app;

import br.edu.ifrn.rotaminima.ui.MainFrame;
import java.awt.*;
import javax.swing.*;

public final class Main {
  private Main() {}

  public static void main(String[] args) {
    if (GraphicsEnvironment.isHeadless()) {
      System.err.println("RotaMinima precisa de um ambiente gráfico para abrir a interface Swing.");
      return;
    }
    SwingUtilities.invokeLater(
        () -> {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          } catch (Exception ignored) {
          }
          UIManager.put("Label.font", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
          new MainFrame().setVisible(true);
        });
  }
}
