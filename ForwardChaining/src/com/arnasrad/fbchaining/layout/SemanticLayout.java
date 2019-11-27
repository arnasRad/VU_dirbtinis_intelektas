package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.SemanticGraph;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.Rule;
import com.arnasrad.fbchaining.model.vertex.EllipseVertex;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.ArrayList;

public class SemanticLayout extends Layout {

    private static class Spacing {

        private static final double LEFT = 20; // left graph padding
        private static final double TOP = 20; // top graph padding
        private static final double BETWEEN_HOR = 70; // horizontal spacing between vertices
        private static final double BETWEEN_VER = 20; // vertical spacing between vertices
    }

    private SemanticGraph graph;
    private ArrayList<String> facts;

    public SemanticLayout(SemanticGraph graph) {

        this.graph = graph;
        this.facts = new ArrayList<>();
    }

    public void execute() {

    }

    public void relocate(Rule rule) {
        ArrayList<String> ruleFacts = rule.getFacts();
        String production = rule.getName();
        String result = rule.getResult();

        Model model = this.graph.getModel();

        Vertex factVertex = null;
        double x = Spacing.LEFT;
        double y = Spacing.TOP;
        for (String fact : ruleFacts) {

            if (this.facts.contains(fact)) { // a rule fact is already present in the graph; use it as a relative point for consequent fact vertices

                factVertex = model.getVertex(fact);
                x = factVertex.getLayoutX();
                y = factVertex.getLayoutY() + EllipseVertex.DEFAULT_RADIUSY*2 + Spacing.BETWEEN_VER;
                break;
            }
        }

        for (String fact : ruleFacts) {

            if (!this.facts.contains(fact)) {

                this.facts.add(fact);
                Vertex vertex = model.getVertex(fact);
                vertex.relocate(x, y);
                y += EllipseVertex.DEFAULT_RADIUSY*2 + Spacing.BETWEEN_VER;
            }
        }

        x += EllipseVertex.DEFAULT_RADIUSX*2 + Spacing.BETWEEN_HOR;

        int maxChildCount = getMaxChildCount(ruleFacts);
        if (factVertex != null) {
            y = factVertex.getLayoutY();
        } else {
            y = model.getVertex(ruleFacts.get(0)).getLayoutY();
        }
        y += (maxChildCount-1) * (EllipseVertex.DEFAULT_RADIUSY*2 + Spacing.BETWEEN_VER);

        model.getVertex(production).relocate(x, y);
        x += EllipseVertex.DEFAULT_RADIUSX*2 + Spacing.BETWEEN_HOR;
        this.facts.add(result);
        model.getVertex(result).relocate(x, y);


        // y spacing * child


        /*
        Vertex factVertex = model.getVertex(fact);
        if (factVertex == null) { // there is no vertex with specified fact id

            return;
        }

        if (facts.size() == 0) { // this is the first fact to be added to the graph

            this.facts.add(fact);

            double x = Spacing.LEFT;
            double y = Spacing.TOP;

            factVertex.relocate(x, y);
        } else if (facts.contains(fact)) { // this fact was previously a result of another production


        } else { // this fact is newly added to a non-empty graph


        }
        */
    }

    /**
     * DEPRECATED
     * returns a pair (min,max) as an array that corresponds to graph fact vertices min and max Y coordinates
     * @param facts graph vertices
     * @return pair {min, max} as a double array
     */
    private double[] getMinY(ArrayList<String> facts) {

        if (facts == null || facts.size() == 0) {

            return null;
        }

        double[] bounds = new double[2];
        Model model = this.graph.getModel();

        double y = model.getVertex(facts.get(0)).getLayoutY();
        bounds[0] = y;
        bounds[1] = y;
        for(int i = 1; i < facts.size(); ++i) {

            y = model.getVertex(facts.get(i)).getLayoutY();

            if (y < bounds[0]) {
                bounds[0] = y; // new min
            } else if (y > bounds[1]) {
                bounds[1] = y; // new max
            }
        }

        return bounds;
    }

    /**
     * Get a number of maximum child vertices count of given fact vertices
     * @param facts given vertices list
     * @return max child count
     */
    private int getMaxChildCount(ArrayList<String> facts) {

        Model model = this.graph.getModel();
        int maxChildCount = 0;
        for(String fact : facts) {

            Vertex vertex = model.getVertex(fact);
            int childrenCount = vertex.getVertexChildren().size();
            if (childrenCount > maxChildCount) {
                maxChildCount = childrenCount;
            }
        }

        return maxChildCount;
    }
}
