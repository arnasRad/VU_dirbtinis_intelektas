package com.arnasrad.fbchaining.model.vertex;

import com.arnasrad.fbchaining.utility.Utils;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Vertex extends StackPane {

    public enum State {

        IDLE,
        CURRENT,
        OPENED,
        CLOSED,
        TARGET,
        TARGET_REACHED,
        PATH,
        UNSPECIFIED
    }

    private String vertexId;
    private Text idTxt;
    private State state;

    private List<Vertex> children = new ArrayList<>();
    private List<Vertex> parents = new ArrayList<>();
    private Vertex sourceParent = null; // used in BFS graph searches
    private double pathCost = 0.0; // used in Dijkstra graph searches

    private Node view;

    public Vertex(String vertexId, State state) {

        this.vertexId = vertexId;
        this.idTxt = new Text(vertexId);
        this.state = state;
    }

    public Vertex(String vertexId) {

        this(vertexId, State.UNSPECIFIED);
    }

    public Vertex(Vertex vertex, State state) {

        this(vertex.getVertexId(), state);
    }

    public Vertex(Vertex vertex) {

        this(vertex.getVertexId(), State.IDLE);
    }

    public void addVertexChild(Vertex vertex) {
        children.add(vertex);
    }

    public List<Vertex> getVertexChildren() {
        return children;
    }

    public void addVertexParent(Vertex vertex) {
        parents.add(vertex);
    }

    public List<Vertex> getVertexParents() {
        return parents;
    }

    public void removeVertexChild(Vertex vertex) {
        children.remove(vertex);
    }

    public List<Vertex> getAdjacentVertices() {

        List<Vertex> adjacentVertices = new ArrayList<>(
                getVertexChildren()
        );
        adjacentVertices.addAll(getVertexParents());
        return adjacentVertices;
    }

    public Text getIdTxt() {
        return this.idTxt;
    }

    public void setView(Node view) {

        this.view = view;
        setViewStyle();

        getChildren().addAll(view, idTxt);

    }

    public void setState(State state) {

        this.state = state;
        setViewStyle();
    }

    public Vertex getSourceParent() {
        return sourceParent;
    }

    public void setSourceParent(Vertex sourceParent) {
        this.sourceParent = sourceParent;
    }

    private void setViewStyle() {

        Shape thisShape = (Shape) this.view;
        switch (state) {
            case IDLE:
                thisShape.setFill(Color.web("#c7ffc4"));
                thisShape.setStrokeWidth(1);
                break;
            case CURRENT:
                thisShape.setFill(Color.web("#0dff00"));
                thisShape.setStrokeWidth(2);
                break;
            case OPENED:
//                thisShape.setFill(Color.web("#0ab800"));
                thisShape.setFill(Color.GRAY);
                thisShape.setStrokeWidth(1);
                break;
            case CLOSED:
//                thisShape.setFill(Color.web("#067300"));
                thisShape.setFill(Color.DARKGRAY);
                thisShape.setStrokeWidth(1);
                break;
            case TARGET:
                thisShape.setFill(Color.web("#cf0000"));
                thisShape.setStrokeWidth(2);
                break;
            case TARGET_REACHED:
                thisShape.setFill(Color.web("#ff0000"));
                thisShape.setStrokeWidth(2);
                break;
            case PATH:
                thisShape.setFill(Color.CRIMSON);
                thisShape.setStrokeWidth(2);
                break;
        }
    }

    public Node getView() {
        return this.view;
    }

    public String getVertexId() {
        return vertexId;
    }

    public double getPathCost() {
        return pathCost;
    }

    public void setPathCost(double pathCost) {
        this.pathCost = pathCost;
    }

    public void appendPathCost(double pathCost) {
        this.pathCost += pathCost;
    }

    @Override
    public String toString() {
        if (pathCost != 0.0) {
            return vertexId + ", " + pathCost;
        } else {
            return vertexId;
        }
    }
}