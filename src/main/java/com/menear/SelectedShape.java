package com.menear;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayDeque;
import java.util.Deque;

class SelectedShape {
    private Deque<double[]> coordinates = new ArrayDeque<>();
    private Label lblWeight;
    private TextField txtWeight = new TextField();
    private double weight = 1.0;

    SelectedShape(int shapeNumber) {
        lblWeight = new Label(String.format("Shape %d Weight: ", shapeNumber));
        txtWeight.setPrefColumnCount(4);
    }

    Deque<double[]> getCoordinates() {
        return coordinates;
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
