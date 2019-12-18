package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.graph.SemanticGraphBackward;
import com.arnasrad.fbchaining.graph.SemanticGraphForward;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.model.vertex.EllipseVertex;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SemanticLayoutBackward extends Layout {

    private static class Spacing {

        private static final double LEFT = 20; // left graph padding
        private static final double TOP = 20; // top graph padding
        private static final double BETWEEN_HOR = 70; // horizontal spacing between vertices
        private static final double BETWEEN_VER = 20; // vertical spacing between vertices
    }

    private final double horizontalOffset;
    private final double verticalOffset;

    private SemanticGraphBackward graph;
    private ArrayList<String> facts;

    // used for backward chaining
    private int currentDepth;
//    private HashMap<Integer, Vertex> depthFacts;
    private final double backwardStartX;

    public SemanticLayoutBackward(SemanticGraphBackward graph) {

        this.graph = graph;
        this.facts = new ArrayList<>();

        this.horizontalOffset = EllipseVertex.DEFAULT_RADIUSX*2 + Spacing.BETWEEN_HOR;
        this.verticalOffset = EllipseVertex.DEFAULT_RADIUSY*2 + Spacing.BETWEEN_VER;

        this.currentDepth = 0;
//        this.depthFacts = new HashMap<>();
        this.backwardStartX = MainController.OPTIONAL_GRAPH_WIDTH - (verticalOffset);
    }

    public void execute() {

    }

    public void relocate(Rule rule, HashMap<Integer, List<Vertex>> depthFacts, int depth) {

        ArrayList<String> ruleFacts = rule.getFacts();
        String production = rule.getName();
        String result = rule.getResult();

        Model model = this.graph.getModel();


    }

    /**
     * relocates vertices in a branch vertically downwards
     */
    private void moveBranchDown(HashMap<Integer, List<Vertex>> depthFacts, int depth) {

//        while ()
    }
}
