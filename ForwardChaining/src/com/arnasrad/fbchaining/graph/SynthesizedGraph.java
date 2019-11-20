package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.Layout;
import com.arnasrad.fbchaining.layout.SynthesizedLayout;
import com.arnasrad.fbchaining.model.Rule;

import java.util.ArrayList;

public class SynthesizedGraph extends Graph {

    private ArrayList<String> factsPart;
    private ArrayList<String> productionsPart;
    private ArrayList<String> resultsPart;

    public SynthesizedGraph(MainController controller) {

        super(controller);

        this.factsPart = new ArrayList<>();
        this.productionsPart = new ArrayList<>();
        this.resultsPart = new ArrayList<>();
    }

    public void reset() {

        this.factsPart = new ArrayList<>();
        this.productionsPart = new ArrayList<>();
        this.resultsPart = new ArrayList<>();
    }

    private void addFact(String fact) {

        this.factsPart.add(fact);
    }

    private void addProduction(String production) {

        this.productionsPart.add(production);
    }

    private void addResult(String result) {

        this.resultsPart.add(result);
    }

    public void apply(Rule rule) {


    }

    public void initializeLayout() {

        Layout layout = new SynthesizedLayout(this);
        layout.execute();
    }
}
