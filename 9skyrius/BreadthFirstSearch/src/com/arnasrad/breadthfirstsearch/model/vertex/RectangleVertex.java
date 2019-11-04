package com.arnasrad.breadthfirstsearch.model.vertex;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class RectangleVertex extends Vertex {

    public RectangleVertex(String id, State state) {
        super(id, state);

        Rectangle view = new Rectangle( 50,50);

        view.setStroke(Color.BLACK);
//        view.setFill(Color.DODGERBLUE);

        setView( view);

    }

    public RectangleVertex(String id) {
        super(id, State.IDLE);

        Rectangle view = new Rectangle( 50,50);

        view.setStroke(Color.BLACK);
        view.setFill(Color.DODGERBLUE);

        setView( view);

    }

}