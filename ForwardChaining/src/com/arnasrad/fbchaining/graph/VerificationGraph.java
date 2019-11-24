package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.Layout;
import com.arnasrad.fbchaining.layout.VerificationLayout;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.utility.Utils;

import java.util.ArrayList;

public class VerificationGraph extends Graph {

    private ArrayList<String> initialFacts; // used for resetting graph
    private ArrayList<String> facts;

    private VerificationLayout layout;

    public VerificationGraph(MainController controller, ArrayList<String> initialFacts) {

        super(controller);

        this.initialFacts = initialFacts;
        this.facts = new ArrayList<>(initialFacts);

        beginUpdate();
        getModel().addVertex(getFactString());
        endUpdate();

        initializeLayout();
    }

    public void reset() {

        this.facts = new ArrayList<>(initialFacts);

        resetContainers();
        getModel().clear();
        beginUpdate();
        getModel().addVertex(getFactString());
        endUpdate();

        initializeLayout();

    }

    private void addFact(String fact) {

        this.facts.add(fact);
        layout.relocateLastVertex();
    }

    public void apply(Rule rule) {

        addFact(rule.getResult());
    }

    public void initializeLayout() {

        layout = new VerificationLayout(this);
        layout.execute();
    }

    private String getFactString() {

        return Utils.getListString(this.facts, "\n");
    }
}
