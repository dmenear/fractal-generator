package com.menear;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

class MainMenu extends MenuBar {

    private static final Logger LOG = LoggerFactory.getLogger(MainMenu.class);

    private Menu fileMenu = new Menu("File");
    private MenuItem miImportRuleSet = new MenuItem("Import Rule Set...");
    private MenuItem miExportRuleSet = new MenuItem("Export Rule Set...");
    private MenuItem miSaveCanvas = new MenuItem("Save Canvas as Image...");
    private MenuItem miExit = new MenuItem("Exit");

    private FileChooser fcImportRuleSet = new FileChooser();
    private FileChooser fcExportRuleSet = new FileChooser();
    private FileChooser fcSaveCanvas = new FileChooser();

    private Menu viewMenu = new Menu("View");
    private MenuItem miSmallCanvas = new MenuItem("Small Canvas");
    private MenuItem miMediumCanvas = new MenuItem("Medium Canvas");
    private MenuItem miLargeCanvas = new MenuItem("Large Canvas");
    private MenuItem miSelectedCanvasSize;

    private EventHandler<ActionEvent> handleImportRuleSet = event -> {
        File ruleSetFile = fcImportRuleSet.showOpenDialog(Fractals.getMainStage());
        if(ruleSetFile != null) {
            LOG.info("Selected the following file for rule set import: " + ruleSetFile.getAbsolutePath());
        } else {
            LOG.info("No file selected for rule set import!");
        }
    };

    private EventHandler<ActionEvent> handleSaveCanvas = event -> {
        File imageFile = fcSaveCanvas.showSaveDialog(Fractals.getMainStage());
        if(imageFile != null) {
            LOG.info("Selected the following location to save image file: " + imageFile.getAbsolutePath());

            Canvas canvas = Fractals.getFractalCanvas().getCanvas();
            WritableImage canvasSnapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, canvasSnapshot);
            RenderedImage canvasImg = SwingFXUtils.fromFXImage(canvasSnapshot, null);
            try {
                ImageIO.write(canvasImg, "png", imageFile);
                LOG.info("Successfully saved image file!");
            } catch(IOException e) {
                LOG.error("Failed to save image file!", e);
                Alert failedToSaveAlert = new Alert(Alert.AlertType.ERROR);
                failedToSaveAlert.setTitle("Error");
                failedToSaveAlert.setHeaderText(null);
                failedToSaveAlert.setContentText("An unexpected error occurred while trying to save image to "
                        + imageFile.getAbsolutePath() + ": " + e.getMessage());
                failedToSaveAlert.showAndWait();
            }
        } else {
            LOG.info("No output location specified for input file!");
        }
    };

    private EventHandler<ActionEvent> handleCanvasSizeChange = event -> {
        Alert confirmSizeChange = new Alert(Alert.AlertType.CONFIRMATION);
        confirmSizeChange.setTitle("Confirm Canvas Size Change");
        confirmSizeChange.setHeaderText(null);
        confirmSizeChange.setContentText("WARNING! Changing canvas size will remove all current selections! "
                + "Do you wish to continue?");
        confirmSizeChange.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        ((Button) confirmSizeChange.getDialogPane().lookupButton(ButtonType.YES)).setDefaultButton(false);
        ((Button) confirmSizeChange.getDialogPane().lookupButton(ButtonType.NO)).setDefaultButton(true);

        Optional<ButtonType> response = confirmSizeChange.showAndWait();
        if(response.isPresent() && response.get() == ButtonType.YES) {
            LOG.info("User confirmed canvas size change");
            miSelectedCanvasSize = (MenuItem) event.getTarget();
            enableCanvasSizeSelection();

            if(miSelectedCanvasSize == miSmallCanvas) {
                Fractals.getFractalCanvas().resizeCanvasSmall();
                LOG.info("Canvas size set to small");
            } else if(miSelectedCanvasSize == miMediumCanvas) {
                Fractals.getFractalCanvas().resizeCanvasMedium();
                LOG.info("Canvas size set to medium");
            } else if(miSelectedCanvasSize == miLargeCanvas) {
                Fractals.getFractalCanvas().resizeCanvasLarge();
                LOG.info("Canvas size set to large");
            }

            Fractals.getMainStage().sizeToScene();
        }
    };

    MainMenu() {
        miImportRuleSet.setOnAction(handleImportRuleSet);
        miSaveCanvas.setOnAction(handleSaveCanvas);

        miSmallCanvas.setOnAction(handleCanvasSizeChange);
        miMediumCanvas.setOnAction(handleCanvasSizeChange);
        miLargeCanvas.setOnAction(handleCanvasSizeChange);

        miExit.setOnAction(event -> confirmExit());
        Fractals.getMainStage().setOnCloseRequest(event -> {
            event.consume();
            confirmExit();
        });

        configureFileChoosers();

        miMediumCanvas.setDisable(true);
        fileMenu.getItems().addAll(miImportRuleSet, miExportRuleSet, miSaveCanvas, new SeparatorMenuItem(), miExit);
        viewMenu.getItems().addAll(miSmallCanvas, miMediumCanvas, miLargeCanvas);

        getMenus().addAll(fileMenu, viewMenu);
    }

    void enableCanvasSizeSelection() {
        miSmallCanvas.setDisable(false);
        miMediumCanvas.setDisable(false);
        miLargeCanvas.setDisable(false);
        miSelectedCanvasSize.setDisable(true);
    }

    void disableCanvasSizeSelection() {
        miSmallCanvas.setDisable(true);
        miMediumCanvas.setDisable(true);
        miLargeCanvas.setDisable(true);
    }

    private void confirmExit() {
        Alert confirmExitAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmExitAlert.setTitle("Confirm Exit");
        confirmExitAlert.setHeaderText(null);
        confirmExitAlert.setContentText("Are you sure you want to exit?");
        confirmExitAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((Button) confirmExitAlert.getDialogPane().lookupButton(ButtonType.YES)).setDefaultButton(false);
        ((Button) confirmExitAlert.getDialogPane().lookupButton(ButtonType.NO)).setDefaultButton(true);

        Optional<ButtonType> response = confirmExitAlert.showAndWait();
        if(response.isPresent() && response.get() == ButtonType.YES) {
            Platform.exit();
            System.exit(0);
        }
    }

    private void configureFileChoosers() {
        fcImportRuleSet.setTitle("Select RuleSet File...");
        fcImportRuleSet.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON File Format", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        fcSaveCanvas.setTitle("Choose Location to Save Image...");
        fcSaveCanvas.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG File", ".png")
        );
    }

}
