package com.arnasrad.fbchaining.model;

import com.arnasrad.fbchaining.model.vertex.*;

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
    private Map<String, List<Vertex>> vertexLabelMap; // <label, list of vertices>
    private Map<String, Integer> vertexIdIndexMap;

    public Model() {

        graphParent = new Vertex( "_ROOT_", "root");

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
        vertexLabelMap = new HashMap<>();
        vertexIdIndexMap = new HashMap<>(); // <idKey, index>
        root = null;
    }

    public void clearAddedLists() {
        addedVertices.clear();
        addedEdges.clear();
    }

    public Vertex getRoot() {

        return this.root;
    }

    public Vertex getVertexById(String id) {
        return vertexMap.get(id);
    }

    public Vertex getVertexById(String id, int index) {

        return getVertexById(id.concat(String.valueOf(index)));
    }

    public Vertex getVertexByLabel(String label) {
        List<Vertex> verticesList = vertexLabelMap.get(label);
        if (verticesList == null) {
            return null;
        }

        return verticesList.get(0);
    }
    public Vertex getVertexByLabel(String label, int index) {
        List<Vertex> verticesList = vertexLabelMap.get(label);
        if (verticesList == null) {
            return null;
        }

        return verticesList.get(index);
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

    public Vertex addVertex(String id) {

        return addVertex(id, VertexType.ELLIPSE, Vertex.State.IDLE, false);
    }

    public Vertex addVertex(String id, boolean isRoot) {

        return addVertex(id, VertexType.ELLIPSE, Vertex.State.IDLE, isRoot);
    }

    public Vertex addVertex(String id, VertexType type, Vertex.State state, boolean isRoot) {

        String vertexId = generateVertexId(id);
        switch (type) {

            case RECTANGLE:
                RectangleVertex rectangleVertex = new RectangleVertex(vertexId, id, state);
                addVertexModel(rectangleVertex, isRoot);
                return rectangleVertex;

            case TRIANGLE:
                TriangleVertex triangleVertex = new TriangleVertex(vertexId, id, state);
                addVertexModel(triangleVertex, isRoot);
                return triangleVertex;

            case ELLIPSE:
                EllipseVertex ellipseVertex = new EllipseVertex(vertexId, id, state);
                addVertexModel(ellipseVertex, isRoot);
                return ellipseVertex;

            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }

    private void addVertexModel(Vertex vertex, boolean isRoot) {

        addedVertices.add(vertex);
        vertexMap.put(vertex.getVertexId(), vertex);
        putToLabelMap(vertex);
        
        if (isRoot) {
            this.root = vertex;
        }
    }

    public void addEdge(String sourceId, String targetId) throws Exception {

        addEdge(sourceId, targetId, false, Edge.NULL_COST);
    }

    public void addEdge(String sourceId, String targetId, double cost) throws Exception {

        addEdge(sourceId, targetId, false, cost);
    }

    public void addEdge(String sourceId, String targetId, boolean isOriented) throws Exception {

        addEdge(sourceId, targetId, isOriented, Edge.NULL_COST);
    }

    public void addEdge(String sourceId, String targetId, boolean isOriented, double cost) throws Exception {

        if (cost < 0 && cost != Edge.NULL_COST) {
            throw new Exception("Edge cost cannot be negative. Specified cost: " + cost +
                    getErrorTxt(sourceId, targetId));
        }

        addEdge(sourceId, targetId, isOriented, String.valueOf(cost));
    }

    public void addEdge(String sourceId, String targetId, boolean isOriented, String label) throws Exception {

//        Vertex sourceVertex = vertexMap.get( sourceId);
//        Vertex targetVertex = vertexMap.get( targetId);
        Vertex sourceVertex = getVertexByLabel(sourceId);
        Vertex targetVertex = getVertexByLabel(targetId);

        if (sourceVertex == null) {
            throw new Exception("Vertex " + sourceId + " not specified in " +
                    "vertex list" + getErrorTxt(sourceId, targetId));
        }
        if (targetVertex == null) {
            throw new Exception("Vertex " + targetId + " not specified in " +
                    "vertex list" + getErrorTxt(sourceId, targetId));
        }

        Edge edge = new Edge(sourceVertex, targetVertex, isOriented, label);
        addedEdges.add(edge);
    }

    public void addEdge(Vertex source, Vertex target) throws Exception {

        addEdge(source, target, true, "");
    }

    public void addEdge(Vertex source, Vertex target, boolean isOriented, String label) throws Exception {


        if (source == null) {
            throw new Exception("Source vertex not specified in vertex list.");
        }
        if (target == null) {
            throw new Exception("Vertex not specified in vertex list.");
        }

        Edge edge = new Edge(source, target, isOriented, label);
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

    private String generateVertexId(String id) {

        if (!vertexIdIndexMap.containsKey(id)) {
            vertexIdIndexMap.put(id, 0);
        }

        int vertexIdIndex = vertexIdIndexMap.get(id);
        String vertexId = id.concat(String.valueOf(vertexIdIndex));
        vertexIdIndexMap.put(id, vertexIdIndex+1);
        return vertexId;
    }

    private void putToLabelMap(Vertex vertex) {

        String label = vertex.getLabel();
        vertexLabelMap.computeIfAbsent(label, k -> new ArrayList<>());
        vertexLabelMap.get(label).add(vertex);
    }

    public int getVerticesCount() {

        return this.allVertices.size();
    }

    /**
     * utility function returning error text: cannot create edge source -> target
     * @param source vertex
     * @param target vertex
     * @return error string
     */
    private String getErrorTxt(String source, String target) {

        return ". Cannot create edge " + source + " -> " + target;
    }
}