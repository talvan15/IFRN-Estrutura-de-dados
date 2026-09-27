package org.example;
import javax.swing.SwingUtilities;
import org.example.ui.*;
public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> {
            Theme.install();new MainFrame().setVisible(true);
        }
        );
    }
}
