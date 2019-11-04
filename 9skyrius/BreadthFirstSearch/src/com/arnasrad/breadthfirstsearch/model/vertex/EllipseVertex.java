package com.arnasrad.breadthfirstsearch.model.vertex;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

public class EllipseVertex extends Vertex {

    public EllipseVertex(String id, State state) {
        super(id, state);

        double textWidth = this.getIdTxt().getLayoutBounds().getWidth();
        if (textWidth < 30) {
            textWidth = 30;
        }

        Ellipse view = new Ellipse(textWidth, 30);

        view.setStroke(Color.BLACK);
//        view.setFill(Color.DODGERBLUE);

        setView( view);

    }

    public EllipseVertex(String id) {
        super(id, State.IDLE);

        double textWidth = this.getIdTxt().getLayoutBounds().getWidth();
        if (textWidth < 30) {
            textWidth = 30;
        }

        Ellipse view = new Ellipse(30, textWidth);

        view.setStroke(Color.BLACK);
        view.setFill(Color.DODGERBLUE);

        setView( view);

    }
}
