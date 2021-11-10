package com.menear;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringJoiner;

class FractalCanvas extends StackPane {

    private static final Logger LOG = LoggerFactory.getLogger(FractalCanvas.class);

    private static final int[] SMALL_CANVAS_DIMENSIONS = {600, 400};
    private static final int[] MEDIUM_CANVAS_DIMENSIONS = {800, 600};
    private static final int[] LARGE_CANVAS_DIMENSIONS = {1200, 800};

    private static final Color BACKGROUND_COLOR = Color.rgb(15, 15, 15);
    private static final Color[] SHAPE_COLORS = {
            Color.BLUE,
            Color.RED,
            Color.PURPLE,
            Color.HOTPINK,
            Color.AQUAMARINE,
            Color.WHITE,
            Color.YELLOW
    };

    private Canvas canvas;
    private Thread drawPoints;

    private int shapeCount = 1;

    private SimpleIntegerProperty counterVal = new SimpleIntegerProperty(0);

    private Deque<SelectedShape> selectedShapes = new ArrayDeque<>();
    private SelectedShape currentShape;
    private Deque<double[]> coordinates;

    private EventHandler<MouseEvent> handleShapeSelection = event -> {
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(SHAPE_COLORS[(shapeCount - 1) % SHAPE_COLORS.length]);
        double minDistance = 16.0;
        double size = 8.0;

        if(event.getButton() == MouseButton.SECONDARY
                || (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
            if(!coordinates.isEmpty()) {
                double[] removedCoords = coordinates.pop();
                double removedX = removedCoords[0];
                double removedY = removedCoords[1];

                clearPoint(removedX, removedY, size);
            } else {
                if(selectedShapes.size() > 1) {
                    removeShape();
                } else {
                    LOG.info("Unable to remove coordinate - no coordinate to remove!");
                }
            }
        } else if(event.getButton() == MouseButton.PRIMARY) {
            double clickX = event.getX();
            double clickY = event.getY();
            if(isValidLocation(coordinates, clickX, clickY, minDistance)) {
                context.fillOval(clickX - (size / 2), clickY - (size / 2), size, size);
                coordinates.push(new double[]{clickX, clickY});
            } else {
                LOG.info("Unable to select coordinate - too close to neighboring node!");
            }
        }

        updateShapeSelectionControls();

        final StringJoiner sjOut = new StringJoiner(", ", "[", "]");
        coordinates.forEach(coord -> sjOut.add("(" + coord[0] + "," + coord[1] + ")"));
        LOG.debug("Current shape coordinates: " + sjOut.toString());
    };

    private EventHandler<MouseEvent> handleStartLocation = event -> {
        if(event.getButton() == MouseButton.PRIMARY) {
            drawPoints = new Thread(new StartButtonRunnable(canvas.getGraphicsContext2D(), selectedShapes,
                    new double[] { event.getX(), event.getY()}, counterVal), "draw-points");
            drawPoints.start();
            canvas.setOnMouseClicked(null);
            canvas.setCursor(Cursor.DEFAULT);
            Fractals.getControlPanel().actionButtonStop();
        }
    };

    FractalCanvas() {
        canvas = new Canvas();

        Font counterFont = Font.font(20.0);

        Label lblCounterTitle = new Label("Iterations: ");
        Label lblCounter = new Label();
        lblCounterTitle.setTextFill(Color.WHITE);
        lblCounterTitle.setFont(counterFont);
        lblCounter.setTextFill(Color.WHITE);
        lblCounter.setFont(counterFont);
        lblCounter.textProperty().bind(counterVal.asString());

        HBox hbCounter = new HBox(lblCounterTitle, lblCounter);
        Group grpCounter = new Group(hbCounter);

        getChildren().addAll(canvas, grpCounter);
        setAlignment(grpCounter, Pos.TOP_CENTER);
    }

    void init() {
        resizeCanvasMedium();
    }

    Deque<SelectedShape> getSelectedShapes() {
        return selectedShapes;
    }

    Canvas getCanvas() {
        return canvas;
    }

    void addShape() {
        currentShape = new SelectedShape(++shapeCount);
        selectedShapes.push(currentShape);
        coordinates = currentShape.getCoordinates();
        updateShapeSelectionControls();
        Fractals.getControlPanel().renderShapeWeightFields();
        LOG.info("Selecting points for new shape...");
    }

    void removeShape() {
        selectedShapes.pop();
        currentShape = selectedShapes.peek();
        coordinates = currentShape.getCoordinates();
        shapeCount--;
        Fractals.getControlPanel().enableAddShapeButton();
        Fractals.getControlPanel().enableActionButton();
        Fractals.getControlPanel().renderShapeWeightFields();
        LOG.info("Shape removed");
    }

    void beginStartPointSelection() {
        LOG.info(selectedShapes.size() + " shape(s) selected.");
        canvas.setOnMouseClicked(handleStartLocation);
        canvas.setCursor(Cursor.HAND);
    }

    void cancelDrawPoints() {
        LOG.info("Cancelling draw points...");
        drawPoints.interrupt();
    }

    void resetCanvas() {
        canvas.setOnMouseClicked(handleShapeSelection);
        canvas.setCursor(Cursor.HAND);

        counterVal.set(0);

        selectedShapes.clear();
        currentShape = new SelectedShape(shapeCount = 1);
        selectedShapes.push(currentShape);
        coordinates = currentShape.getCoordinates();

        clearCanvas();

        Fractals.getControlPanel().actionButtonStart();
        Fractals.getControlPanel().renderShapeWeightFields();

        LOG.debug("Canvas reset");
    }

    void resizeCanvasSmall() {
        resizeCanvas(SMALL_CANVAS_DIMENSIONS);
    }

    void resizeCanvasMedium() {
        resizeCanvas(MEDIUM_CANVAS_DIMENSIONS);
    }

    void resizeCanvasLarge() {
        resizeCanvas(LARGE_CANVAS_DIMENSIONS);
    }

    private void resizeCanvas(int[] dimensions) {
        canvas.setWidth(dimensions[0]);
        canvas.setHeight(dimensions[1]);
        resetCanvas();
    }

    private void clearPoint(double coordX, double coordY, double size) {
        clearArea(coordX - (size / 2), coordY - (size / 2), size, size);
    }

    private void clearCanvas() {
        clearArea(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
    }

    private void clearArea(double startX, double startY, double areaWidth, double areaHeight) {
        GraphicsContext context = canvas.getGraphicsContext2D();
        Paint prevColor = context.getFill();
        context.setFill(BACKGROUND_COLOR);
        canvas.getGraphicsContext2D().fillRect(startX, startY, areaWidth, areaHeight);
        context.setFill(prevColor);
    }

    private void updateShapeSelectionControls() {
        if(coordinates.size() < 2) {
            LOG.info("Disabling buttons since not enough coordinates are selected.");
            Fractals.getControlPanel().disableAddShapeButton();
            Fractals.getControlPanel().disableActionButton();
        } else {
            Fractals.getControlPanel().enableActionButton();
            Fractals.getControlPanel().enableAddShapeButton();
        }
    }

    private boolean isValidLocation(Deque<double[]> coordinates, double x, double y, double minDistance) {
        for(double[] coord : coordinates) {
            if(Math.abs(x - coord[0]) < minDistance && Math.abs(y - coord[1]) < minDistance) {
                return false;
            }
        }
        return true;
    }
}
