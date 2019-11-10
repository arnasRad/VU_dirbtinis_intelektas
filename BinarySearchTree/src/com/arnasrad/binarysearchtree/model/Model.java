package com.arnasrad.binarysearchtree.model;

import com.arnasrad.binarysearchtree.model.vertex.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {

    private Vertex graphParent;
    private Vertex root;

    private List<Vertex> allVertices;
    private List<Vertex> addedVertices;
    private List<Vertex> removedVertices;

    private List<Edge> allEdges;
    private List<Edge> addedEdges;
    private List<Edge> removedEdges;

    private Map<String, Vertex> vertexMap; // <id,vertex>

    public Model() {

        graphParent = new Vertex( "_ROOT_");

        // clear model, create lists
        clear();
    }

    public void clear() {

        allVertices = new ArrayList<>();
        addedVertices = new ArrayList<>();
        removedVertices = new ArrayList<>();

        allEdges = new ArrayList<>();
        addedEdges = new ArrayList<>();
        removedEdges = new ArrayList<>();

        vertexMap = new HashMap<>(); // <id,vertex>
        root = null;
    }

    public void clearAddedLists() {
        addedVertices.clear();
        addedEdges.clear();
    }

    private Vertex getRoot() {

        return this.root;
    }

    public Vertex getVertex(String id) {
        return vertexMap.get(id);
    }

    public List<Vertex> getAddedVertices() {
        return addedVertices;
    }

    public List<Vertex> getRemovedVertices() {
        return removedVertices;
    }

    public List<Vertex> getAllVertices() {
        return allVertices;
    }

    public List<Edge> getAddedEdges() {
        return addedEdges;
    }

    public List<Edge> getRemovedEdges() {
        return removedEdges;
    }

    public List<Edge> getAllEdges() {
        return allEdges;
    }

    public Edge getEdge(Vertex source, Vertex target) {

        for (Edge edge : allEdges) {
            if ((edge.getSource() == source && edge.getTarget() == target) ||
            edge.getSource() == target && edge.getTarget() == source) {

                return edge;
            }
        }

        return null;
    }

    public void addVertex(String id) {

        addVertex(id, VertexType.ELLIPSE, Vertex.State.IDLE, false);
    }

    public void addVertex(String id, boolean isRoot) {

        addVertex(id, VertexType.ELLIPSE, Vertex.State.IDLE, isRoot);
    }

    public void addVertex(String id, VertexType type, Vertex.State state, boolean isRoot) {

        switch (type) {

            case RECTANGLE:
                RectangleVertex rectangleVertex = new RectangleVertex(id, state);
                addVertexModel(rectangleVertex, isRoot);
                break;

            case TRIANGLE:
                TriangleVertex triangleVertex = new TriangleVertex(id, state);
                addVertexModel(triangleVertex, isRoot);
                break;

            case ELLIPSE:
                EllipseVertex ellipseVertex = new EllipseVertex(id, state);
                addVertexModel(ellipseVertex, isRoot);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }

    private void addVertexModel(Vertex vertex, boolean isRoot) {

        addedVertices.add(vertex);
        vertexMap.put( vertex.getVertexId(), vertex);
        this.root = vertex;
    }

    public void addEdge(String sourceId, String targetId) throws Exception {

        addEdge(sourceId, targetId, false, Edge.NULL_COST);
    }

    public void addEdge(String sourceId, String targetId, double cost) throws Exception {

        addEdge(sourceId, targetId, false, cost);
    }

    public void addEdge(String sourceId, String targetId, boolean isOriented, double cost) throws Exception {

        if (cost < 0 && cost != Edge.NULL_COST) {
            throw new Exception("Edge cost cannot be negative. Specified cost: " + cost +
                    getErrorTxt(sourceId, targetId));
        }
        Vertex sourceVertex = vertexMap.get( sourceId);
        Vertex targetVertex = vertexMap.get( targetId);

        if (sourceVertex == null) {
            throw new Exception("Vertex " + sourceId + " not specified in " +
                    "vertex list" + getErrorTxt(sourceId, targetId));
        }
        if (targetVertex == null) {
            throw new Exception("Vertex " + targetId + " not specified in " +
                    "vertex list" + getErrorTxt(sourceId, targetId));
        }

        if (sourceVertex.getVertexChildren().size() > 1) {
            throw new Exception("Parent vertex " + sourceId + " already has more than one children. " +
                    "Binary tree nodes can have 2 children at most" + getErrorTxt(sourceId, targetId));
        }
        if (targetVertex.getVertexParents().size() > 0) {
            throw new Exception("Target vertex " + targetId + " already has a parent. " +
                    "Binary tree nodes can only have one parent vertex" + getErrorTxt(sourceId, targetId));
        }

        Edge edge = new Edge(sourceVertex, targetVertex, isOriented, cost);
        addedEdges.add(edge);
    }

    /**
     * Attach all vertices which don't have a parent to graphParent
     * @param vertexList
     */
    public void attachOrphansToGraphParent( List<Vertex> vertexList) {

        for( Vertex vertex : vertexList) {
            if( vertex.getVertexParents().size() == 0) {
                graphParent.addVertexChild(vertex);
            }
        }

    }

    /**
     * Remove the graphParent reference if it is set
     * @param vertexList
     */
    public void disconnectFromGraphParent( List<Vertex> vertexList) {

        for( Vertex vertex : vertexList) {
            graphParent.removeVertexChild(vertex);
        }
    }

    public void merge() {

        // vertices
        allVertices.addAll(addedVertices);
        allVertices.removeAll(removedVertices);

        addedVertices.clear();
        removedVertices.clear();

        // edges
        allEdges.addAll(addedEdges);
        allEdges.removeAll(removedEdges);

        addedEdges.clear();
        removedEdges.clear();

    }

    public int getGraphDepth() {

        return getGraphDepth(getRoot(), 0);
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

    public void setBTVertexPositions() {

        int graphDepth = getGraphDepth();
        double totalWidth = Math.pow(2, graphDepth) * 65;
        double currentLayoutX = totalWidth / 2;
        getRoot().relocate(currentLayoutX, 50);

        List<Vertex> children = getRoot().getVertexChildren();
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
            getRoot().relocate(currentLayoutX - currentLayoutX/2, 50);
            return;
        }

        child = children.get(1); // right child
        positionVertex(child, currentLayoutX/2);
    }

    /**
     * Recursive function
     * Sets layout coordinates for vertex
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

    /**
     * utility function: cannot create edge source -> target
     * @param source vertex
     * @param target vertex
     * @return error string
     */
    private String getErrorTxt(String source, String target) {

        return ". Cannot create edge " + source + " -> " + target;
    }
}