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

    private static final double SIZE = 2.0;
    private static final Color COLOR = Color.LIMEGREEN;

    private SelectedShape selectedShape;
    private GraphicsContext context;
    private double[] currentLocation;
    private SimpleIntegerProperty counterVal;

    public StartButtonRunnable(GraphicsContext context, SelectedShape selectedShape, double[] startingLocation,
                               SimpleIntegerProperty counterVal) {
        this.context = context;
        this.selectedShape = selectedShape;
        currentLocation = new double[] { startingLocation[0], startingLocation[1] };
        this.counterVal = counterVal;
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

        try {
            List<double[]> coordinatesList = new ArrayList<>(selectedShape.getCoordinates());
            double[] previousCoord = null;

            int i = 2;
            while(i <= Fractals.getControlPanel().getIterations() && !Thread.interrupted()) {
                Thread.sleep(Fractals.getControlPanel().getDelay());
                double[] coord = coordinatesList.get(secRandom.nextInt(coordinatesList.size()));

                switch(Fractals.getControlPanel().getVertexSelectionRule()) {
                    case NO_RESTRICTION:
                        break;
                    case DIFFERENT_THAN_PREVIOUS:
                        while(coord == previousCoord) {
                            coord = coordinatesList.get(secRandom.nextInt(coordinatesList.size()));
                        }
                }

                currentLocation = new double[]{
                        (currentLocation[0] + coord[0]) * 0.5, (currentLocation[1] + coord[1]) * 0.5
                };
                drawPoint(currentLocation, i++);
                previousCoord = coord;
            }
        } catch(InterruptedException e) {
            LOG.warn("Interrupted!");
            return;
        } finally {
            LOG.info("Finished drawing points.");
            Platform.runLater(() -> Fractals.getControlPanel().actionButtonReset());
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
