package com.arnasrad.fbchaining.graph;

import java.util.ArrayList;

// semantic graph section
public class SGSection {

    public enum Subsection {

        FIRST,
        SECOND,
        THIRD
    }

    private ArrayList<String> firstSubsection;
    private ArrayList<String> secondSubsection;
    private ArrayList<String> thirdSubsection;

    public SGSection() {

        firstSubsection = new ArrayList<>();
        secondSubsection = new ArrayList<>();
        thirdSubsection = new ArrayList<>();
    }

    public SGSection(Subsection subsection, String element) {

        this();
        add(subsection, element);
    }

    public void add(Subsection subsection, String element) {

        switch (subsection) {
            case FIRST:
                this.firstSubsection.add(element);
            case SECOND:
                this.secondSubsection.add(element);
            case THIRD:
                this.thirdSubsection.add(element);
        }
    }

    public void remove(Subsection subsection, String element) {

        switch (subsection) {
            case FIRST:
                this.firstSubsection.remove(element);
            case SECOND:
                this.secondSubsection.remove(element);
            case THIRD:
                this.thirdSubsection.remove(element);
        }
    }

    public boolean contains(Subsection subsection, String element) {

        switch (subsection) {
            case FIRST:
                return firstSubsection.contains(element);
            case SECOND:
                return secondSubsection.contains(element);
            case THIRD:
                return thirdSubsection.contains(element);
            default:
                return false;
        }
    }
}
