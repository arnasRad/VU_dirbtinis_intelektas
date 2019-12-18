package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.SemanticLayoutBackward;
import com.arnasrad.fbchaining.layout.SemanticLayoutForward;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SemanticGraphBackward extends Graph {

    private SemanticLayoutBackward layout;

    private HashMap<Integer, List<Vertex>> depthFacts; // contains used facts so it may be possible to get facts with equal ids
    private ArrayList<String> usedProductions;

    public SemanticGraphBackward(MainController controller) {

        super(controller);

        this.depthFacts = new HashMap<>();
        this.usedProductions = new ArrayList<>();

        initializeLayout();
    }

    public void reset() {

        resetModel();
        resetContainers();

        this.depthFacts = new HashMap<>();
        this.usedProductions = new ArrayList<>();

        initializeLayout();
    }

    @Override
    public void apply(Rule rule) {
        apply(rule, 0);
    }

    public void apply(Rule rule, int depth) {

        try {
            ArrayList<String> facts = rule.getFacts();
            String ruleName = rule.getName();
            String result = rule.getResult();

            Model model = getModel();

            beginUpdate();
//            for (String fact : facts) {
////                addDepthFact(depth, fact);
////            }
////
////            addDepthFact(depth+1, result);
////            model.addVertex(ruleName);
            for(String fact : facts) {
                model.addVertex(fact);
            }
            model.addVertex(result);
            model.addVertex(ruleName);
            endUpdate();

            beginUpdate();

            for(String fact : facts) {
                getModel().addEdge(fact, ruleName, true);
            }
            getModel().addEdge(ruleName, result, true);

//            Vertex ruleVertex = model.getVertex(ruleName);
//            for(Vertex fact : depthFacts.get(depth)) {
//                model.addEdge(fact, ruleVertex);
//            }
//
//            getModel().addEdge(ruleVertex, getLastDepthFact(depth+1));

        } catch (Exception e) {

            System.err.println("ERROR: error occurred while adding a new edge. " + e.getMessage());
            e.printStackTrace();
        } finally {

            endUpdate();
            layout.relocate(rule, depthFacts, depth);
        }
    }

    private void addDepthFact(int depth, String fact) {

        // add new vertex to model and to map
        if (this.depthFacts.get(depth) == null) {
            ArrayList<Vertex> tempFacts = new ArrayList<>();
            tempFacts.add(getModel().addVertex(fact));
            this.depthFacts.put(depth, tempFacts);
        } else {
            this.depthFacts.get(depth).add(getModel().addVertex(fact));
        }
    }

    private Vertex getLastDepthFact(int depth) {

        if (this.depthFacts.get(depth) == null) {
            return null;
        } else {

            List<Vertex> vertices = depthFacts.get(depth);
            return vertices.get(vertices.size()-1);
        }
    }

    public void initializeLayout() {

        layout = new SemanticLayoutBackward(this);
        layout.execute();
    }
}
