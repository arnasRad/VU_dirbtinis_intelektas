package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.SynthesizedLayout;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.utility.Utils;

import java.util.ArrayList;

public class SynthesizedGraph extends Graph {

    public final static String PRODUCTIONS_ID = "productions";
    private ArrayList<String> factsPart;
    private ArrayList<String> productionsPart;
    private ArrayList<String> resultsPart;

    private SynthesizedLayout layout;

    public SynthesizedGraph(MainController controller) {

        super(controller);

        this.factsPart = new ArrayList<>();
        this.productionsPart = new ArrayList<>();
        this.resultsPart = new ArrayList<>();

        beginUpdate();
        getModel().addVertex(PRODUCTIONS_ID);
        endUpdate();

        initializeLayout();
    }

    public void reset() {

        this.factsPart = new ArrayList<>();
        this.productionsPart = new ArrayList<>();
        this.resultsPart = new ArrayList<>();
    }

    private void addFact(String fact) {

        try {
            this.factsPart.add(fact);

            beginUpdate();

            Model model = getModel();
            model.addVertex(fact);
            model.addEdge(fact, PRODUCTIONS_ID);

            layout.relocateFact(this.factsPart.size()-1, fact);

        } catch (Exception e) {
            System.err.println("ERROR: error occurred while adding a new edge. " + e.getMessage());
            e.printStackTrace();
        } finally {
            endUpdate();
        }
    }

    private void addFacts(ArrayList<String> facts) {

        try {

            beginUpdate();
            for (String fact : facts) {

                this.factsPart.add(fact);

                Model model = getModel();
                model.addVertex(fact);
                model.addEdge(fact, PRODUCTIONS_ID);

                layout.relocateFact(this.factsPart.size()-1, fact);
            }

        } catch (Exception e) {
            System.err.println("ERROR: error occurred while adding a new edge. " + e.getMessage());
            e.printStackTrace();
        } finally {
            endUpdate();
        }
    }

    private void addProduction(String production) {

        this.productionsPart.add(production);
        layout.appendProdVertexTxt(production, resultsPart);
    }

    private void addResult(String result) {

        try {
            this.resultsPart.add(result);

            beginUpdate();

            Model model = getModel();
            model.addVertex(result);
            model.addEdge(PRODUCTIONS_ID, result);

            layout.relocateResult(this.resultsPart.size()-1, result);

        } catch (Exception e) {
            System.err.println("ERROR: error occurred while adding a new edge. " + e.getMessage());
            e.printStackTrace();
        } finally {
            endUpdate();
        }
    }

    public void apply(Rule rule) {

        ArrayList<String> factsDifference = Utils.getListsDifference(
                rule.getFacts(), this.factsPart);
        String production = rule.getName();
        String result = rule.getResult();

        addFacts(factsDifference);
        addProduction(production);
        if (!this.resultsPart.contains(result)) {
            addResult(result);
        }


    }

    public void initializeLayout() {

        layout = new SynthesizedLayout(this);
        layout.execute();
    }
}