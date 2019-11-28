package com.arnasrad.fbchaining.model.vertex;

import com.arnasrad.fbchaining.utility.Utils;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class RectangleVertex extends Vertex {

    public static int DEFAULT_WIDTH = 60;
    public static int DEFAULT_HEIGHT = 60;

    public RectangleVertex(String id, State state) {
        super(id, state);

        double textWidth = this.getIdTxt().getLayoutBounds().getWidth();
        double textHeight = this.getIdTxt().getLayoutBounds().getHeight();

        if (textWidth < DEFAULT_WIDTH) {
            textWidth = DEFAULT_WIDTH;
        }

        if (textHeight < DEFAULT_HEIGHT) {
            textHeight = DEFAULT_HEIGHT;
        }

        Rectangle view = new Rectangle(textWidth*2, textHeight*2);

        view.setStroke(Color.BLACK);
//        view.setFill(Color.DODGERBLUE);

        setView( view);

    }

    public RectangleVertex(String id) {
        this(id, State.IDLE);

    }

    public void appendIdTxt(ArrayList<String> text) {

        if (text == null || text.size() == 0) {
            return;
        }

        Text idTxt = getIdTxt();
        idTxt.setText(idTxt.getText() + "\n"
                + Utils.getListString(text, "\n"));

        resizeView();
    }

    public void appendIdTxt(String text) {

        if (text == null) {
            return;
        }

        Text idTxt = getIdTxt();
        idTxt.setText(idTxt.getText() + "\n" + text);

        resizeView();
    }

    private void resizeView() {

        Text idTxt = getIdTxt();
        double textWidth = idTxt.getLayoutBounds().getWidth();
        double textHeight = idTxt.getLayoutBounds().getHeight();

        Ellipse view = (Ellipse) getView();
        view.setRadiusX(textWidth);
        view.setRadiusY(textHeight);
    }

}