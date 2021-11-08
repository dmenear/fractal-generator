package com.menear;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Fractals extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Fractal Generator");

        FractalCanvas fractalCanvas = new FractalCanvas(1200.0, 800.0);
        ControlPanel controlPanel = new ControlPanel(stage);

        fractalCanvas.init(controlPanel);
        controlPanel.init(fractalCanvas);

        VBox vbMain = new VBox(fractalCanvas, new Separator(Orientation.HORIZONTAL), controlPanel);
        Scene scene = new Scene(vbMain);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main( String[] args ) {
        launch(args);
    }

}
