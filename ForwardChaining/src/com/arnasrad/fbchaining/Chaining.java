package com.arnasrad.fbchaining;

import com.arnasrad.fbchaining.graph.Graph;
import com.arnasrad.fbchaining.graph.SemanticGraph;
import com.arnasrad.fbchaining.graph.SynthesizedGraph;
import com.arnasrad.fbchaining.graph.VerificationGraph;
import com.arnasrad.fbchaining.model.Edge;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.ProductionSystem;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.model.vertex.Vertex;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * credit for JavaFX Graph layout implementation goes to Roland
 * reference: https://stackoverflow.com/questions/30679025/graph-visualisation-like-yfiles-in-javafx
*/
public class Chaining implements Runnable {

    public enum ChainingType {

        FORWARD, // 0;
        BACKWARD, // 1;
        UNDEFINED
    }

    private MainController controller;

    private boolean exists; // specifies whether a path from start to end vertices exists
    private SynthesizedGraph synthesizedGraph;
    private SemanticGraph semanticGraph;
    private VerificationGraph verificationGraph;
    private ProductionSystem productionSystem;
    private ChainingType chainingType;

    private int currentIteration;

    private ArrayList<Vertex> searchPath; // a list of vertices specifying a path form start vertex to end vertex
    private ArrayList<KeyFrame> traversalFrames;
    private Timeline traversalTimeline;
    private int millisecondDelay;
    private int currentTransitionStep;

    // TODO: fix line positioning on graph load

    public Chaining(MainController controller, String fileName) throws Exception {

        this.controller = controller;
        resetInfoFields();
        resetAnimationFields();
        initializeGraphs();
        loadInputFile(fileName);
    }

    @Override
    public void run() {

        ChainingType traversalOption = controller.getTraversalOption();
        setChainingType(traversalOption);

        millisecondDelay = 0;
        currentTransitionStep = 0;

        addTraversalFrameDelay(e -> printInfoData());

        if (traversalOption.equals(ChainingType.FORWARD)) {
            runForwardChaining();
        } else if (traversalOption.equals(ChainingType.BACKWARD)) {
            runBackwardChaining();
        } else {
            return;
        }

        traversalTimeline = new Timeline();
        for(KeyFrame key : traversalFrames) {

            traversalTimeline.getKeyFrames().add(key);
        }
        Platform.runLater(traversalTimeline::play);
    }

    public void reset() throws Exception {

        resetInfoFields();
        resetProductionSystem();
        resetGraph();
    }

