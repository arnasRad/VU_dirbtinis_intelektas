package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.Layout;
import com.arnasrad.fbchaining.layout.SemanticLayout;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.utility.Utils;

import java.util.ArrayList;

public class SemanticGraph extends Graph {

    private SemanticLayout layout;

    private ArrayList<String> usedFacts;
    private ArrayList<String> usedProductions;

    public SemanticGraph(MainController controller) {

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
        }
    }

    public void initializeLayout() {

        layout = new SemanticLayout(this);
        layout.execute();
    }
}
