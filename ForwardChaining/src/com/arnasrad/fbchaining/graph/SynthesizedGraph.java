package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.Layout;
import com.arnasrad.fbchaining.layout.SynthesizedLayout;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.utility.Utils;
import com.sun.webkit.network.Util;

import java.util.ArrayList;
import java.util.SplittableRandom;

public class SynthesizedGraph extends Graph {

    private ArrayList<String> factsPart;
    private ArrayList<String> productionsPart;
    private ArrayList<String> resultsPart;

    private Layout layout;

    public SynthesizedGraph(MainController controller) {

        super(controller);

        this.factsPart = new ArrayList<>();
        this.productionsPart = new ArrayList<>();
        this.resultsPart = new ArrayList<>();
        layout = new SynthesizedLayout(this);
    }

    public void reset() {

        this.factsPart = new ArrayList<>();
        this.productionsPart = new ArrayList<>();
        this.resultsPart = new ArrayList<>();
    }

    private void addFact(String fact) {

        this.factsPart.add(fact);
    }

    private void addFacts(ArrayList<String> facts) {

        this.factsPart.addAll(facts);
    }

    private void addProduction(String production) {

        this.productionsPart.add(production);
    }

    private void addResult(String result) {

        this.resultsPart.add(result);
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
