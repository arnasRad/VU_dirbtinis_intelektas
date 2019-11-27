package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.VerificationGraph;
import com.arnasrad.fbchaining.model.Model;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VerificationLayout extends Layout {

    private static class Spacing {

        private static final double LEFT = 20; // left graph padding
        private static final double TOP = 20; // top graph padding
        private static final double BETWEEN_HOR = 150; // horizontal spacing between vertices
    }

    private VerificationGraph graph;

    public VerificationLayout(VerificationGraph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Vertex> vertices = graph.getModel().getAllVertices();

        Vertex vertex = vertices.get(0);

        double x = Spacing.LEFT;
        double y = Spacing.TOP;

        vertex.relocate(x, y);

    }

    public void relocateLastVertex() {

        Model model = graph.getModel();
        List<Vertex> vertices = model.getAllVertices();
        int verticesSize = vertices.size();
        Vertex lastVertex = vertices.get(verticesSize-1);

        double x = Spacing.LEFT + Spacing.BETWEEN_HOR * (verticesSize-1);
        double y = Spacing.TOP;

        lastVertex.relocate(x, y);
    }
}
