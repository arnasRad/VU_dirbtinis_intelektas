package com.arnasrad.labyrinth.model;

public class Coordinate {
    private int x, y;

    public Coordinate(int x, int y) {
        setCoordinates(x, y);
    }

    public Coordinate(Coordinate coordinate) {
        setCoordinates(coordinate);
    }

    public Coordinate() {
        setCoordinates(-1, -1);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setCoordinates(Coordinate coordinate) {
        setX(coordinate.getX());
        setY(coordinate.getY());
    }

    public void setCoordinates(int x, int y) {
        setX(x);
        setY(y);
    }

    public void moveByCoordinate(Coordinate coordinate) {
        setX(this.x + coordinate.getX());
        setY(this.y + coordinate.getY());
    }

    public boolean isExceedingBounds(Coordinate coordinate) {
        return x < 0 || x >= coordinate.getX() || y < 0 || y >= coordinate.getY();
    }

    public boolean isBetweenBorders(int cols, int rows) {
        return x > 0 && x < (cols - 1) && y > 0 && y < (rows - 1);
    }

    public boolean isAtBorder(int cols, int rows) {
        return x == 0 || x == (cols-1) || y == 0 || y == (rows-1);
    }

    public boolean equals(Coordinate coordinate) {
        return x == coordinate.getX() && y == coordinate.getY();
    }

    @Override
    public String toString() {
        return "[X=" + (x+1) +
                ",Y=" + (y+1) +
                ']';
    }
}
