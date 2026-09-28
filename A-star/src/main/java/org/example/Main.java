package org.example;

import org.example.ui.MainWindow;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            configureInterface();

            MainWindow window =
                    new MainWindow();

            window.setVisible(true);
        });
    }

    private static void configureInterface() {


        UIManager.put(
                "Button.background",
                new Color(31, 41, 55)
        );

        UIManager.put(
                "Button.foreground",
                new Color(243, 244, 246)
        );

        UIManager.put(
                "Button.select",
                new Color(55, 65, 81)
        );

        UIManager.put(
                "ComboBox.background",
                new Color(31, 41, 55)
        );

        UIManager.put(
                "ComboBox.foreground",
                new Color(243, 244, 246)
        );

        UIManager.put(
                "ComboBox.selectionBackground",
                new Color(59, 130, 246)
        );

        UIManager.put(
                "ComboBox.selectionForeground",
                Color.WHITE
        );

        UIManager.put(
                "ToolTip.background",
                new Color(31, 41, 55)
        );

        UIManager.put(
                "ToolTip.foreground",
                new Color(243, 244, 246)
        );
    }
}