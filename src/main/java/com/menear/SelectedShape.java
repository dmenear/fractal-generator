package com.menear;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;

class SelectedShape {
    private Deque<double[]> coordinates = new ArrayDeque<>();
    private Color color;

    SelectedShape(Color color) {
        this.color = color;
    }

    Deque<double[]> getCoordinates() {
        return coordinates;
    }

    Color getColor() {
        return color;
    }
}
