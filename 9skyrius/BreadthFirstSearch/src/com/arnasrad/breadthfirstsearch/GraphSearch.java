package com.arnasrad.breadthfirstsearch;

import com.arnasrad.breadthfirstsearch.layout.Layout;
import com.arnasrad.breadthfirstsearch.layout.RandomLayout;
import com.arnasrad.breadthfirstsearch.model.Edge;
import com.arnasrad.breadthfirstsearch.model.Model;
import com.arnasrad.breadthfirstsearch.model.vertex.Vertex;
import com.arnasrad.breadthfirstsearch.model.vertex.VertexType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.io.*;
import java.util.*;

/**
 * credit for JavaFX Graph layout implementation goes to Roland
 * reference: https://stackoverflow.com/questions/30679025/graph-visualisation-like-yfiles-in-javafx
*/
public class GraphSearch implements Runnable {

    public enum SearchType {

        BFS, // 0; breadth first search without cost
        DIJKSTRA, // 1; unoriented search with costs
        DFS, // 2; depth first search without cost
        UNDEFINED
    }

    private MainController controller;

    private Vertex startVertex, targetVertex; // search start and end vertices
    private boolean exists; // specifies whether a path from start to end vertices exists
    private Graph graph;
    private SearchType graphSearchType;
    private boolean isGraphOriented;

    private int currentIteration;

    private ArrayList<Vertex> searchPath; // a list of vertices specifying a path form start vertex to end vertex
    private ArrayList<KeyFrame> traversalFrames;
    private Timeline traversalTimeline;
    private int millisecondDelay;
    private int currentTransitionStep;

    // TODO: fix line positioning on graph load
    // TODO: hide edge costs on search type change in menu
    // TODO: test search when there is no solution to the problem (disconnected graphs)

    public GraphSearch(MainController controller, String fileName) throws Exception {

        this.controller = controller;
        resetInfoFields();
        resetAnimationFields();
        initializeGraph();
        loadGraph(fileName);
    }

    @Override
    public void run() {

        SearchType traversalOption = controller.getTraversalOption();
        setGraphSearchType(traversalOption);

        millisecondDelay = 0;
        currentTransitionStep = 0;

        addTraversalFrame(e -> printInfoData());

        if (traversalOption.equals(SearchType.BFS) ||
            traversalOption.equals(SearchType.DIJKSTRA) ||
            traversalOption.equals(SearchType.DFS)) {

            runBFS();
        }

        traversalTimeline = new Timeline();
        for(KeyFrame key : traversalFrames) {

            traversalTimeline.getKeyFrames().add(key);
        }
        Platform.runLater(traversalTimeline::play);
    }

    public void reset() throws Exception {

        resetInfoFields();
        resetGraph();
    }

    private void resetInfoFields() {

        stopTraversalAnimation();
        resetAnimationFields();
        this.exists = false;
        startVertex = null;
        targetVertex = null;
//        setStartingVertex(new Vertex("null"));
//        setFinishVertex(new Vertex("null"));
//        resetAnimationFields();
        currentIteration = 0;
    }

    private void stopTraversalAnimation() {

        if (traversalTimeline != null) {
            traversalTimeline.stop();
        }
    }

    private void resetAnimationFields() {

        this.searchPath = new ArrayList<>();
        this.traversalFrames = new ArrayList<>();
        this.traversalTimeline = new Timeline();
    }

    private void initializeGraph() {

        this.graph = new Graph(controller);
    }

    private void initializeLayout() {

        Layout layout = new RandomLayout(graph);
        layout.execute();
    }

    private void resetGraph() throws Exception {

        ArrayList<Vertex> graphVertices = new ArrayList<>(graph.getModel().getAllVertices());
        for(Vertex vertex : graphVertices) {

            vertex.setState(Vertex.State.IDLE);
        }

        setEdgesUnoriented();
    }

    private void restartGraph() throws Exception {

        Model model = graph.getModel();
        graph.resetContainers();
        this.graph = new Graph(controller);
        this.graph.copyModel(model);
        initializeLayout();
    }

    private void loadGraph(String fileName) throws Exception {

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(new File(fileName)));

