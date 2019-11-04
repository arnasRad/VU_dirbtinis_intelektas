package com.arnasrad.labyrinth.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Button;

public class Tile extends Button {

    public static final class States {
        public static final byte IDLE = 0;
        public static final byte CURRENT = 1;
        public static final byte VISITED = 2;
        public static final byte WALL = 3;
        public static final byte THREAD = 4;
    }

    public static final int STARTING_VALUE = 2;

//    private int x, y = 0;
    private Coordinate coordinate;
    private int value = 0; // tile step index; specifies
    private int tileState;
    private boolean isPath;   // type of tile; true - path (traveler can walk on it); false - wall (cannot pass)

    public Tile(int x, int y, boolean isPath) {

        this(isPath);
        this.coordinate = new Coordinate(x, y);
    }

    public Tile(Coordinate coordinate, boolean isPath) {

        this(isPath);
        this.coordinate = new Coordinate(coordinate);
    }

    private Tile(boolean isPath) {

        this.isPath = isPath;

        setPrefHeight(2000);
        setPrefWidth(2000);
        setResizableText();

        if (isPath) {
            setValue(0);
            setDisable(false);
            getStyleClass().add("tile-btn-idle");
        } else {
            setValue(1);
            setDisable(true);
            getStyleClass().add("tile-btn-disabled");
        }
    }

    public int getX() {
        return coordinate.getX();
    }

    public int getY() {
        return coordinate.getY();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        setText(String.valueOf(value));
    }

    public boolean isPath() {
        return isPath;
    }

    public int getTileState() {
        return tileState;
    }

    private void setTileState(int tileState) {
        this.tileState = tileState;

        getStyleClass().clear();

        getStyleClass().add("button");
        switch (tileState) {
            case States.IDLE:
                getStyleClass().add("tile-btn-idle");
                break;
            case States.CURRENT:
                getStyleClass().add("tile-btn-active");
                getStyleClass().add("tile-btn-current");
                break;
            case States.VISITED:
                getStyleClass().add("tile-btn-active");
                getStyleClass().add("tile-btn-visited");
                break;
            case States.THREAD:
                getStyleClass().add("tile-btn-active");
                getStyleClass().add("tile-btn-thread");
                break;
        }
    }

    public void setIdle() {
        setTileState(States.IDLE);
        setValue(0);
    }

    public void setCurrent(int value) {
        setTileState(States.CURRENT);
        setValue(value);
    }

    public void setCurrent() {
        setTileState(States.CURRENT);
    }

    public void setVisited() {
        setTileState(States.VISITED);
    }

    public void setDisabled() {
        setTileState(States.WALL);
    }

    public void setThread() {
        setTileState(States.THREAD);
        setValue(-1);
    }

    private void setResizableText() {
        DoubleProperty fontSize = new SimpleDoubleProperty(10);
//        Scene scene = boardVBox.getParent().getScene();
        fontSize.bind(widthProperty().add(heightProperty()).divide(8));
        styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
    }
}
