package com.arnasrad.fbchaining.layout;

import com.arnasrad.fbchaining.graph.Graph;
import com.arnasrad.fbchaining.model.vertex.Vertex;

import java.util.List;
import java.util.Random;

public class RandomLayout extends Layout {

    private Graph graph;

    private Random rnd = new Random();

    public RandomLayout(Graph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Vertex> vertices = graph.getModel().getAllVertices();

        for (Vertex vertex : vertices) {

            double x = rnd.nextDouble() * 500;
            double y = rnd.nextDouble() * 500;

            vertex.relocate(x, y);

        }

    }

}