            br.readLine(); // ignore the first line
            // graph search type
            String line = br.readLine();
            if (line == null) {
                throw new Exception("ERROR: Incorrect input file format. " +
                        "Second line must not be empty.");
            }

//            int type = Integer.parseInt(line);
//            setGraphSearchType(getGraphSearchType(type));
//
//            // is graph oriented?
//            line = br.readLine();
//            if (line == null) {
//                throw new Exception("ERROR: Incorrect input file format. " +
//                        "Third line must not be empty.");
//            }
//
//            int isOriented = Integer.parseInt(line);
//            if (isOriented == 0) {
//
//                this.isGraphOriented = true;
//            } else {
//
//                this.isGraphOriented = false;
//            }
//
//            // ignore the two following lines
//            br.readLine(); br.readLine();
//            line = br.readLine();
//
//            if (line == null) {
//                throw new Exception("ERROR: Incorrect input file format. " +
//                        "Sixth line must not be empty.");
//            }

            // the sixth line of input file contains vertex names
            String[] names = line.split(" ");

            if (names.length == 0) {
                throw new Exception("ERROR: No vertex names specified in " +
                        "input file");
            } else if (names.length == 1) {
                throw new Exception("ERROR: a graph must contain more than one " +
                        "vertex to search for a path");
            }

            // leave only distinct vertex names
            Set<String> distinctNames = new HashSet<>(Arrays.asList(names));

            Model model = graph.getModel();

            graph.beginUpdate();

            // load vertex names
            for (String name : distinctNames) {

                model.addVertex(name, VertexType.ELLIPSE, Vertex.State.IDLE);
            }

            controller.setVertexCountLbl(distinctNames.size());
            // ignore the two following lines
            br.readLine(); br.readLine();
            String[] edgeVertices;
            int fileLine = 4; // BufferedReader is currently on input file line 4

