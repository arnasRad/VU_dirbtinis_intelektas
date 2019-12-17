package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.SemanticLayoutForward;
import com.arnasrad.fbchaining.model.Rule;

import java.util.ArrayList;

public class SemanticGraphForward extends Graph {

    private SemanticLayoutForward layout;

    private ArrayList<String> usedFacts;
    private ArrayList<String> usedProductions;

    public SemanticGraphForward(MainController controller) {

        super(controller);

        this.usedFacts = new ArrayList<>();
        this.usedProductions = new ArrayList<>();

        initializeLayout();
    }

    public void reset() {

        resetModel();
        resetContainers();

        this.usedFacts = new ArrayList<>();
        this.usedProductions = new ArrayList<>();

        initializeLayout();
    }

    public void apply(Rule rule) {

        try {
            ArrayList<String> facts = rule.getFacts();
            String ruleName = rule.getName();
            String result = rule.getResult();

            beginUpdate();
            for (String fact : facts) {
                if (!usedFacts.contains(fact)) {

                    this.usedFacts.add(fact);
                    getModel().addVertex(fact);
                }
            }

            this.usedFacts.add(result);
            getModel().addVertex(ruleName);
            getModel().addVertex(result);
            endUpdate();

            beginUpdate();
            for(String fact : facts) {
                getModel().addEdge(fact, ruleName, true);
            }
            getModel().addEdge(ruleName, result, true);

        } catch (Exception e) {

            System.err.println("ERROR: error occurred while adding a new edge. " + e.getMessage());
            e.printStackTrace();
        } finally {

            endUpdate();
            layout.relocate(rule);
        }
    }

    public void initializeLayout() {

        layout = new SemanticLayoutForward(this);
        layout.execute();
    }
}
