package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.SemanticGraph;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.List;
import java.util.Random;

public class SemanticLayout extends Layout {

    private static class Spacing {

        private static final int LEFT = 20; // left graph padding
        private static final int TOP = 20; // top graph padding
        private static final int BETWEEN_HOR = 100; // horizontal spacing between vertices
        private static final int BETWEEN_VER = 30; // vertical spacing between vertices
    }

    private SemanticGraph graph;

    private int currentLayer;
    private int currentSection;

    public SemanticLayout(SemanticGraph graph) {

        this.graph = graph;
        currentLayer = 0;
        currentSection = 0;

    }

    public void execute() {

        List<Vertex> vertices = graph.getModel().getAllVertices();

        for (Vertex vertex : vertices) {

            // define vertices layout

        }

    }
}
