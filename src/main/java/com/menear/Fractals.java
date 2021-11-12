package com.menear;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Fractals extends Application {

    private static ControlPanel controlPanel;
    private static FractalCanvas fractalCanvas;
    private static MainMenu mainMenu;
    private static Stage mainStage;

    @Override
    public void start(Stage stage) {
        mainStage = stage;

        mainMenu = new MainMenu();
        fractalCanvas = new FractalCanvas();
        controlPanel = new ControlPanel();

        fractalCanvas.init();

        VBox vbMain = new VBox(mainMenu, fractalCanvas, controlPanel);
        Scene scene = new Scene(vbMain);

        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.jpg")));
        stage.setTitle("Chaos Game Fractal Generator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main( String[] args ) {
        launch(args);
    }

    public static ControlPanel getControlPanel() {
        return controlPanel;
    }

    public static FractalCanvas getFractalCanvas() {
        return fractalCanvas;
    }

    public static MainMenu getMainMenu() {
        return mainMenu;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

}
