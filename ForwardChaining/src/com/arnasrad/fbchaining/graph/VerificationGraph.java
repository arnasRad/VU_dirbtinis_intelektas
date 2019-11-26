package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.Layout;
import com.arnasrad.fbchaining.layout.VerificationLayout;
import com.arnasrad.fbchaining.model.Edge;
import com.arnasrad.fbchaining.model.Model;
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

    public void apply(Rule rule) {

        try {
            String previousVertexId = getFactString();
            String newVertexId = getAppendedFactString(rule.getResult());
            this.facts.add(rule.getResult());

            beginUpdate();
            Model model = getModel();
            model.addVertex(newVertexId);
            endUpdate();

            beginUpdate();
            model.addEdge(previousVertexId,
                    newVertexId, true, rule.getName());

            layout.relocateLastVertex();
        } catch (Exception e) {
            System.err.println("ERROR: error occurred while adding a new edge. " + e.getMessage());
            e.printStackTrace();
        } finally {
            endUpdate();
        }
    }

    public void initializeLayout() {

        layout = new VerificationLayout(this);
        layout.execute();
    }

    private String getFactString() {

        return Utils.getListString(this.facts, "\n");
    }

    private String getAppendedFactString(String fact) {

        String facts = Utils.getListString(this.facts, "\n");
        return facts.concat("\n").concat(fact);
    }
}
