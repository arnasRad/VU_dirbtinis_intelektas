package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.SynthesizedGraph;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.vertex.EllipseVertex;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.ArrayList;
import java.util.Random;

public class SynthesizedLayout extends Layout {

    private static class Spacing {

        private static final double LEFT = 20; // left graph padding
        private static final double TOP = 20; // top graph padding
        private static final double BETWEEN_HOR = 100; // horizontal spacing between vertices
        private static final double BETWEEN_VER = 10; // vertical spacing between vertices
    }

    private SynthesizedGraph graph;

    private Random rnd = new Random();

    public SynthesizedLayout(SynthesizedGraph graph) {

        this.graph = graph;

    }

    public void execute() {

        Vertex productionsVertex = graph.getModel().getFirstVertexByLabel(SynthesizedGraph.PRODUCTIONS_ID);

        // x = left_margin + default_vertex_width + between_hor
        double x = Spacing.LEFT + EllipseVertex.DEFAULT_RADIUSX + Spacing.BETWEEN_HOR;
        double y = Spacing.TOP;
        productionsVertex.relocate(x, y);

    }

    public void relocateFact(int index, String fact) {

        Vertex factVertex = graph.getModel().getFirstVertexByLabel(fact);

        double x = Spacing.LEFT;
        double y = Spacing.TOP + (EllipseVertex.DEFAULT_RADIUSY *2 + Spacing.BETWEEN_VER) * index;
        factVertex.relocate(x, y);
    }

    public void relocateFacts(int startIndex, ArrayList<String> facts) {


        for(String fact : facts) {
            Vertex factVertex = graph.getModel().getFirstVertexByLabel(fact);

            double x = Spacing.LEFT;
            double y = Spacing.TOP + (EllipseVertex.DEFAULT_RADIUSY * 2 + Spacing.BETWEEN_VER) * startIndex;
            factVertex.relocate(x, y);
            ++startIndex;
        }
    }

    public void relocateResult(int index, String result) {

        Model model = graph.getModel();
        Vertex resultVertex = model.getFirstVertexByLabel(result);
        Vertex productionsVertex = model.getFirstVertexByLabel(SynthesizedGraph.PRODUCTIONS_ID);

        double x = Spacing.LEFT + EllipseVertex.DEFAULT_RADIUSX +
                Spacing.BETWEEN_HOR + productionsVertex.getWidth() +
                Spacing.BETWEEN_HOR;

        double y = Spacing.TOP + (EllipseVertex.DEFAULT_RADIUSY *2 + Spacing.BETWEEN_VER) * index;
        resultVertex.relocate(x, y);
    }

    public void appendProdVertexTxt(String production, ArrayList<String> resultPart) {

        Model model = graph.getModel();
        EllipseVertex productionsVertex = (EllipseVertex) model.getFirstVertexByLabel(SynthesizedGraph.PRODUCTIONS_ID);

        double widthBefore = productionsVertex.getWidth();

        productionsVertex.appendLabelTxt(production);

        double widthAfter = productionsVertex.getWidth();

        if (widthAfter > widthBefore) {

            double x = Spacing.LEFT + EllipseVertex.DEFAULT_RADIUSX +
                    Spacing.BETWEEN_HOR + widthAfter + Spacing.BETWEEN_HOR;

            for (String result : resultPart) {

                Vertex resultVertex = model.getFirstVertexByLabel(result);
                double y = resultVertex.getLayoutY();

                resultVertex.relocate(x, y);
            }
        }
    }

    public void triggerOnChanged(String vertexId) {

        Vertex vertex = graph.getModel().getFirstVertexByLabel(vertexId);
        vertex.relocate(vertex.getLayoutX(), vertex.getLayoutY()+1);
    }
}
