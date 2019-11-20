package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.SynthesizedGraph;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.List;
import java.util.Random;

public class SynthesizedLayout extends Layout {

    private SynthesizedGraph graph;

    private Random rnd = new Random();

    public SynthesizedLayout(SynthesizedGraph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Vertex> vertices = graph.getModel().getAllVertices();

        for (Vertex vertex : vertices) {

            // define vertices layout

        }

    }
}
