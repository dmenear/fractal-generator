package com.menear;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class ControlPanel extends VBox {

    private static final Logger LOG = LoggerFactory.getLogger(ControlPanel.class);

    private Button btnAction = new Button();

    private Label lblIterations = new Label("Iterations: ");
    private TextField txtIterations = new TextField("20000");
    private HBox hbIterations = new HBox(lblIterations, txtIterations);
    private int iterations = 20000;

    private Label lblDelay = new Label("Delay (ms): ");
    private Slider sldrDelay = new Slider(0.0, 10.0, 1.0);
    private HBox hbDelay = new HBox(lblDelay, sldrDelay);
    private SimpleDoubleProperty delayValue = new SimpleDoubleProperty();

    private HBox hbMainControls = new HBox(btnAction, new Separator(Orientation.VERTICAL), hbIterations,
            new Separator(Orientation.VERTICAL), hbDelay);

    private Label lblVertexRule = new Label("Vertex Selection Restriction:");
    private ComboBox<String> cmbVertexRule = new ComboBox<>();
    private ReadOnlyIntegerProperty selectedVertexRule = cmbVertexRule.getSelectionModel().selectedIndexProperty();

    private HBox hbVertexRule = new HBox(lblVertexRule, cmbVertexRule);

    private EventHandler<ActionEvent> handleStart = event -> {
        if(parseNumericFields()) {
            btnAction.setOnAction(null);
            setActionButtonDisabled(true);
            setIterationControlsDisabled(true);
            Fractals.getMainMenu().disableCanvasSizeSelection();
            Fractals.getFractalCanvas().beginStartPointSelection();
        }
    };

    private EventHandler<ActionEvent> handleStop = event -> Fractals.getFractalCanvas().cancelDrawPoints();

    private EventHandler<ActionEvent> handleReset = event -> {
        Fractals.getFractalCanvas().resetCanvas();
        btnAction.setOnAction(handleStart);
        Fractals.getMainMenu().enableCanvasSizeSelection();
    };

    private EventHandler<ActionEvent> handleSelectVertexRule = event -> {
        LOG.info(String.valueOf(cmbVertexRule.getSelectionModel().getSelectedIndex()));
    };

    ControlPanel() {
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

        cmbVertexRule.setOnAction(handleSelectVertexRule);
        cmbVertexRule.getItems().add("Any vertex can be chosen");
        cmbVertexRule.getItems().add("Previous vertex cannot be chosen");
        cmbVertexRule.getSelectionModel().select(0);

        hbVertexRule.setSpacing(3.0);
        hbVertexRule.setAlignment(Pos.CENTER);

        setPadding(new Insets(5.0, 10.0, 5.0, 10.0));
        setSpacing(8.0);
        setAlignment(Pos.CENTER);
        getChildren().addAll(hbMainControls, hbVertexRule);

        actionButtonStart();
    }

    int getIterations() {
        return iterations;
    }

    int getDelay() {
        return (int) delayValue.get();
    }

    VertexSelectionRule getVertexSelectionRule() {
        switch(selectedVertexRule.get()) {
            case 1:
                return VertexSelectionRule.DIFFERENT_THAN_PREVIOUS;
            default:
                return VertexSelectionRule.NO_RESTRICTION;
        }
    }

    void setIterationControlsDisabled(boolean val) {
        txtIterations.setDisable(val);
    }

    void setActionButtonDisabled(boolean val) {
        btnAction.setDisable(val);
    }

    void actionButtonStop() {
        setActionButtonDisabled(false);
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
        setActionButtonDisabled(true);
        setIterationControlsDisabled(false);
        btnAction.setText("Start");
        btnAction.setOnAction(handleStart);
        LOG.debug("Start button enabled.");
    }

    private boolean parseNumericFields() {
        try {
            iterations = Integer.parseInt(txtIterations.getText());
            txtIterations.setText(String.valueOf(iterations));
        } catch(NumberFormatException e) {
            showInvalidNumberMessage(txtIterations.getText());
            return false;
        }

        return true;
    }

    private void showInvalidNumberMessage(String value) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Number Error");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Error! Value \"%s\" is not a valid number!", value));
        alert.showAndWait();
    }

}
