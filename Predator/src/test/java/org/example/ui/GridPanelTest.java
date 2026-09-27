package org.example.ui;

import org.example.model.*;
import org.example.util.MazeLoader;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GridPanelTest {
    static { System.setProperty("java.awt.headless", "true"); }
    @Test void centeredBoardEditingAndResetPreserveScenario() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            GridPanel panel=new GridPanel();
            panel.setSize(600,400); // 38.4px cells, horizontal offset 108.
            panel.setScenario(new MazeLoader.Scenario(new Grid(10,10),new Position(1,1),List.of(new Position(8,8))));
            press(panel,204,104); // cell (2,2), accounting for centering.
            assertFalse(panel.scenario().grid().walkable(new Position(2,2)));
            press(panel,204,104);
            assertTrue(panel.scenario().grid().walkable(new Position(2,2)));
            press(panel,10,104); // margin must not edit cell zero.
            assertTrue(panel.scenario().grid().walkable(new Position(0,2)));
            press(panel,165,65); // A
            panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_DRAGGED,0,MouseEvent.BUTTON1_DOWN_MASK,242,142,0,false));
            assertEquals(new Position(3,3),panel.scenario().start());
            panel.clearSearch();
            assertEquals(new Position(3,3),panel.scenario().start());
            assertEquals(new Position(8,8),panel.scenario().goals().getFirst());
            panel.setEditing(false);
            press(panel,204,104);
            assertTrue(panel.scenario().grid().walkable(new Position(2,2)));
        });
    }
    private static void press(GridPanel panel,int x,int y){
        panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_RELEASED,0,0,x,y,1,false,MouseEvent.BUTTON1));
        panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_PRESSED,0,0,x,y,1,false,MouseEvent.BUTTON1));
    }
}
