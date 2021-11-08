package com.menear;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

class StartButtonRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StartButtonRunnable.class);

    private static final double SIZE = 3.0;
    private static final Color COLOR = Color.LIMEGREEN;

    private Deque<SelectedShape> selectedShapes;
    private GraphicsContext context;
    private double[] currentLocation;
    private SimpleIntegerProperty counterVal;
    private ControlPanel controlPanel;

    public StartButtonRunnable(GraphicsContext context, Deque<SelectedShape> selectedShapes, double[] startingLocation,
                               SimpleIntegerProperty counterVal, ControlPanel controlPanel) {
        this.context = context;
        this.selectedShapes = selectedShapes;
        currentLocation = new double[] { startingLocation[0], startingLocation[1] };
        this.counterVal = counterVal;
        this.controlPanel = controlPanel;
    }

    @Override
    public void run() {
        SecureRandom secRandom;
        try {
            secRandom = SecureRandom.getInstanceStrong();
        } catch(NoSuchAlgorithmException e) {
            LOG.warn("Unable to get strong random number generator algorithm!");
            secRandom = new SecureRandom();
        }

        LOG.info("Drawing points...");
        drawPoint(currentLocation, 1);

        List<SelectedShape> selectedShapeList = new ArrayList<>(selectedShapes);
        selectedShapeList.sort(Comparator.comparing(SelectedShape::getWeight));
        TreeMap<Double, List<double[]>> weightedShapes = new TreeMap<>();
        double weightSum = 0.0;
        for(SelectedShape selectedShape : selectedShapeList) {
            weightSum += selectedShape.getWeight();
            weightedShapes.put(weightSum, new ArrayList<>(selectedShape.getCoordinates()));
        }

        try {
            int i = 2;
            while(i <= controlPanel.getIterations() && !Thread.interrupted()) {
                List<double[]> coordinatesList = null;

                double roll = secRandom.nextDouble() * weightSum;
                for(Double key : weightedShapes.keySet()) {
                    if(roll < key) {
                        coordinatesList = weightedShapes.get(key);
                        break;
                    }
                }

                Thread.sleep(controlPanel.getDelay());
                double[] coord = coordinatesList.get(secRandom.nextInt(coordinatesList.size()));
                currentLocation = new double[]{
                        (currentLocation[0] + coord[0]) / 2.0, (currentLocation[1] + coord[1]) / 2.0
                };
                drawPoint(currentLocation, i++);
            }
        } catch(InterruptedException e) {
            LOG.warn("Interrupted!");
            return;
        } finally {
            LOG.info("Finished drawing points.");
            Platform.runLater(() -> controlPanel.actionButtonReset());
        }
    }

    private void drawPoint(double[] point, int iteration) {
        Platform.runLater(() -> {
            context.setFill(COLOR);
            context.fillOval(point[0] - (SIZE / 2.0), point[1] - (SIZE / 2.0), SIZE, SIZE);
            counterVal.set(iteration);
        });
    }
}
