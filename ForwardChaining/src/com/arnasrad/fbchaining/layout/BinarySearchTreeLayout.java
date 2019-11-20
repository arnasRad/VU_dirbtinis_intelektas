package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.Graph;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.List;
import java.util.Random;

public class BinarySearchTreeLayout {

    private Graph graph;

    private Random rnd = new Random();

    public BinarySearchTreeLayout(Graph graph) {

        this.graph = graph;

    }

    public void execute() {

        int graphDepth = getGraphDepth();
        double totalWidth = Math.pow(2, graphDepth) * 65;
        double currentLayoutX = totalWidth / 2;
        Vertex rootVertex = graph.getModel().getRoot();

        rootVertex.relocate(currentLayoutX, 50);
        List<Vertex> children = rootVertex.getVertexChildren();
        if (children.size() == 0) {
            // root has no children
            return;
        } else if (children.size() > 2) {
            // root has more than two children -> unexpected error
            return;
        }


        Vertex child = children.get(0); // left child
        positionVertex(child, -currentLayoutX/2);

        if (children.size() == 1) {
            // root only has one child
            rootVertex.relocate(currentLayoutX - currentLayoutX/2, 50);
            return;
        }

        child = children.get(1); // right child
        positionVertex(child, currentLayoutX/2);

    }



    public int getGraphDepth() {

        return getGraphDepth(graph.getModel().getRoot(), 0);
    }

    private int getGraphDepth(Vertex vertex, int currentDepth) {

        int leftDepth = currentDepth;
        int rightDepth = currentDepth;
        List<Vertex> children = vertex.getVertexChildren();
        if (children == null || children.size() == 0) {
            // edge condition
            return currentDepth;
        }

        Vertex child = children.get(0);
        leftDepth = getGraphDepth(child, currentDepth + 1);

        if (children.size() == 1) {
            return Math.max(leftDepth, rightDepth);
        }

        child = children.get(1);
        rightDepth = getGraphDepth(child, currentDepth + 1);

        return Math.max(leftDepth, rightDepth);
    }

    /**
     * Recursive function
     * Sets layout coordinates for vertex in binary tree graph
     * @param vertex to set coordinates to
     * @param xOffset relative to parent
     */
    private void positionVertex(Vertex vertex, double xOffset) {

        Vertex parentVertex = vertex.getVertexParents().get(0);
        vertex.setLayoutX(parentVertex.getLayoutX() + xOffset);
        vertex.setLayoutY(parentVertex.getLayoutY() + 70);

        List<Vertex> children = vertex.getVertexChildren();

        if (children.size() == 0) {
            return;
        }

        Vertex child = vertex.getVertexChildren().get(0);
        positionVertex(child, -(xOffset/2)); // left child

        if (children.size() == 1) {
            return;
        }

        child = vertex.getVertexChildren().get(1);
        positionVertex(child, xOffset/2); // right child
    }
}
