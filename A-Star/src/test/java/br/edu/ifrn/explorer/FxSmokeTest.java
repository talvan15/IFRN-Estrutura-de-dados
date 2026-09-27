package br.edu.ifrn.explorer;

import br.edu.ifrn.explorer.model.Movement;
import br.edu.ifrn.explorer.ui.ExplorerApp;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

/** Optional integration check: opens a real JavaFX window, needs a graphical session. */
@EnabledIfSystemProperty(named = "atlas.uiTest", matches = "true")
class FxSmokeTest {
    private Stage stage;
    private ExplorerApp app;
    @Test void realWindowComparisonPauseStepResumeAndReset() throws Exception {
        Platform.startup(() -> { });
        try {
            fx(() -> { app = new ExplorerApp(); stage = new Stage(); app.start(stage); return null; });
            fx(() -> {
                assertTrue(stage.isShowing());
                button("clear").fire(); check("compare").fire();
                button("step").fire(); assertTrue(button("clear").isDisabled()); return null;
            });
            await(() -> !button("step").isDisabled());
            fx(() -> {
                assertEquals(2, stage.getScene().getRoot().lookupAll(".metric-value").stream().filter(n -> ((Label)n).getText().equals("1")).count());
                ((Slider)stage.getScene().lookup("#speed")).setValue(120);
                button("run").fire(); button("pause").fire(); assertFalse(button("run").isDisabled());
                button("run").fire(); return null;
            });
            await(() -> stage.getScene().getRoot().lookupAll(".state").stream().allMatch(n -> ((Label)n).getText().contains("chegou a B")));
            fx(() -> {
                assertTrue(button("run").isDisabled());
                assertEquals(2, stage.getScene().getRoot().lookupAll(".metric-value").stream().filter(n -> ((Label)n).getText().equals("220")).count());
                saveScreenshot();
                button("reset").fire(); assertFalse(button("clear").isDisabled());
                button("run").fire();
                @SuppressWarnings("unchecked") ComboBox<Movement> movement = (ComboBox<Movement>) stage.getScene().lookup("#movement");
                movement.setValue(Movement.FOUR); // invalidates pending work
                assertFalse(button("clear").isDisabled());
                assertTrue(stage.getScene().getRoot().lookupAll(".state").stream().allMatch(n -> ((Label)n).getText().contains("Pronto")));
                return null;
            });
            // Queue a final FX turn: stale worker completion must not restore a discarded result.
            fx(() -> { assertFalse(button("clear").isDisabled()); return null; });
        } finally {
            fx(() -> { if (app != null) app.stop(); if (stage != null) stage.close(); return null; });
            Platform.exit();
        }
    }
    private Button button(String id) { return (Button) stage.getScene().lookup("#" + id); }
    private CheckBox check(String id) { return (CheckBox) stage.getScene().lookup("#" + id); }
    private static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action); Platform.runLater(task); return task.get(10, TimeUnit.SECONDS);
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (!fx(condition::getAsBoolean)) {
            if (System.nanoTime() > deadline) fail("Interface não alcançou o estado esperado em 15 s");
            Thread.sleep(40);
        }
    }
    private void saveScreenshot() throws Exception {
        WritableImage shot = stage.getScene().snapshot(null);
        BufferedImage image = new BufferedImage((int)shot.getWidth(), (int)shot.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) image.setRGB(x, y, shot.getPixelReader().getArgb(x, y));
        ImageIO.write(image, "png", new File("target/ui-smoke.png"));
    }
}
