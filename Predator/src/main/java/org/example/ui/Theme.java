package org.example.ui;
import java.awt.*;
import javax.swing.*;
public final class Theme {
    public static final Color BACKGROUND=new Color(0x0d1117),SURFACE=new Color(0x161e2b),CYAN=new Color(0x67c8d5),PURPLE=new Color(0xb8a0e5),GREEN=new Color(0x61f5ad),TEXT=new Color(0xdce6f2),MUTED=new Color(0x93a4bc);
    public static void install() {
        Font font=new Font(Font.SANS_SERIF,Font.PLAIN,13);
        for(String key:new String[] {
            "Label.font","Button.font","ToggleButton.font","CheckBox.font","ComboBox.font","Table.font","TableHeader.font","ToolTip.font"
        }
        )UIManager.put(key,font);
        UIManager.put("Button.focus",SURFACE);
        UIManager.put("ToggleButton.focus",SURFACE);
        UIManager.put("ToggleButton.select",new Color(0x286070));
        UIManager.put("Slider.background",BACKGROUND);
        UIManager.put("ScrollBar.background",SURFACE);
        UIManager.put("ScrollBar.thumb",new Color(0x39465b));
        UIManager.put("Panel.background",BACKGROUND);
        UIManager.put("Label.foreground",TEXT);
        UIManager.put("CheckBox.background",BACKGROUND);
        UIManager.put("CheckBox.foreground",TEXT);
        UIManager.put("Button.background",SURFACE);
        UIManager.put("Button.foreground",TEXT);
        UIManager.put("ToggleButton.background",SURFACE);
        UIManager.put("ToggleButton.foreground",TEXT);
        UIManager.put("ComboBox.background",SURFACE);
        UIManager.put("ComboBox.foreground",TEXT);
        UIManager.put("Table.background",SURFACE);
        UIManager.put("Table.foreground",TEXT);
        UIManager.put("Table.selectionBackground",new Color(0x30566b));
        UIManager.put("TableHeader.background",SURFACE);
        UIManager.put("TableHeader.foreground",TEXT);
        UIManager.put("ScrollPane.background",BACKGROUND);
        UIManager.put("ToolTip.background",SURFACE);
        UIManager.put("ToolTip.foreground",TEXT);
    }
    public static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    private Theme() {
    }
}
