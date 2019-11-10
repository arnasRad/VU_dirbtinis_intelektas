package com.arnasrad.binarysearchtree;

import com.arnasrad.binarysearchtree.layout.ZoomableScrollPane;
import com.arnasrad.binarysearchtree.model.Edge;
import com.arnasrad.binarysearchtree.model.vertex.Vertex;
import com.arnasrad.binarysearchtree.model.vertex.VertexLayer;
import com.arnasrad.binarysearchtree.model.Model;
import com.arnasrad.binarysearchtree.model.vertex.VertexType;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

public class Graph {

    private Model model;

    private Group canvas;

    private ZoomableScrollPane scrollPane;

    private MouseGestures mouseGestures;

    private MainController controller;

    /**
     * the pane wrapper is necessary or else the scrollpane would always align
     * the top-most and left-most child to the top and left eg when you drag the
     * top child down, the entire scrollpane would move down
     */
    private VertexLayer vertexLayer;

    public Graph(MainController controller) {

        this.controller = controller;

        this.model = new Model();

        setupContainers();
    }

    public ScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public Pane getVertexLayer() {
        return this.vertexLayer;
    }

    public Model getModel() {
        return model;
    }

    public void beginUpdate() {
    }

    public void resetContainers() {

        this.vertexLayer.getChildren().clear();
        this.canvas.getChildren().clear();

        setupContainers();
    }

    private void setupContainers() {

        canvas = new Group();
        vertexLayer = new VertexLayer();

        canvas.getChildren().add(vertexLayer);

        mouseGestures = new MouseGestures(controller, this);

        scrollPane = new ZoomableScrollPane(canvas);

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

    }

    public void copyModel(Model model) throws Exception {

        this.model = new Model();
        beginUpdate();

        for(Vertex vertex : model.getAllVertices()) {

            this.model.addVertex(vertex.getVertexId(),
                    VertexType.ELLIPSE, Vertex.State.IDLE);
        }

        for(Edge edge : model.getAllEdges()) {

            this.model.addEdge(edge.getSource().getVertexId(),
                    edge.getTarget().getVertexId());
        }

        endUpdate();
    }

    public void endUpdate() {

        // add components to graph pane
        getVertexLayer().getChildren().addAll(model.getAddedEdges());
        getVertexLayer().getChildren().addAll(model.getAddedVertices());

        // remove components from graph pane
        getVertexLayer().getChildren().removeAll(model.getRemovedVertices());
        getVertexLayer().getChildren().removeAll(model.getRemovedEdges());

        // enable dragging of vertices
        for (Vertex vertex : model.getAddedVertices()) {
            mouseGestures.makeDraggable(vertex);
        }

        // every vertex must have a parent, if it doesn't, then the graphParent is
        // the parent
        getModel().attachOrphansToGraphParent(model.getAddedVertices());

        // remove reference to graphParent
        getModel().disconnectFromGraphParent(model.getRemovedVertices());

        // merge added & removed vertices with all vertices
        getModel().merge();

    }

    public double getScale() {
        return this.scrollPane.getScaleValue();
    }
}