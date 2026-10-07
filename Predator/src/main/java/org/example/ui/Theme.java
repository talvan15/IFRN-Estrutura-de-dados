package org.example.ui;
import java.awt.*;
import javax.swing.*;
public final class Theme {
    public static final Color BACKGROUND=new Color(0x121214),SURFACE=new Color(0x1E1E2E),
        CARD=new Color(0x252536),GRID=new Color(0x2D2D3A),WALL=new Color(0x454553),
        BLUE=new Color(0x367BF5),ORANGE=new Color(0xC85A24),YELLOW=new Color(0xFFB800),
        CYAN=new Color(0x75B4FF),PURPLE=new Color(0xC4AAFF),GREEN=new Color(0x59D49A),
        TEXT=new Color(0xF0F0F5),MUTED=new Color(0xA0A0B5);
    public static void install() {
        Font font=new Font(Font.SANS_SERIF,Font.PLAIN,13);
        for(String key:new String[] {
            "Label.font","Button.font","ToggleButton.font","CheckBox.font","ComboBox.font","Table.font","TableHeader.font","ToolTip.font"
        }
        )UIManager.put(key,font);
        UIManager.put("Button.focus",SURFACE);
        UIManager.put("ToggleButton.focus",SURFACE);
        UIManager.put("ToggleButton.select",BLUE);
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