            // read the rest of the input
            while ((line = br.readLine()) != null) {

                ++fileLine;
                // each line must contain only names of adjacent vertices
                // separated by a whitespace
                edgeVertices = line.split(" ");

//                if (graphSearchType == SearchType.BFS) {
//                    if (edgeVertices.length < 2 || edgeVertices.length > 3) {
//                        throw new Exception("ERROR: incorrect count of vertices in input file line " + fileLine +
//                                ". Must contain only two vertex names separated by a whitespace");
//                    }
//
//                    model.addEdge(edgeVertices[0], edgeVertices[1]);
//                } else if (graphSearchType == SearchType.DIJKSTRA) {
                if (edgeVertices.length != 3) {
                    throw new Exception("ERROR: incorrect input file line " + fileLine +
                            ". Must contain two vertex names followed by cost double value, " +
                            "all of which are separated by a whitespace");
                }

                model.addEdge(edgeVertices[0], edgeVertices[1],
                        Double.parseDouble(edgeVertices[2]));
//                }
            }

        } catch (IOException e) {

            graph.endUpdate();
            throw new Exception("ERROR: No such file exists. Enter a valid file name");
        } catch (Exception e) {

            graph.endUpdate();
//            throw new Exception("ERROR occurred while reading input file.");
            throw e;
        } finally {

            graph.endUpdate();
            initializeLayout();
        }
    }

    private void readVertices(BufferedReader br) {


    }

    private void readVerticesCost(BufferedReader br) {


    }

    public SearchType getGraphSearchType(int type) {

        switch (type) {

            case 0:
                return SearchType.BFS;
            case 1:
                return SearchType.DIJKSTRA;
            default:
                return SearchType.UNDEFINED;
        }
    }

    public void setGraphSearchType(SearchType type) {

        this.graphSearchType = type;
    }

    public Graph getGraph() {
        return this.graph;
    }

    public void setStartVertex(Vertex vertex) {

        vertex.setState(Vertex.State.CURRENT);
        this.startVertex = vertex;
    }

    public void setTargetVertex(Vertex vertex) {

        vertex.setState(Vertex.State.TARGET);
        this.targetVertex = vertex;
    }

    public boolean exitExists() {

        return this.exists;
    }

    public int getCurrentIteration() {

        return this.currentIteration;
    }

    /**** PRINT OUTPUT DATA ****/

    private void printInfoData() {

        try {
            FileWriter fileWriter = controller.getFileWriter();

            fileWriter.write("1 DALIS. Duomenys\n");
            fileWriter.write("\t1.1. Grafas\n\n");
            printGraph();
            fileWriter.write("\t1.2. Pradinė grafo viršūnė: " +
                    startVertex + ".\n\n");
            fileWriter.write("\t1.3. Terminalnė grafo viršūnė: " +
                    targetVertex + ".\n\n");
            fileWriter.write("\t1.3. Naudojama procedūra: ");
            fileWriter.write(controller.getTraversalOption() + "\n\n");
            fileWriter.write("2 DALIS. Vykdymas\n\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printGraph() {

        FileWriter fileWriter = controller.getFileWriter();
        try {

            fileWriter.write("");
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void printResults() {

        try {

            FileWriter fileWriter = controller.getFileWriter();
            fileWriter.write("\n3 DALIS. Rezultatai\n");

            if (exitExists()) {

                fileWriter.write("\t3.1) Kelias rastas. Bandymų " +
                        getCurrentIteration() + "\n");

//                fileWriter.write("\t2) Kelias grafiškai\n");
//
//                printGraph();
//
//                fileWriter.write("\n3.3. Kelias taisyklėmis: " +
//                        getProdPath() + "\n");

                fileWriter.write("\n3.2) Kelias viršūnėmis: " +
                        getSearchPathString() + "\n");
            } else {

                fileWriter.write("\t3.1) Kelias neegzistuoja\n");
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**** WRITE TO OUTPUT FILE ****/
    private void writeToDefaultFile(StringBuilder sb) {

        FileWriter fileWriter = controller.getFileWriter();
        try {
            fileWriter.write(sb.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToDefaultFile(String str) {

        FileWriter fileWriter = controller.getFileWriter();
        try {
            fileWriter.write(str + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**** UPDATE UI LABELS ****/

    private void updateCurrentIterationLbl(int iteration) {

        controller.setCurrentIterationLbl(iteration);
    }

    private void updateCurrentVertexLbl(Vertex vertex) {

        controller.setCurrentVertexLbl(vertex);
    }

    /**** APPEND ANIMATION FRAMES ****/

    private void addTraversalFrame(EventHandler<ActionEvent> eventHandler) {

        traversalFrames.add(new KeyFrame(
                Duration.millis(controller.getTraversalSpeed() * currentTransitionStep
                        + millisecondDelay), eventHandler));

    }

    private void addTraversalFrameDelay(EventHandler<ActionEvent> eventHandler) {

        traversalFrames.add(new KeyFrame(
                Duration.millis(controller.getTraversalSpeed() * currentTransitionStep
                        + millisecondDelay), eventHandler));
        millisecondDelay += 5; // used for correct step ordering
    }

    /********* ALGORITHMS *********/
    private void runBFS() {

        ArrayList<Vertex> open = new ArrayList<>();
        ArrayList<Vertex> closed = new ArrayList<>();

        open.add(startVertex);

        while(!open.isEmpty() && !exists) {

            StringBuilder sb = new StringBuilder((currentIteration+1) + ". ---------------------------------\n");

            if (graphSearchType == SearchType.DIJKSTRA) {
                orderListByCost(open);
            }
            Vertex currentVertex = open.get(0);
            addTraversalFrame(e -> currentVertex.setState(Vertex.State.CURRENT));
            addTraversalFrame(e -> updateCurrentVertexLbl(currentVertex));
            int tempCurrentIteration = currentIteration;
            addTraversalFrame(e -> updateCurrentIterationLbl(tempCurrentIteration+1));

            ++currentTransitionStep;
            ++currentIteration;

            if (currentVertex.equals(targetVertex)) {

                sb.append("Rasta terminalinė viršūnė ").append(currentVertex);
                addTraversalFrame(e -> writeToDefaultFile(sb));

                this.exists = true;
                addTraversalFrame(e -> deriveSearchPath(currentVertex));
                addTraversalFrame(e -> currentVertex.setState(Vertex.State.TARGET_REACHED));
                addTraversalFrameDelay(e -> controller.processEndOfTraversal());
                return;
            }

            sb.append("\tATIDARYTA: ").append(getVerticesListString(open)).append("\n");
            sb.append("\tUŽDARYTA: ").append(getVerticesListString(closed));
            addTraversalFrame(e -> writeToDefaultFile(sb));

            closed.add(currentVertex);
            addTraversalFrame(e -> currentVertex.setState(Vertex.State.CLOSED));
            open.remove(0);
            ArrayList<Vertex> newOpen = null;

            newOpen = getOpenVertices(currentVertex, open, closed);

            if (newOpen != null) {
                for(Vertex vertex : newOpen) {

                    vertex.setSourceParent(currentVertex);
                    addTraversalFrame(e -> vertex.setState(Vertex.State.OPENED));
                }

                if(graphSearchType == SearchType.DFS) {
                    open.addAll(0, newOpen);
                } else {
                    open.addAll(newOpen);
                }
            }
        }
    }

    private void orderListByCost(ArrayList<Vertex> vertices) {

        vertices.sort((o1, o2) -> Double.compare(o2.getPathCost(), o1.getPathCost()));
    }

    private ArrayList<Vertex> getOpenVertices(Vertex vertex, List<Vertex> openedVertices, List<Vertex> closedVertices) {

        switch(graphSearchType) {
            case BFS:
                return getAdjacentVerticesBFS(vertex, openedVertices, closedVertices);
            case DIJKSTRA:
                return getAdjacentVerticesDijkstra(vertex, openedVertices, closedVertices);
            case DFS:
                return getAdjacentVerticesDFS(vertex, openedVertices, closedVertices);
            default:
                return null;
        }
    }

    private ArrayList<Vertex> getAdjacentVerticesBFS(Vertex vertex, List<Vertex> openedVertices, List<Vertex> closedVertices) {

        ArrayList<Vertex> adjacentVertices = new ArrayList<>(vertex.getAdjacentVertices());
        ArrayList<Vertex> filteredList = new ArrayList<>();

        for(Vertex adjVertex : adjacentVertices) {
            if (!openedVertices.contains(adjVertex)
                    && !closedVertices.contains(adjVertex)) {

                filteredList.add(adjVertex);
            }
        }

        if (filteredList.size() == 0)
            return null;

        return filteredList;
    }

    private ArrayList<Vertex> getAdjacentVerticesDijkstra(Vertex vertex, List<Vertex> openedVertices, List<Vertex> closedVertices) {

        ArrayList<Vertex> adjacentVertices = new ArrayList<>(vertex.getAdjacentVertices());
        ArrayList<Vertex> filteredList = new ArrayList<>();

        for(Vertex adjVertex : adjacentVertices) {
            if (!closedVertices.contains(adjVertex)) {

                if (openedVertices.contains(adjVertex)) {

                    Edge edge = graph.getModel().getEdge(vertex, adjVertex);
                    double newCost = vertex.getPathCost() + edge.getCost();
                    if (newCost < adjVertex.getPathCost()) {

                        adjVertex.setSourceParent(vertex);
                        adjVertex.setPathCost(newCost);
                    }
                } else {

                    filteredList.add(adjVertex);
                }
            }
        }

        if (filteredList.size() == 0)
            return null;

        return filteredList;
    }

    private ArrayList<Vertex> getAdjacentVerticesDFS(Vertex vertex, List<Vertex> openedVertices, List<Vertex> closedVertices) {

        ArrayList<Vertex> adjacentVertices = new ArrayList<>(vertex.getAdjacentVertices());
        ArrayList<Vertex> filteredList = new ArrayList<>();

        for(Vertex adjVertex : adjacentVertices) {
            if (!closedVertices.contains(adjVertex)) {

                filteredList.add(adjVertex);
                openedVertices.remove(adjVertex);
            }
        }

        if (filteredList.size() == 0)
            return null;

        return filteredList;
    }

    private String getVerticesListString(ArrayList<Vertex> vertices) {

        if (vertices == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder("[");

        int i = 0;
        for(Vertex vertex : vertices) {

            String sourceParentString = "pradžia";
            if (vertex.getSourceParent() != null) {
                sourceParentString = vertex.getSourceParent().toString();
            }
            sb.append(vertex).append("(").append(sourceParentString).append(")");

            if (i < vertices.size()-1) {
                sb.append(", ");
            }
            ++i;
        }
        sb.append("]");
        return sb.toString();
    }

    /********* DERIVE VERTICES PATH *********/

    public void deriveSearchPath(Vertex endVertex) {

        if (endVertex == null) {

            searchPath = null;
            return;
        }

        Vertex currentVertex = endVertex;
        while(!currentVertex.equals(startVertex)) {

            if(currentVertex == null) {

                searchPath = null;
                return;
            }

            if (currentVertex != endVertex) {

                currentVertex.setState(Vertex.State.PATH);
            }

            searchPath.add(currentVertex);
            currentVertex = currentVertex.getSourceParent();
        }
        currentVertex.setState(Vertex.State.PATH);
        searchPath.add(currentVertex); // add the last source parent (start vertex)

        reverseSearchPath();
        setSearchPathOrientated();
    }

    private void reverseSearchPath() {

        int vertexListSize = searchPath.size();
        for(int i = 0; i < vertexListSize / 2; ++i) {

            Vertex vertex = searchPath.get(vertexListSize-i-1);
            searchPath.set(vertexListSize-i-1, searchPath.get(i));
            searchPath.set(i, vertex);
        }
    }

    private void setSearchPathOrientated() {

        if (searchPath.size() < 2) {

            return;
        }

        Vertex source, target;
        Edge edge;
        Model model = graph.getModel();
        for(int i = 0; i < searchPath.size()-1; ++i) {

            source = searchPath.get(i);
            target = searchPath.get(i+1);
            edge = model.getEdge(source, target);
            edge.defineOrientation(source, target);
            edge.setOriented(true);
        }
    }

    private void setEdgesUnoriented() {

        List<Edge> edges = graph.getModel().getAllEdges();
        for(Edge edge : edges) {

            edge.setOriented(false);
        }
    }

    public String getSearchPathString() {
        if (searchPath == null || searchPath.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Vertex vertex : searchPath) {

            sb.append(vertex.getVertexId()).append(" -> ");
            ++i;
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 4);

        return str;
    }


    // how the program should work:
    // 1. INPUT
    // 1.1. read the graph input from file
    // 1.1.1. INPUT FILE EXAMPLE
    //          > 1. META-DATA // line is ignored by scanner
    //          > 0 // search type; 0 - breadth first (no edge cost); other types are defined in GraphSearch class
    //          > 0 // is graph oriented? 0 - no, 1 - yes
    //          > // empty line to mark end of reading vertex input
    //          > 2. Vertices // line is ignored by scanner
    //          > a b c d e f // vertex names are separated by a whitespace
    //          > a // vertex already defined -> ERROR or IGNORE???
    //          > // empty line to mark end of reading vertex input
    //          > 3. Edges // line is ignored by scanner
    //          > a b // a, b - names of edge vertices; must be separated by a whitespace
    //          > a e
    //          > b a // edge already defined -> ERROR or IGNORE???
    //          > b d
    //          > b e
    //          > e f
    //          > e g
    //          > e h
    // 1.2. convert the .txt input to java objects
    // 1.3. create a backup graph for resets
    // 1.4. show the graph
    // 2. OUTPUT
    // 2.1. specify output filename
    // 3. COORDINATES
    // 3.1. click on a graph vertex to set it as starting vertex
    // 3.2. click on a graph vertex to set it as end vertex
    // 4. TRAVERSAL
    // 4.1. click RUN to begin the traversal
    // 4.2. on a background thread: breadth first search algorithm
    //          calculates a path from starting to end vertex
    // 4.3. traversal steps are shown similarly as in wikipedia
    //          note: https://en.wikipedia.org/wiki/Breadth-first_search
    // 4.4. traversal steps are stored in Timeline object as KeyFrames
    // 4.5. animate the traversal after calculation.
    // 4.6. traversal animation speed can be regulated by slider
    // 5. FINISH
    // 5.1. reset the graph and choose new path vertices or
    //          open a new graph from an input file
}
