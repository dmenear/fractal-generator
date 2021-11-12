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

import java.util.Deque;
import java.util.Iterator;
import java.util.StringJoiner;

class FractalCanvas extends StackPane {

    private static final Logger LOG = LoggerFactory.getLogger(FractalCanvas.class);

    private static final int[] SMALL_CANVAS_DIMENSIONS = {600, 400};
    private static final int[] MEDIUM_CANVAS_DIMENSIONS = {800, 600};
    private static final int[] LARGE_CANVAS_DIMENSIONS = {1200, 800};

    private static final Color BACKGROUND_COLOR = Color.rgb(15, 15, 15);
    private static final Color SHAPE_COLOR = Color.BLUE;

    private static double SELECTED_POINT_SIZE = 8.0;
    private static double SELECTED_POINT_MIN_DISTANCE = 16.0;

    private Canvas selectionCanvas;
    private Canvas dotCanvas;
    private Thread drawPoints;

    private boolean drawShapeLines = true;

    private SimpleIntegerProperty counterVal = new SimpleIntegerProperty(0);

    private SelectedShape selectedShape;
    private Deque<double[]> coordinates;

    private EventHandler<MouseEvent> handleShapeSelection = event -> {
        if(event.getButton() == MouseButton.SECONDARY
                || (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
            if(!coordinates.isEmpty()) {
                coordinates.pop();
            } else {
                LOG.info("Unable to remove coordinate - no coordinate to remove!");
            }
        } else if(event.getButton() == MouseButton.PRIMARY) {
            double clickX = event.getX();
            double clickY = event.getY();
            if(isValidLocation(coordinates, clickX, clickY, SELECTED_POINT_MIN_DISTANCE)) {
                coordinates.push(new double[]{clickX, clickY});
            } else {
                LOG.info("Unable to select coordinate - too close to neighboring node!");
            }
        }

        redrawSelectedShapes();
        updateStartButtonDisabled();

        final StringJoiner sjOut = new StringJoiner(", ", "[", "]");
        coordinates.forEach(coord -> sjOut.add("(" + coord[0] + "," + coord[1] + ")"));
        LOG.debug("Current shape coordinates: " + sjOut.toString());
    };

    private EventHandler<MouseEvent> handleStartLocation = event -> {
        if(event.getButton() == MouseButton.PRIMARY) {
            drawPoints = new Thread(new StartButtonRunnable(dotCanvas.getGraphicsContext2D(), selectedShape,
                    new double[] { event.getX(), event.getY()}, counterVal), "draw-points");
            drawPoints.start();
            this.setOnMouseClicked(null);
            this.setCursor(Cursor.DEFAULT);
            Fractals.getControlPanel().actionButtonStop();
        }
    };

    FractalCanvas() {
        selectionCanvas = new Canvas();
        dotCanvas = new Canvas();

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

        getChildren().addAll(dotCanvas, selectionCanvas, grpCounter);
        setAlignment(grpCounter, Pos.TOP_CENTER);

        selectedShape = new SelectedShape(SHAPE_COLOR);
        coordinates = selectedShape.getCoordinates();
    }

    void init() {
        resizeCanvasMedium();
    }

    void beginStartPointSelection() {
        this.setOnMouseClicked(handleStartLocation);
        this.setCursor(Cursor.CROSSHAIR);
    }

    void cancelDrawPoints() {
        LOG.info("Cancelling draw points...");
        drawPoints.interrupt();
    }

    void resetCanvas() {
        this.setOnMouseClicked(handleShapeSelection);
        this.setCursor(Cursor.HAND);

        counterVal.set(0);

        clearDotCanvas();
        redrawSelectedShapes();

        Fractals.getControlPanel().actionButtonStart();
        updateStartButtonDisabled();

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
        dotCanvas.setWidth(dimensions[0]);
        dotCanvas.setHeight(dimensions[1]);
        selectionCanvas.setWidth(dimensions[0]);
        selectionCanvas.setHeight(dimensions[1]);
        resetCanvas();
    }

    private void redrawSelectedShapes() {
        clearSelectionCanvas();
        GraphicsContext context = selectionCanvas.getGraphicsContext2D();

        if(drawShapeLines) {
            drawSelectedShapeLines(selectedShape);
        }

        context.setFill(selectedShape.getColor());
        for(double[] coord : selectedShape.getCoordinates()) {
            context.fillOval(coord[0] - (SELECTED_POINT_SIZE / 2), coord[1] - (SELECTED_POINT_SIZE / 2),
                    SELECTED_POINT_SIZE, SELECTED_POINT_SIZE);
        }
    }

    private void drawSelectedShapeLines(SelectedShape selectedShape) {
        GraphicsContext context = selectionCanvas.getGraphicsContext2D();
        Iterator<double[]> iter = selectedShape.getCoordinates().iterator();

        context.setStroke(selectedShape.getColor());
        context.setLineWidth(1.0);

        if(iter.hasNext()) {
            double[] startingPoint = iter.next();
            context.beginPath();
            context.moveTo(startingPoint[0], startingPoint[1]);
            while (iter.hasNext()) {
                double[] nextPoint = iter.next();
                context.lineTo(nextPoint[0], nextPoint[1]);
            }
            context.lineTo(startingPoint[0], startingPoint[1]);
            context.stroke();
        }
    }

    void setDrawShapeLines(boolean val) {
        drawShapeLines = val;
        redrawSelectedShapes();
    }

    private void clearDotCanvas() {
        GraphicsContext context = dotCanvas.getGraphicsContext2D();
        Paint prevColor = context.getFill();
        context.setFill(BACKGROUND_COLOR);
        dotCanvas.getGraphicsContext2D().fillRect(0.0, 0.0, dotCanvas.getWidth(), dotCanvas.getHeight());
        context.setFill(prevColor);
    }

    private void clearSelectionCanvas() {
        GraphicsContext context = selectionCanvas.getGraphicsContext2D();
        context.clearRect(0.0, 0.0, selectionCanvas.getWidth(), selectionCanvas.getHeight());
    }

    private void updateStartButtonDisabled() {
        if(coordinates.size() < 2) {
            Fractals.getControlPanel().setActionButtonDisabled(true);
        } else {
            Fractals.getControlPanel().setActionButtonDisabled(false);
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
