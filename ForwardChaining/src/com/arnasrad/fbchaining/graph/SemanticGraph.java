package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.Layout;
import com.arnasrad.fbchaining.layout.SemanticLayout;
import com.arnasrad.fbchaining.model.Rule;

import java.util.ArrayList;

public class SemanticGraph extends Graph {

    private ArrayList<SGSection> sections;

    public SemanticGraph(MainController controller) {

        super(controller);
        this.sections = new ArrayList<>();
    }

    public void reset() {

        this.sections = new ArrayList<>();
    }

    public void add(SGSection.Subsection subsection, String element) {

        if (sections.size() == 0) {

            sections.add(new SGSection(subsection, element));
            return;
        }

        switch (subsection) {

            case FIRST:

                break;
            case SECOND:

                break;
            case THIRD:

                break;
        }
    }

    public void apply(Rule rule) {


    }

    public void initializeLayout() {

        Layout layout = new SemanticLayout(this);
        layout.execute();
    }
}
