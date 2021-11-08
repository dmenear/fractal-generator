package com.menear;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Iterator;

class ControlPanel extends VBox {

    private static final Logger LOG = LoggerFactory.getLogger(ControlPanel.class);

    private Stage mainStage;
    private FractalCanvas fractalCanvas;

    private Button btnAction = new Button();
    private Button btnAddShape = new Button("Add Shape");

    private Label lblIterations = new Label("Iterations: ");
    private TextField txtIterations = new TextField("20000");
    private HBox hbIterations = new HBox(lblIterations, txtIterations);
    private int iterations = 20000;

    private Label lblDelay = new Label("Delay (ms): ");
    private Slider sldrDelay = new Slider(0.0, 10.0, 1.0);
    private HBox hbDelay = new HBox(lblDelay, sldrDelay);
    private SimpleDoubleProperty delayValue = new SimpleDoubleProperty();

    private HBox hbMainControls = new HBox();
    private FlowPane fpShapeWeights = new FlowPane();

    private EventHandler<ActionEvent> handleStart = action -> {
        if(parseNumericFields()) {
            btnAction.setOnAction(null);
            disableActionButton();
            disableAddShapeButton();
            disableWeightFields();
            disableIterationControls();
            fractalCanvas.beginStartPointSelection();
        }
    };

    private EventHandler<ActionEvent> handleStop = action -> fractalCanvas.cancelDrawPoints();

    private EventHandler<ActionEvent> handleReset = action -> {
        fractalCanvas.resetCanvas();
        btnAction.setOnAction(handleStart);
    };

    ControlPanel(Stage mainStage) {
        this.mainStage = mainStage;

        hbIterations.setSpacing(3.0);
        hbIterations.setAlignment(Pos.CENTER);
        txtIterations.setPrefColumnCount(4);

        hbDelay.setSpacing(3.0);
        hbDelay.setAlignment(Pos.CENTER);
        sldrDelay.setShowTickMarks(true);
        sldrDelay.setShowTickLabels(true);
        sldrDelay.setSnapToTicks(true);
        sldrDelay.setMajorTickUnit(1.0);
        sldrDelay.setMinorTickCount(0);
        delayValue.bind(sldrDelay.valueProperty());

        hbMainControls.setSpacing(3.0);
        hbMainControls.setAlignment(Pos.CENTER);

        fpShapeWeights.setHgap(12.0);
        fpShapeWeights.setVgap(5.0);
        fpShapeWeights.setAlignment(Pos.CENTER);

        hbMainControls.getChildren().addAll(btnAction, btnAddShape, new Separator(Orientation.VERTICAL), hbIterations);
        hbMainControls.getChildren().addAll(new Separator(Orientation.VERTICAL), hbDelay);

        setPadding(new Insets(5.0, 10.0, 5.0, 10.0));
        setSpacing(8.0);
        setAlignment(Pos.CENTER);
        getChildren().addAll(hbMainControls, fpShapeWeights);

        actionButtonStart();
        btnAddShape.setOnAction(event -> fractalCanvas.addShape());
        disableAddShapeButton();
    }

    void init(FractalCanvas fractalCanvas) {
        this.fractalCanvas = fractalCanvas;
    }

    int getIterations() {
        return iterations;
    }

    int getDelay() {
        return (int) delayValue.get();
    }

    void renderShapeWeightFields() {
        fpShapeWeights.getChildren().clear();
        Deque<SelectedShape> selectedShapes = fractalCanvas.getSelectedShapes();
        if(selectedShapes.size() > 1) {
            Iterator<SelectedShape> iter = selectedShapes.descendingIterator();
            while(iter.hasNext()) {
                SelectedShape selectedShape = iter.next();
                HBox hbSelectedWeight = new HBox(selectedShape.getLblWeight(), selectedShape.getTxtWeight());
                hbSelectedWeight.setAlignment(Pos.CENTER);
                hbSelectedWeight.setSpacing(3.0);
                fpShapeWeights.getChildren().add(hbSelectedWeight);
            }
        }
        mainStage.sizeToScene();
    }

    void enableIterationControls() {
        txtIterations.setDisable(false);
        sldrDelay.setDisable(false);
    }

    void disableIterationControls() {
        txtIterations.setDisable(true);
        sldrDelay.setDisable(true);
    }

    void disableWeightFields() {
        for(SelectedShape selectedShape : fractalCanvas.getSelectedShapes()) {
            selectedShape.getTxtWeight().setDisable(true);
        }
    }

    void enableActionButton() {
        btnAction.setDisable(false);
    }

    void disableActionButton() {
        btnAction.setDisable(true);
    }

    void actionButtonStop() {
        enableActionButton();
        btnAction.setText("Stop");
        btnAction.setOnAction(handleStop);
        LOG.debug("Stop button enabled.");
    }

    void actionButtonReset() {
        btnAction.setText("Reset");
        btnAction.setOnAction(handleReset);
        LOG.debug("Reset button enabled.");
    }

    void actionButtonStart() {
        disableActionButton();
        enableIterationControls();
        btnAction.setText("Start");
        btnAction.setOnAction(handleStart);
        LOG.debug("Start button enabled.");
    }

    void enableAddShapeButton() {
        btnAddShape.setDisable(false);
    }

    void disableAddShapeButton() {
        btnAddShape.setDisable(true);
    }

    private boolean parseNumericFields() {
        try {
            iterations = Integer.parseInt(txtIterations.getText());
            txtIterations.setText(String.valueOf(iterations));
        } catch(NumberFormatException e) {
            showInvalidNumberMessage(txtIterations.getText());
            return false;
        }

        Deque<SelectedShape> selectedShapes = fractalCanvas.getSelectedShapes();
        if(selectedShapes.size() > 1) {
            for (SelectedShape selectedShape : selectedShapes) {
                String numberString = selectedShape.getTxtWeight().getText();
                try {
                    double weight = Double.parseDouble(numberString);
                    selectedShape.getTxtWeight().setText(String.valueOf(weight));
                    selectedShape.setWeight(weight);
                } catch (NumberFormatException e) {
                    showInvalidNumberMessage(numberString);
                    return false;
                }
            }
        }
        return true;
    }

    private void showInvalidNumberMessage(String value) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error! Invalid number!");
        alert.setContentText(String.format("Value \"%s\" is not a valid number!", value));
        alert.showAndWait();
    }

}
