package com.arnasrad.fbchaining.model;

import com.arnasrad.fbchaining.model.vertex.Vertex;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class Edge extends Group {

    public static final int NULL_COST = 1;

    private Vertex source;
    private Vertex target;
    private String label;

    private Line line;
    private Text labelTxt;

    private Line arrow1;
    private Line arrow2;

    private final int arrowLength = 20;
    private final int arrowWidth = 7;

    private boolean oriented;

    private ChangeListener<Number> connectionCoordinatesListener = (observable, oldValue, newValue) -> {

        Ellipse sourceNode = (Ellipse) source.getView();
        Ellipse targetNode = (Ellipse) target.getView();

        Point2D endLineOffset = getOffset(sourceNode, targetNode);
        Point2D startLineOffset = getOffset(targetNode, sourceNode);

        setStartX((source.getBoundsInParent().getCenterX() + startLineOffset.getX()));
        setStartY((source.getBoundsInParent().getCenterY() + startLineOffset.getY()));
        setEndX((target.getBoundsInParent().getCenterX() + endLineOffset.getX()));
        setEndY((target.getBoundsInParent().getCenterY() + endLineOffset.getY()));
    };

    private InvalidationListener updater = o -> {

        drawArrow();
    };

    public Edge(Vertex source, Vertex target, boolean isOriented, String label) {

        labelTxt = new Text();
        line = new Line();
        arrow1 = new Line();
        arrow2 = new Line();

        defineOrientation(source, target);
        getChildren().add(line);

        bindVerticesXYProperty();
        addArrowListener();

        setOriented(isOriented);
//        setLabelTxt(label);
//        getChildren().add(labelTxt);
//        bindCostTxtToLine();
    }

    public Edge(Vertex source, Vertex target) {

        this(source, target, false, "");
    }

    public Edge(Vertex source, Vertex target, String label) {

        this(source, target, false, label);
    }

    private void addArrowListener() {

//        startYProperty().removeListener(updater);
//        startYProperty().removeListener(updater);
//        endXProperty().removeListener(updater);
//        endYProperty().removeListener(updater);

        startXProperty().addListener(updater);
        startYProperty().addListener(updater);
        endXProperty().addListener(updater);
        endYProperty().addListener(updater);
        updater.invalidated(null);
    }

    private void drawArrow() {

        double ex = getEndX();
        double ey = getEndY();
        double sx = getStartX();
        double sy = getStartY();

        arrow1.setEndX(ex);
        arrow1.setEndY(ey);
        arrow2.setEndX(ex);
        arrow2.setEndY(ey);

        if (ex == sx && ey == sy) {
            // arrow parts of length 0
            arrow1.setStartX(ex);
            arrow1.setStartY(ey);
            arrow2.setStartX(ex);
            arrow2.setStartY(ey);
        } else {
            final double hypot = Math.hypot(sx - ex, sy - ey);
            double factor = arrowLength / hypot;
            double factorO = arrowWidth / hypot;

            // part in direction of main line
            double dx = (sx - ex) * factor;
            double dy = (sy - ey) * factor;

            // part ortogonal to main line
            double ox = (sx - ex) * factorO;
            double oy = (sy - ey) * factorO;

            arrow1.setStartX(ex + dx - oy);
            arrow1.setStartY(ey + dy + ox);
            arrow2.setStartX(ex + dx + oy);
            arrow2.setStartY(ey + dy - ox);
        }
    }

    private void bindVerticesXYProperty() {

//        source.layoutXProperty().removeListener(targetListener);
//        source.layoutYProperty().removeListener(targetListener);
//        target.layoutXProperty().removeListener(targetListener);
//        target.layoutYProperty().removeListener(targetListener);

        source.layoutXProperty().addListener(connectionCoordinatesListener);
        source.layoutYProperty().addListener(connectionCoordinatesListener);
        target.layoutXProperty().addListener(connectionCoordinatesListener);
        target.layoutYProperty().addListener(connectionCoordinatesListener);
    }

    private Point2D getOffset(Ellipse source, Ellipse target) {

        double angle = getAngle(source, target);

        return new Point2D(calculateOffsetX(target.getRadiusX(), target.getRadiusY(), angle),
                calculateOffsetY(target.getRadiusX(), target.getRadiusY(), angle));
    }

    private double getAngle(Ellipse source, Ellipse target) {

        Bounds boundsSource = source.localToScene(source.getBoundsInLocal());
        Bounds boundsTarget = target.localToScene(target.getBoundsInLocal());

        Point2D pointSource = new Point2D(boundsSource.getCenterX(), boundsSource.getCenterY());
        Point2D pointTarget = new Point2D(boundsTarget.getCenterX(), boundsTarget.getCenterY());

        double dx = pointTarget.getX() - pointSource.getX();
        double dy = pointTarget.getY() - pointSource.getY();

        return -Math.atan2(dy,dx);

//          // used for vector angles
//        Point2D p1 = new Point2D(boundsE1.getCenterX(), boundsE1.getCenterY());
//        Point2D p2 = new Point2D(boundsE2.getCenterX(), boundsE2.getCenterY());
//        Point2D p3 = p2.subtract(p1);
//        Point2D p4 = new Point2D(1, 0);
//
//        return toRadians(p4.angle(p3));
    }

    private double calculateOffsetX(double a, double b, double angle) {

        double dividend = a * b;
        double divisor = (Math.sqrt(Math.pow(b, 2) + Math.pow(a, 2) * Math.pow(Math.tan(angle), 2)));
        double result = dividend / divisor;

        // return with + sign if -90 < angle < 90
        double angleDegrees = Math.toDegrees(angle);

        if (angleDegrees > -90 && angleDegrees < 90) {
            return -result;
        } else {
            return result;
        }
    }

    private double calculateOffsetY(double a, double b, double angle) {

        double dividend = a * b;
        double divisor = Math.sqrt(Math.pow(a, 2) + (Math.pow(b, 2)/Math.pow(Math.tan(angle), 2)));
        double result = dividend / divisor;

        // return with + sign if -90 < angle < 90
        double angleDegrees = Math.toDegrees(angle);
        if (angleDegrees > 0 && angleDegrees < 180) {
            return result;
        } else {
            return -result;
        }
    }

    private void setSourceAndTarget(Vertex newSource, Vertex newTarget) {

        // new source and target are equal to current source and target - no need to reset them
        if (newSource == this.source && newTarget == this.target) {
            return;
        }

        if (this.source != null) {
            this.source.getVertexChildren().remove(target);
        }
        if (this.target != null) {
            this.target.getVertexParents().remove(source);
        }

        this.source = newSource;
        this.target = newTarget;

        this.source.addVertexChild(this.target);
        this.target.addVertexParent(this.source);
    }

    public void defineOrientation(Vertex source, Vertex target) {

        // orientation must be defined between current source/target vertices
        if (this.source != null && this.target != null) {
            if (source != this.source && source != this.target) {
                return;
            }

            if (target != this.target && target != this.source) {
                return;
            }
        }

        if (this.source == target && this.target == source) {
            swapLineStartEndPositions();
        }

        setSourceAndTarget(source, target);
    }

    public void swapLineStartEndPositions() {

        double startX = getStartX();
        double startY = getStartY();
        setStartX(getEndX());
        setStartY(getEndY());
        setEndX(startX);
        setEndY(startY);
    }

    public boolean isOriented() {
        return this.oriented;
    }

    public void setOriented(boolean oriented) {

        this.oriented = oriented;
        showOrientation();
    }

    private void showOrientation() {

        if (oriented) {

            getChildren().addAll(arrow1, arrow2);
        } else {

            getChildren().removeAll(arrow1, arrow2);
        }
    }

    private void setLabelTxt(String label) {

        this.label = label;
        if (!label.equals(String.valueOf(NULL_COST))) {
            this.labelTxt.setText(this.label);
        }
    }

    private void bindCostTxtToLine() {

//        costTxt.rotateProperty().bind(line.rotateProperty());
        labelTxt.xProperty().bind(startXProperty().add((endXProperty().subtract(startXProperty())).divide(2)).add(5));
        labelTxt.yProperty().bind(startYProperty().add((endYProperty().subtract(startYProperty())).divide(2)).subtract(5));
    }

    public String getLabel() {
        return label;
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

    private void setStartX(double value) {
        line.setStartX(value);
    }

    private double getStartX() {
        return line.getStartX();
    }

    private DoubleProperty startXProperty() {
        return line.startXProperty();
    }

    private void setStartY(double value) {
        line.setStartY(value);
    }

    private double getStartY() {
        return line.getStartY();
    }

    private DoubleProperty startYProperty() {
        return line.startYProperty();
    }

    private void setEndX(double value) {
        line.setEndX(value);
    }

    private double getEndX() {
        return line.getEndX();
    }

    private DoubleProperty endXProperty() {
        return line.endXProperty();
    }

    private void setEndY(double value) {
        line.setEndY(value);
    }

    private double getEndY() {
        return line.getEndY();
    }

    private DoubleProperty endYProperty() {
        return line.endYProperty();
    }

}