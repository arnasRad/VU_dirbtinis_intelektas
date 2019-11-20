package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.VerificationGraph;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.List;
import java.util.Random;

public class VerificationLayout extends Layout {

    private VerificationGraph graph;

    private Random rnd = new Random();

    public VerificationLayout(VerificationGraph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Vertex> vertices = graph.getModel().getAllVertices();

        for (Vertex vertex : vertices) {

            // define vertices layout

        }

    }
}
