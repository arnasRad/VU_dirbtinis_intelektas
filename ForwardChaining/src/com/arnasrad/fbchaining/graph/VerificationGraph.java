package com.arnasrad.fbchaining.graph;

import com.arnasrad.fbchaining.MainController;
import com.arnasrad.fbchaining.layout.Layout;
import com.arnasrad.fbchaining.layout.VerificationLayout;
import com.arnasrad.fbchaining.model.Rule;

public class VerificationGraph extends Graph {

    public VerificationGraph(MainController controller) {

        super(controller);
    }

    public void reset() {

    }

    public void apply(Rule rule) {


    }

    public void initializeLayout() {

        Layout layout = new VerificationLayout(this);
        layout.execute();
    }
}
