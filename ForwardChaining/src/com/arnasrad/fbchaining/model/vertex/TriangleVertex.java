package com.arnasrad.fbchaining.model.vertex;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class TriangleVertex extends Vertex {

    public TriangleVertex(String id, String label, State state) {
        super(id, label, state);

        double width = 50;
        double height = 50;

        Polygon view = new Polygon( width / 2, 0, width, height, 0, height);

        view.setStroke(Color.BLACK);
//        view.setFill(Color.RED);

        setView( view);

    }

    public TriangleVertex(String id, String label) {
        super(id, label, State.IDLE);

        double width = 50;
        double height = 50;

        Polygon view = new Polygon( width / 2, 0, width, height, 0, height);

        view.setStroke(Color.BLACK);
        view.setFill(Color.RED);

        setView( view);

    }

}