package com.arnasrad.fbchaining.model.vertex;

import com.arnasrad.fbchaining.utility.Utils;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class EllipseVertex extends Vertex {

    public static int DEFAULT_RADIUSX = 30;
    public static int DEFAULT_RADIUSY = 30;

    public EllipseVertex(String id, String label, State state) {
        super(id, label, state);

        double textWidth = this.getLabelTxt().getLayoutBounds().getWidth();
        double textHeight = this.getLabelTxt().getLayoutBounds().getHeight();

        if (textWidth < DEFAULT_RADIUSX) {
            textWidth = DEFAULT_RADIUSX;
        }

        if (textHeight < DEFAULT_RADIUSY) {
            textHeight = DEFAULT_RADIUSY;
        }

        Ellipse view = new Ellipse(textWidth, textHeight);

        view.setStroke(Color.BLACK);
//        view.setFill(Color.DODGERBLUE);

        setView( view);

    }

    public EllipseVertex(String id, String label) {
        this(id, label, State.IDLE);

    }

    public void appendLabelTxt(ArrayList<String> text) {

        if (text == null || text.size() == 0) {
            return;
        }

        Text labelTxt = getLabelTxt();
        labelTxt.setText(labelTxt.getText() + "\n"
                + Utils.getListString(text, "\n"));

        resizeView();
    }

    public void appendLabelTxt(String text) {

        if (text == null) {
            return;
        }

        Text labelTxt = getLabelTxt();
        labelTxt.setText(labelTxt.getText() + "\n" + text);

        resizeView();
    }

    private void resizeView() {

        Text idTxt = getLabelTxt();
        double textWidth = idTxt.getLayoutBounds().getWidth();
        double textHeight = idTxt.getLayoutBounds().getHeight();

        Ellipse view = (Ellipse) getView();
        view.setRadiusX(textWidth);
        view.setRadiusY(textHeight);
    }
}
