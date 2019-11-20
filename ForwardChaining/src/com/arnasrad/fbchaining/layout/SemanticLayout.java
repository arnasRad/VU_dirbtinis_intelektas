package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.SemanticGraph;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.List;
import java.util.Random;

public class SemanticLayout extends Layout {

    private SemanticGraph graph;

    private Random rnd = new Random();

    public SemanticLayout(SemanticGraph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Vertex> vertices = graph.getModel().getAllVertices();

        for (Vertex vertex : vertices) {

            // define vertices layout

        }

    }
}
