package com.menear;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;

class SelectedShape {
    private Deque<double[]> coordinates = new ArrayDeque<>();
    private Label lblWeight;
    private TextField txtWeight = new TextField();
    private double weight = 1.0;
    private Color color;

    SelectedShape(int shapeNumber, Color color) {
        lblWeight = new Label(String.format("Shape %d Weight: ", shapeNumber));
        txtWeight.setPrefColumnCount(4);
        this.color = color;
    }

    Deque<double[]> getCoordinates() {
        return coordinates;
    }

    Color getColor() {
        return color;
    }

    double getWeight() {
        return weight;
    }

    void setWeight(double weight) {
        this.weight = weight;
    }

    Label getLblWeight() {
        return lblWeight;
    }

    TextField getTxtWeight() {
        return txtWeight;
    }
}
