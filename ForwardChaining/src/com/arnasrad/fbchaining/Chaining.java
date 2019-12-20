package com.arnasrad.fbchaining;

import com.arnasrad.fbchaining.graph.*;
import com.arnasrad.fbchaining.model.Edge;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.ProductionSystem;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.model.vertex.Vertex;
import com.arnasrad.fbchaining.utility.Utils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
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
    private SemanticGraphForward semanticGraphForward;
    private SemanticGraphBackward semanticGraphBackward;
    private VerificationGraph verificationGraph;
    private ProductionSystem productionSystem;
    private ChainingType chainingType;

    private int currentIteration;
    private ArrayList<String> addedFacts; // used in backward chaining algorithm
    private ArrayList<String> newFacts; // used in backward chaining algorithm
    private int currentDepth; // used in backward chaining algorithm

    private ArrayList<String> searchPath; // a list of vertices specifying a path from start vertex to end vertex
    private ArrayList<KeyFrame> traversalFrames;
    private Timeline traversalTimeline;
    private int millisecondDelay;
    private int currentTransitionStep;

    public Chaining(MainController controller, String fileName) throws Exception {

        this.controller = controller;
        resetInfoFields();
        resetAnimationFields();
        initializeMainGraph();
        loadInputFile(fileName);
    }

    @Override
    public void run() {

        ChainingType traversalOption = controller.getTraversalOption();
        setChainingType(traversalOption);

        millisecondDelay = 0;
        currentTransitionStep = 0;
//
//        addTraversalFrame(e -> printInfoData());

        if (traversalOption.equals(ChainingType.FORWARD)) {
            addTraversalFrame(e -> printInfoData());
            runForwardChaining();
        } else if (traversalOption.equals(ChainingType.BACKWARD)) {
            printInfoData();
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
        resetAnimationFields();
        resetProductionSystem();
        initializeMainGraph();
        initializeOptionalGraphs();
        resetGraph();
    }

    private void resetInfoFields() {

        stopTraversalAnimation();
        resetAnimationFields();
        this.exists = false;
        currentIteration = 0;
        currentDepth = 0;
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

    private void initializeMainGraph() {

        this.synthesizedGraph = new SynthesizedGraph(controller);
    }

    private void initializeOptionalGraphs() {

        this.semanticGraphForward = new SemanticGraphForward(controller);
        this.semanticGraphBackward = new SemanticGraphBackward(controller);
        this.verificationGraph = new VerificationGraph(controller, productionSystem.getFacts());
    }

    private void resetProductionSystem() {

        this.productionSystem.reset();
    }

    private void resetGraph() throws Exception {

        ArrayList<Vertex> graphVertices = new ArrayList<>(synthesizedGraph.getModel().getAllVertices());
        for(Vertex vertex : graphVertices) {

            vertex.setState(Vertex.State.IDLE);
        }

    }

    private void restartGraphs() throws Exception {

        restartSynthesizedGraph();
        restartSemanticForwardGraph();
        restartSemanticBackwardGraph();
        restartVerificationGraph();
    }

    private void restartSynthesizedGraph() throws Exception {

        Model model = synthesizedGraph.getModel();
        synthesizedGraph.resetContainers();
        this.synthesizedGraph = new SynthesizedGraph(controller);
        this.synthesizedGraph.copyModel(model);
//        initializeSynthesizedLayout();
    }

    private void restartSemanticForwardGraph() throws Exception {

        Model model = semanticGraphForward.getModel();
        semanticGraphForward.resetContainers();
        this.semanticGraphForward = new SemanticGraphForward(controller);
        this.semanticGraphForward.copyModel(model);
    }

    private void restartSemanticBackwardGraph() throws Exception {

        Model model = semanticGraphBackward.getModel();
        semanticGraphBackward.resetContainers();
        this.semanticGraphBackward = new SemanticGraphBackward(controller);
        this.semanticGraphBackward.copyModel(model);
    }

    private void restartVerificationGraph() throws Exception {

        Model model = verificationGraph.getModel();
        verificationGraph.resetContainers();
        this.verificationGraph = new VerificationGraph(controller, productionSystem.getFacts());
        this.verificationGraph.copyModel(model);
//        initializeVerificationLayout();
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
            initializeOptionalGraphs();

        } catch (IOException e) {

            throw new Exception("ERROR: No such file exists. Enter a valid file name");
        } catch (Exception e) {

//            throw new Exception("ERROR occurred while reading input file.");
            throw e;
        }
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

    public Graph getVerificationGraph() {
        return this.verificationGraph;
    }

    public Graph getSemanticForwardGraph() {
        return this.semanticGraphForward;
    }

    public Graph getSemanticBackwardGraph() {
        return this.semanticGraphBackward;
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
                        " išvestas. Bandymų " + getCurrentIteration());

                String pathString = getSearchPathString();
                if (pathString != null) {
                    fileWriter.write("\n\t2) Kelias: " +
                            getSearchPathString() + "\n");
                } else {
                    fileWriter.write("\n\t2) Kelias tuščias.\n");
                }
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

    private void writeToDefaultFileFrame(int currentIteration, String initialText, String endText) {

        addTraversalFrameDelay(e -> writeToDefaultFile(
                String.format("%3d", currentIteration) + initialText + endText));
    }

    private void writeToDefaultFileFrame(String text) {

        addTraversalFrameDelay(e -> writeToDefaultFile(text));
    }

    /**** UPDATE UI LABELS ****/

    private void updateCurrentIterationLbl(int iteration) {

        controller.setCurrentIterationLbl(iteration);
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

        if (productionSystem.isTargetReached()) {

            this.exists = true;
            ++currentTransitionStep;
            addTraversalFrame(e -> writeToDefaultFile("\tTikslas "
                    + productionSystem.getTarget() + " tarp faktų. Kelias tuščias."));
            addTraversalFrame(e -> controller.processEndOfTraversal());
            return;
        }

        int iteration = 0;
        boolean ruleApplied;

        while (true) {

            StringBuilder sb = new StringBuilder("\t" + (iteration+1) + " ITERACIJA\n");

            ArrayList<Rule> rules = productionSystem.getRules();
            ruleApplied = false;

            int i = 0;
            for(Rule rule : rules) {

                ++i;
                sb.append("\t\t").append(rule).append(" ");

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
                        addTraversalFrame(e -> synthesizedGraph.apply(rule));
                        addTraversalFrame(e -> semanticGraphForward.apply(rule));
                        addTraversalFrame(e -> verificationGraph.apply(rule));
                        final int tempFactsCount = productionSystem.getFactsCount();
                        addTraversalFrame(e -> controller.setFactsCountLbl(tempFactsCount));
                        ++currentTransitionStep;
                        ++currentIteration;
                        final int tempTransitionStep = currentTransitionStep;
                        addTraversalFrame(e -> controller.setCurrentIterationLbl(tempTransitionStep));
                        sb.append("taikome. Pakeliame flag1. Faktai ")
                                .append(productionSystem.getFactsString());

                        ruleApplied = true;
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
            }

            if (productionSystem.isTargetReached()) {

                this.exists = true;
                sb.append("\n\t\tTikslas gautas.");
                addTraversalFrame(e -> deriveSearchPath());
                addTraversalFrame(e -> writeToDefaultFile(sb));
                addTraversalFrame(e -> controller.processEndOfTraversal());
                break;
            }
            if (!ruleApplied && !this.exists && i == rules.size()) {
                // solution does not exist

                sb.append("\n\t\tTikslas neegzistuoja.");
                addTraversalFrame(e -> writeToDefaultFile(sb));
                addTraversalFrame(e -> controller.processEndOfTraversal());
                break;
            }

            addTraversalFrame(e -> writeToDefaultFile(sb));
            ++iteration;
        }

    }

    private boolean runBackwardChaining() {

        this.addedFacts = new ArrayList<>();
        this.newFacts = new ArrayList<>();
        String target = productionSystem.getTarget();

        if (productionSystem.getFacts().contains(target)) {

            writeToDefaultFileFrame("\tTikslas A tarp faktų. Tuščias kelias.");
            addTraversalFrameDelay(e -> controller.processEndOfTraversal());
            this.exists = true;
            return true;
        }

        boolean result = runBackwardChaining(target,
                new ArrayList<String>(Collections.singleton(target)));
        if (result) {
            this.exists = true;
        }
        addTraversalFrameDelay(e -> controller.processEndOfTraversal());
        return result;
    }

    private boolean runBackwardChaining(String target, ArrayList<String> usedFactsInChain) {

        String initialText = ") " +
                "-".repeat(Math.max(0, currentDepth)) +
                "Tikslas " + target + ". ";

        if (usedFactsInChain.subList(0, usedFactsInChain.size()-1).contains(target)) { // exclude the last element from list while searching for cycle
            usedFactsInChain.remove(usedFactsInChain.size()-1);
            --currentDepth;
            ++currentIteration;
            writeToDefaultFileFrame(currentIteration, initialText, "Ciklas. Grįžtame, FAIL.");
            return false;
        }
        if (productionSystem.getFacts().contains(target)) {
            usedFactsInChain.remove(usedFactsInChain.size()-1);
            --currentDepth;
            ++currentIteration;
            writeToDefaultFileFrame(currentIteration, initialText,
                    "Faktas (duotas), nes faktai " + getFactsString() + ". Grįžtame, sėkmė.");

            return true;
        }

        ArrayList<Rule> rules = productionSystem.getRules();

        int rulesForTargetCount = 0;
        for(Rule rule : rules) {

            if (rule.getResult().equals(target)) {
                ++rulesForTargetCount;
                ++currentIteration;
                writeToDefaultFileFrame(currentIteration, initialText, "Randame " + rule
                                + ". Nauji tikslai " + rule.getFactString());

                addTraversalFrame(e -> semanticGraphBackward.apply(rule, currentDepth));

                int factBranchesSucceeded = 0;
                ArrayList<String> ruleFacts = new ArrayList<>(rule.getFacts());

                for(String fact : ruleFacts) {
                    usedFactsInChain.add(fact);
                    ++currentDepth;
                    if (runBackwardChaining(fact, usedFactsInChain)) {
                        ++factBranchesSucceeded;
                        if (factBranchesSucceeded == ruleFacts.size()) {
                            addedFacts.add(target);
                        }
                    }
                }
                if (factBranchesSucceeded == ruleFacts.size()) {

                    for(String fact : addedFacts) {
                        if (!productionSystem.getFacts().contains(target)) {

                            productionSystem.addFact(fact);
                            newFacts.add(fact);
                        }
                    }

                    addedFacts.clear();

                    usedFactsInChain.remove(usedFactsInChain.size()-1);
                    --currentDepth;
                    ++currentIteration;

                    if (productionSystem.getInitialFacts().contains(target)) {
                        writeToDefaultFileFrame(currentIteration, initialText, "Faktas (dabar gautas). Faktai "
                                + getFactsString() + ". Grįžtame, sėkmė.");
                    } else {
                        writeToDefaultFileFrame(currentIteration, initialText, "Faktas (buvo gautas). Faktai "
                                + getFactsString() + ". Grįžtame, sėkmė.");
                    }

                    this.searchPath.add(rule.getName());
                    return true;
                } else { // error occurred while deriving a rule

                    newFacts.removeAll(addedFacts);
                    productionSystem.removeFacts(addedFacts);
                    addedFacts.clear();
                }
            }
        }

        if (!productionSystem.getFacts().contains(target)) {
            String endText;
            if (rulesForTargetCount > 0) {
                endText = "Nėra daugiau taisyklių jo išvedimui. Grįžtame, FAIL.";
            } else {
                endText = "Nėra taisyklių jo išvedimui. Grįžtame, FAIL.";
            }
            ++currentIteration;
            writeToDefaultFileFrame(currentIteration, initialText, endText);
        }

        usedFactsInChain.remove(usedFactsInChain.size()-1);
        --currentDepth;
        return false;
    }

    private String getFactsString() {
        if (newFacts.size() > 0) {
            return productionSystem.getInitialFactsString() + " ir "
                    + Utils.getListString(newFacts, ", ");
        } else {
            return productionSystem.getInitialFactsString();
        }
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

    public void deriveSearchPath() {

        if (this.chainingType.equals(ChainingType.FORWARD)) {

            deriveSearchPathForward();
        } //else if (this.chainingType.equals(ChainingType.BACKWARD)) {
//
//            deriveSearchPathBackward();
//        }
    }

    public void deriveSearchPathForward() {

        this.searchPath.addAll(this.semanticGraphForward.getPathProductions());
    }

    public String getSearchPathString() {
        if (searchPath == null || searchPath.size() == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (String vertex : searchPath) {

            sb.append(vertex).append(" -> ");
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