    private void resetInfoFields() {

        stopTraversalAnimation();
        resetAnimationFields();
        this.exists = false;
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

    private void initializeGraphs() {

        this.synthesizedGraph = new SynthesizedGraph(controller);
        this.semanticGraph = new SemanticGraph(controller);
        this.verificationGraph = new VerificationGraph(controller);
    }

    private void initializeLayouts() {

        initializeSemanticLayout();
        initializeSemanticLayout();
        initializeVerificationLayout();
    }

    private void initializeSynthesizedLayout() {

        synthesizedGraph.initializeLayout();
    }

    private void initializeSemanticLayout() {

        semanticGraph.initializeLayout();
    }

    private void initializeVerificationLayout() {

        verificationGraph.initializeLayout();
    }

    private void resetProductionSystem() {

        this.productionSystem.reset();
    }

    private void resetGraph() throws Exception {

        ArrayList<Vertex> graphVertices = new ArrayList<>(synthesizedGraph.getModel().getAllVertices());
        for(Vertex vertex : graphVertices) {

            vertex.setState(Vertex.State.IDLE);
        }

        setEdgesUnoriented();
    }

    private void restartGraphs() throws Exception {

        restartSynthesizedGraph();
        restartSemanticGraph();
        restartVerificationGraph();
    }

    private void restartSynthesizedGraph() throws Exception {

        Model model = synthesizedGraph.getModel();
        synthesizedGraph.resetContainers();
        this.synthesizedGraph = new SynthesizedGraph(controller);
        this.synthesizedGraph.copyModel(model);
        initializeSynthesizedLayout();
    }

    private void restartSemanticGraph() throws Exception {

        Model model = semanticGraph.getModel();
        semanticGraph.resetContainers();
        this.semanticGraph = new SemanticGraph(controller);
        this.semanticGraph.copyModel(model);
        initializeSemanticLayout();
    }

    private void restartVerificationGraph() throws Exception {

        Model model = verificationGraph.getModel();
        verificationGraph.resetContainers();
        this.verificationGraph = new VerificationGraph(controller);
        this.verificationGraph.copyModel(model);
        initializeVerificationLayout();
    }

    private void loadInputFile(String fileName) throws Exception {

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(new File(fileName)));

            br.readLine();br.readLine(); br.readLine(); // skip the first three lines (file header + rules caption)

            int lineNo = 4;
            // read the rules of production system
            String line, result;
            String[] facts;
            ArrayList<Rule> rules = new ArrayList<>();
            int ruleIndex = 1;
            while(!(line = br.readLine()).equals("")) {

                String[] rule = line.split(" ");

                if (rule.length == 0) {
                    throw new Exception("ERROR: No rule names specified in " +
                            "input file line " + lineNo);
                } else if (rule.length == 1) {
                    throw new Exception("ERROR: a rule must contain more than one " +
                            "fact to make a rule");
                }

                facts = new String[rule.length - 1];
                result = rule[0];
                for(int i = 1; i < rule.length; ++i) {
                    facts[i-1] = rule[i];
                }
                rules.add(new Rule("R" + ruleIndex, result, facts));
                ++ruleIndex;

                ++lineNo;
            }

            br.readLine(); // skip the following line (facts caption)
            // load facts
            if ((line = br.readLine()) == null) {

                throw new Exception("ERROR: Incorrect input file format. " +
                        "Facts line must not be empty.");
            }

            int i = 0;
            facts = line.split(" ");

            br.readLine();br.readLine(); // skip the following two lines (target caption)
            line = br.readLine();
            if (line == null) {

                throw new Exception("ERROR: Incorrect input file format. " +
                        "Target line must not be empty.");
            }

            String[] targets = line.split(" ");
            if (targets.length != 1) {

                throw new Exception("ERROR: Incorrect input file format. " +
                        "There must be only one target. Found: " + targets.length);
            }
            String target = targets[0];

            controller.setFactsCountLbl(facts.length);
            controller.setRulesCountLbl(rules.size());

            productionSystem = new ProductionSystem(rules, facts, target);

        } catch (IOException e) {

            throw new Exception("ERROR: No such file exists. Enter a valid file name");
        } catch (Exception e) {

//            throw new Exception("ERROR occurred while reading input file.");
            throw e;
        } finally {

            initializeLayouts();
        }
    }

    private void readVertices(BufferedReader br) {


    }

    private void readVerticesCost(BufferedReader br) {


    }

    public ChainingType getGraphSearchType(int type) {

        switch (type) {

            case 0:
                return ChainingType.FORWARD;
            case 1:
                return ChainingType.BACKWARD;
            default:
                return ChainingType.UNDEFINED;
        }
    }

    public void setChainingType(ChainingType type) {

        this.chainingType = type;
    }

    public Graph getSynthesizedGraph() {
        return this.synthesizedGraph;
    }

    public void setStartVertex(Vertex vertex) {

        vertex.setState(Vertex.State.CURRENT);
//        this.startVertex = vertex;
    }

    public void setTargetVertex(Vertex vertex) {

        vertex.setState(Vertex.State.TARGET);
//        this.targetVertex = vertex;
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

            fileWriter.write("1 DALIS. Duomenys\n\n");
            fileWriter.write("\t1) Taisyklės\n");
            fileWriter.write(productionSystem.getRulesString() + "\n");
            fileWriter.write("\t2) Faktai: " +
                    productionSystem.getFactsString() + ".\n\n");
            fileWriter.write("\t3) Tikslas " +
                    productionSystem.getTarget() + ".\n\n");
            fileWriter.write("\t4) Naudojama procedūra: ");
            fileWriter.write(controller.getTraversalOption() + "\n\n");
            fileWriter.write("2 DALIS. Vykdymas\n\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printResults() {

        try {

            FileWriter fileWriter = controller.getFileWriter();
            fileWriter.write("\n3 DALIS. Rezultatai\n");

            if (exitExists()) {

                fileWriter.write("\t1) Tikslas " + productionSystem.getTarget() +
                        " išvestas. Bandymų " + getCurrentIteration() + "\n");

                fileWriter.write("\n2) Kelias: " +
                        getSearchPathString() + "\n");
            } else {

                fileWriter.write("\t3.1) Tikslas negali būti pasiektas. " +
                        "Kelias neegzistuoja\n");
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
    private void runForwardChaining() {

        int iteration = 0;

        while (true) {

            StringBuilder sb = new StringBuilder("\t" + (iteration+1) + " ITERACIJA\n");

            ArrayList<Rule> rules = productionSystem.getRules();

            int i = 0;
            for(Rule rule : rules) {

                ++i;
                sb.append("\t\tR").append(i).append(":").append(rule).append(" ");

                byte flag;
                if ((flag = rule.getFlag()) != 0) {

                    sb.append("praleidžiame, nes pakelta flag").append(flag).append("\n");
                    continue;
                } else if (productionSystem.resultInFacts(rule)) {

                    sb.append("netaikome, nes konsekventas faktuose. Pakeliame flag2\n");
                    continue;
                }

                try {
                    ArrayList<String> absentFacts = productionSystem.applyRule(rule);

                    if (absentFacts.size() == 0) {
                        // TODO: update graphs with vertices on rule application
                        addTraversalFrameDelay(e -> synthesizedGraph.apply(rule));
                        addTraversalFrameDelay(e -> semanticGraph.apply(rule));
                        addTraversalFrameDelay(e -> verificationGraph.apply(rule));
                        ++currentTransitionStep;
                        sb.append("taikome. Pakeliame flag1. Faktai ")
                                .append(productionSystem.getFactsString());
                        break;
                    } else {

                        sb.append("netaikome, nes trūksta");
                        for (String fact : absentFacts) {
                            sb.append(" ").append(fact);
                        }
                        sb.append("\n");
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }

                if (!this.exists && i == rules.size()) {
                    // solution does not exist
                    addTraversalFrameDelay(e -> controller.processEndOfTraversal());
                    break;
                }
            }

            if (productionSystem.isTargetReached()) {

                this.exists = true;
                sb.append("\n\t\tTikslas gautas.");
                addTraversalFrameDelay(e -> writeToDefaultFile(sb));
                addTraversalFrameDelay(e -> controller.processEndOfTraversal());
                break;
            }

            addTraversalFrameDelay(e -> writeToDefaultFile(sb));
            ++iteration;
        }

    }

    private void runBackwardChaining() {

    }

    private void orderListByCost(ArrayList<Vertex> vertices) {

        vertices.sort((o1, o2) -> Double.compare(o2.getPathCost(), o1.getPathCost()));
    }

    private ArrayList<Vertex> getOpenVertices(Vertex vertex, List<Vertex> openedVertices, List<Vertex> closedVertices) {

        switch(chainingType) {
            case FORWARD:
                return getAdjacentVerticesDFS(vertex, openedVertices, closedVertices);
//            case INFIX:
//                return getAdjacentVerticesDijkstra(vertex, openedVertices, closedVertices);
//            case POSTFIX:
//                return getAdjacentVerticesDFS(vertex, openedVertices, closedVertices);
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

                    Edge edge = synthesizedGraph.getModel().getEdge(vertex, adjVertex);
                    double newCost = vertex.getPathCost() + Double.parseDouble(edge.getLabel());
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

//        if (endVertex == null) {
//
//            searchPath = null;
//            return;
//        }
//
//        Vertex currentVertex = endVertex;
//        while(!currentVertex.equals(startVertex)) {
//
//            if(currentVertex == null) {
//
//                searchPath = null;
//                return;
//            }
//
//            if (currentVertex != endVertex) {
//
//                currentVertex.setState(Vertex.State.PATH);
//            }
//
//            searchPath.add(currentVertex);
//            currentVertex = currentVertex.getSourceParent();
//        }
//        currentVertex.setState(Vertex.State.PATH);
//        searchPath.add(currentVertex); // add the last source parent (start vertex)
//
//        reverseSearchPath();
//        setSearchPathOrientated();
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
        Model model = synthesizedGraph.getModel();
        for(int i = 0; i < searchPath.size()-1; ++i) {

            source = searchPath.get(i);
            target = searchPath.get(i+1);
            edge = model.getEdge(source, target);
            edge.defineOrientation(source, target);
            edge.setOriented(true);
        }
    }

    private void setEdgesUnoriented() {

        List<Edge> edges = synthesizedGraph.getModel().getAllEdges();
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
