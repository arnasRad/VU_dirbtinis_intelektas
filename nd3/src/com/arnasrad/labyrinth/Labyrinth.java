package com.arnasrad.labyrinth;

import com.arnasrad.labyrinth.model.Tile;
import com.arnasrad.labyrinth.model.Coordinate;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;

public class Labyrinth implements Runnable {

    public static final class TraverseOption {
        public static final byte BACKTRACK = 0;
        public static final byte BACKTRACK1 = 1;
        public static final byte BACKTRACK_MEMORY = 2;
        public static final byte WAVE_ALGORITHM = 3;
    }

    //    private static final int[] CX = new int[]{-1, 0, 1, 0};  // step rule x coordinate
//    private static final int[] CY = new int[]{0, -1, 0, 1};  // step rule y coordinate
    private static final Coordinate[] rules = new Coordinate[] {
            new Coordinate(-1, 0),
            new Coordinate(0, -1),
            new Coordinate(1, 0),
            new Coordinate(0, 1)
    };

    private Controller controller;

    private boolean exists; // specifies whether a path out of labyrinth exists
    private int rows, cols; // labyrinth dimensions (rows,cols) = (m,n)
    //    private int startingX, startingY, finishX, finishY; // starting coordinates
    private Coordinate startingCoordinate, finishCoordinate; // starting coordinates
    private int[][] backupL; // backup labyrinth array for resetting the board
    private int[][] labyrinth; // matrix used for result path calculations
    private Tile[][] L; // UI labyrinth array

    private static int CURRENT_ITERATION;
    private boolean stackOverflowFlag;

    private ArrayList<Integer> prodPath; // a list of rules used to traverse labyrinth
    private ArrayList<Coordinate> traversalPath; // a list of coordinates specifying a path from starting coordinate to the exit

    private ArrayList<KeyFrame> traversal;
    private Timeline traversalTimeline;
    private int millisecondDelay;
    private int currentTransitionStep;

    /********* CONSTRUCTOR *********/

    public Labyrinth(Controller controller, String fileName) throws Exception {

        this.controller = controller;
        resetInfoFields();
        loadLabyrinth(fileName);
    }

    /********* RUN TRAVERSAL THREAD *********/

    @Override
    public void run() {
        int traversalOption = getTraversalOption();
        controller.initializeFileWriter();

        millisecondDelay = 0;
        currentTransitionStep = 0;

        addTraversalFrame(e -> showDataInfo());
        if (traversalOption == TraverseOption.WAVE_ALGORITHM) {
            runWave();
        } else if (traversalOption == TraverseOption.BACKTRACK ||
                traversalOption == TraverseOption.BACKTRACK1 ||
                traversalOption == TraverseOption.BACKTRACK_MEMORY) {

            int backtrackOption = getTraversalOption();

            // unexpected error
            if (backtrackOption == TraverseOption.WAVE_ALGORITHM ||
                    backtrackOption == -1) {

                return;
            }

            runBacktrack(startingCoordinate, Tile.STARTING_VALUE, backtrackOption);
        }

        traversalTimeline = new Timeline();
        for (KeyFrame key : traversal) {
            traversalTimeline.getKeyFrames().add(key);
        }
        Platform.runLater(traversalTimeline::play);
    }

    /********* RESET/RESTART LABYRINTH *********/

    public void restart() {
        resetInfoFields();
        this.rows = this.cols = 0;
        this.backupL = null;
        this.labyrinth = null;
        this.L = null;
    }

    public void reset() {
        resetInfoFields();

        this.L = new Tile[this.rows][this.cols];
        this.labyrinth = new int[this.rows][this.cols];
        for (int i = 0; i < this.rows; ++i) {
            for (int j = 0; j < this.cols; ++j) {
                int num = getBackupLValue(j, i);
                labyrinth[i][j] = num;
                if (num == 0) {
                    createTile(j, i, true);
                } else if (num == 1) {
                    createTile(j, i, false);
                }
            }
        }
    }

    /********* GETTERS/SETTERS *********/
    public void stopTraversalAnimation() {

        if (traversalTimeline != null) {
            traversalTimeline.stop();
        }
    }

    /********* GETTERS/SETTERS *********/

    public boolean exitExists() {
        return this.exists;
    }

    private void resetInfoFields() {
        stopTraversalAnimation();
        this.exists = false;
        this.stackOverflowFlag = false;
        setStartingCoordinate(0, 0);
        setFinishCoordinates(0, 0);
        setNewPathArrays();
        CURRENT_ITERATION = 0;
    }

    private void setNewPathArrays() {
        prodPath = new ArrayList<>();
        traversalPath = new ArrayList<>();
        traversal = new ArrayList<>();
    }

    public void setStartingCoordinate(int x, int y) {

        this.startingCoordinate = new Coordinate(x, y);
//        this.startingX = x;
//        this.startingY = y;
    }

    public Coordinate getStartingCoordinate() {

        return this.startingCoordinate;
//        this.startingX = x;
//        this.startingY = y;
    }

    private void setFinishCoordinates(int x, int y) {

        this.finishCoordinate = new Coordinate(x, y);
//        this.finishX = x;
//        this.finishY = y;
    }

    private void setFinishCoordinates(Coordinate coordinate) {
        this.finishCoordinate.setCoordinates(coordinate);
    }

    private Coordinate getFinishCoordinate() {
        return this.finishCoordinate;
    }

    private void setBackupLValue(int col, int row, int value) {
        this.backupL[row][col] = value;
    }

    private void setLabyrinthValue(int col, int row, int value) {
        this.labyrinth[row][col] = value;
    }

    private int getLabyrinthValueAtCoordinate(Coordinate coordinate) {
        return this.labyrinth[coordinate.getY()][coordinate.getX()];
    }

    private void setLabyrinthValueAtCoordinate(Coordinate coordinate, int value) {
        setLabyrinthValue(coordinate.getX(), coordinate.getY(), value);
//        this.labyrinth[coordinate.getY()][coordinate.getX()] = value;
    }

    private int getBackupLValue(int col, int row) {
        return this.backupL[row][col];
    }

    private int getBackupLValueAtCoordinate(Coordinate coordinate) {
        return this.backupL[coordinate.getY()][coordinate.getX()];
    }

    private void setBackupLValueAtCoordinate(Coordinate coordinate, int value) {
        setBackupLValue(coordinate.getX(), coordinate.getY(), value);
//        this.backupL[coordinate.getY()][coordinate.getX()] = value;
    }

    public Tile getStartingTile() {
        return getTileAtCoordinate(startingCoordinate);
//        return this.L[startingCoordinate.getY()][startingCoordinate.getX()];
//        return this.L[startingY][startingX];
    }

    private Tile getTileAtCoordinate(Coordinate coordinate) {
        return this.L[coordinate.getY()][coordinate.getX()];
    }

    private void setTileAtCoordinate(Coordinate coordinate, Tile tile) {
        this.L[coordinate.getY()][coordinate.getX()] = tile;
    }

    private void setTileValueAtCoordinate(Coordinate coordinate, int value) {
        getTileAtCoordinate(coordinate).setValue(value);
    }

    public int getStartingX() {
        return startingCoordinate.getX();
//        return startingX;
    }

    public int getStartingY() {
        return startingCoordinate.getY();
//        return startingY;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getCurrentIteration() {
        return CURRENT_ITERATION;
    }

    private void createTile(int col, int row, boolean isPath) {
        Tile tile = new Tile(col, row, isPath);

        tile.setOnMouseClicked(e -> processTileClicked(e, tile));

        this.L[row][col] = tile;

        // m-row-1 because bottom left corner in input is (x,y)=(0,0),
        // whilst in grid pane (x,y)=(0,0) is top left corner
        controller.getBoardGridPane().add(tile, col, this.rows - row - 1);
    }

    private void setVisited(Coordinate coordinate) {

//        L[y][x].setVisited();
        getTileAtCoordinate(coordinate).setVisited();
    }

    private void setCurrent(Coordinate coordinate, int l) {

        getTileAtCoordinate(coordinate).setCurrent(l);
        controller.setCurrentCoordinateLbl(coordinate);
    }

    private void setCurrent(Coordinate coordinate) {

        getTileAtCoordinate(coordinate).setCurrent();
    }

    private void setThread(Coordinate coordinate) {

        getTileAtCoordinate(coordinate).setThread();
    }

    private void setIdle(Coordinate coordinate) {

        getTileAtCoordinate(coordinate).setIdle();
    }

    private byte getTraversalOption() {
        String traversalOption = controller.getTraversalOption();

        switch (traversalOption) {
            case "menuBacktrack":
                return TraverseOption.BACKTRACK;
            case "menuBacktrack1":
                return TraverseOption.BACKTRACK1;
            case "menuBacktrackMem":
                return TraverseOption.BACKTRACK_MEMORY;
            case "menuWave":
                return TraverseOption.WAVE_ALGORITHM;
            default:
                return -1; // unexpected error
        }
    }

    /********* LOAD LABYRINTH *********/

    private void loadLabyrinth(String fileName) throws Exception {

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(new File(fileName)));

            String line = br.readLine();
            String[] numbers = line.split(" ");

            if (numbers.length != 2) {
                throw new Exception("> ERROR: first line in input file must contain " +
                        "two numbers - labyrinth dimensions separated by whitespace: y x");
            }

            // two first numbers of input file must be labyrinth dimensions
            this.rows = Integer.parseInt(numbers[1]); // m is for y's (rows)
            this.cols = Integer.parseInt(numbers[0]);  // n is for x's (columns)
            this.backupL = new int[rows][cols];
            this.labyrinth = new int[rows][cols];
            this.L = new Tile[rows][cols];

            controller.setLabyrinthSizeLbl(new Coordinate(cols, rows));

            int i = rows - 1;
            int j;
            int num;
            // read the rest of the input
            while ((line = br.readLine()) != null) {
                numbers = line.split(" ");

                if (numbers.length != cols) {
                    throw new Exception("> ERROR: incorrect count of numbers in input file line " + i);
                }

                j = 0;
                for (String input : numbers) {
                    num = Integer.parseInt(input);
                    if (num == 0) {
                        setBackupLValue(j, i, 0);
                        setLabyrinthValue(j, i, 0);
                        createTile(j, i, true);
                    } else if (num == 1) {
                        setBackupLValue(j, i, 1);
                        setLabyrinthValue(j, i, 1);
                        createTile(j, i, false);
                    } else {
                        throw new Exception("> ERROR: incorrect value in input file at tile (" + i + "," + j + ")");
                    }
                    ++j;
                }
                --i;
            }
        } catch (Exception e) {
            throw new Exception("> ERROR: No such file exists. Enter a valid file name");
        }
    }

    private void processTileClicked(MouseEvent e, Tile tile) {
        int currentRunState = controller.getCurrentRunState();

        if (currentRunState == Controller.State.COORDINATES) {
            processStartingCoordinates(tile);
        }
    }

    private void processStartingCoordinates(Tile tile) {
        setStartingCoordinate(tile.getX(), tile.getY());
//        this.startingX = tile.getX();
//        this.startingY = tile.getY();

        setLabyrinthValueAtCoordinate(startingCoordinate, Tile.STARTING_VALUE);
//        this.labyrinth[startingCoordinate.getY()][startingCoordinate.getX()]
//                = Tile.STARTING_VALUE;

        controller.setCurrentCoordinateLbl(startingCoordinate);
        controller.changeToTraversalState();
    }

    /********* PRINT BOARD TO FILE WRITER *********/


    private void showDataInfo() {

        try {
            FileWriter fileWriter = controller.getFileWriter();

            fileWriter.write("1 DALIS. Duomenys\n");
            fileWriter.write("\t1.1. Lenta " + cols +
                    "x" + rows + "\n\n");
            printBoard();
            fileWriter.write("\n\t1.2. Pradinė keliautojo padėtis " +
                    startingCoordinate + ". L=" + Tile.STARTING_VALUE + "\n\n");
            fileWriter.write("\t1.3. Naudojama prodedūra: ");
            fileWriter.write(controller.getTraversalOption() + "\n\n");
            fileWriter.write("2 DALIS. Vykdymas\n\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printResults() {
        try {
            FileWriter fileWriter = controller.getFileWriter();

            fileWriter.write("\n3 DALIS. Rezultatai\n");
            if (exitExists()) {

                fileWriter.write("\t1) Kelias rastas. Bandymų " +
                        getCurrentIteration() + "\n");

                fileWriter.write("\t2) Kelias grafiškai\n");

                printBoard();

                fileWriter.write("\n3.3. Kelias taisyklėmis: " +
                        getProdPath() + "\n");

                fileWriter.write("\n3.4. Kelias viršūnėmis: " +
                        getTraversalPath() + "\n");
            } else {
                fileWriter.write("\t1) Kelias neegzistuoja\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printBoard() {
        FileWriter fileWriter = controller.getFileWriter();
        try {
            fileWriter.write("Y,\tV   ^\n");
            for (int i = this.rows - 1; i >= 0; --i) {
                fileWriter.write(String.format("\t%3d |", (i + 1)));
                for (int j = 0; j < this.cols; ++j) {
                    fileWriter.write(String.format(" %2d", labyrinth[i][j]));
                }
                fileWriter.write("\n");
            }
            printBoardBottom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printBoardBottom() {
        FileWriter fileWriter = controller.getFileWriter();

        try {
            fileWriter.write("\t    -");
            for (int i = 0; i < this.cols; ++i) {
                fileWriter.write("---");
            }
            fileWriter.write("> X, U\n");
            fileWriter.write("\t     ");
            for (int i = 0; i < this.cols; ++i) {
                fileWriter.write(String.format(" %2d", (i + 1)));
            }
            fileWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /********* DELAY (SLEEP) MAIN APPLICATION THREAD *********/
    private void delayTransition() {
        try {
            Thread.sleep(controller.getTraversalSpeed());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void delayTransition(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /********* WRITE TO FILE *********/

    private void writeToDefaultFile(StringBuilder sb) {

        FileWriter fileWriter = controller.getFileWriter();
        try {
            fileWriter.write(sb.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToDefaultFile(String str) {

        FileWriter fileWriter = controller.getFileWriter();
        try {
            fileWriter.write(str + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCurrentIterationLbl(String iteration) {

        controller.setCurrentIterationLbl(iteration);
    }

    // TODO: fix error handling: incorrect file, stack overflow, etc.
    // TODO: fix BACKTRACK traversal
    // TODO: fix current coordinate updates

    private void addTraversalFrame(EventHandler<ActionEvent> eventHandler) {

        traversal.add(new KeyFrame(
                Duration.millis(controller.getTraversalSpeed() * currentTransitionStep
                        + millisecondDelay), eventHandler));

    }

    private void addTraversalFrameDelay(EventHandler<ActionEvent> eventHandler) {

        traversal.add(new KeyFrame(
                Duration.millis(controller.getTraversalSpeed() * currentTransitionStep
                        + millisecondDelay), eventHandler));
        millisecondDelay += 5; // used for correct step ordering
    }


    /********* ALGORITHMS *********/

    private synchronized void runBacktrack(Coordinate coordinate, int l, int backtrackOption) {
        try {

            if (stackOverflowFlag) {
                return;
            }

            // exit found!
            if (exitFound(coordinate)) {

//                setFinishCoordinates(x, y);
                setFinishCoordinates(coordinate);
                addTraversalFrame(e -> controller.processEndOfTraversal());
                return;
            }

            int k = 0;
//            int u, v;
            while (!exists && k < 4) {
                ++CURRENT_ITERATION;

                Coordinate currentCoordinate = new Coordinate(coordinate);
                currentCoordinate.moveByCoordinate(rules[k]);
//                u = x + CX[k];
//                v = y + CY[k];

                StringBuilder sb = new StringBuilder();

                sb.append("\t").append(CURRENT_ITERATION).append(") ");
                sb.append("-".repeat(Math.max(0, l - 1)));
                sb.append("R").append(k + 1).append(". ").append(currentCoordinate)
                        .append(". L=").append(l).append(". ");

                if (getLabyrinthValueAtCoordinate(currentCoordinate) == 0) {
                    ++currentTransitionStep;

                    final int tempIterationStep1 = currentTransitionStep;
                    addTraversalFrame(e -> updateCurrentIterationLbl(
                            String.valueOf(tempIterationStep1)));

                    sb.append("Laisva. LENTA ").append(currentCoordinate)
                            .append(":=").append(l + 1).append(".");

                    addTraversalFrame(e -> writeToDefaultFile(sb));

                    ++l;
                    if (backtrackOption == TraverseOption.BACKTRACK1 ||
                            backtrackOption == TraverseOption.BACKTRACK_MEMORY) {

                        setLabyrinthValueAtCoordinate(currentCoordinate, l);
//                        labyrinth[v][u] = l;

                        // update UI on main Java Application Thread
//                        addTraversalFrame(e -> setVisited(x, y));
                        addTraversalFrame(e -> setVisited(coordinate));
                        final int tempL = l;
                        addTraversalFrame(e -> setCurrent(currentCoordinate, tempL));
                    } else {

                        // labyrinth matrix values do not change
                        addTraversalFrame(e -> setVisited(coordinate));
                        addTraversalFrame(e -> setCurrent(currentCoordinate));
                    }

                    prodPath.add(k);
                    traversalPath.add(new Coordinate(coordinate.getX() + 1,
                            coordinate.getY() + 1));

                    runBacktrack(currentCoordinate, l, backtrackOption);
                    if (!exists) {

                        ++currentTransitionStep;

                        final int tempIterationStep2 = currentTransitionStep;
                        addTraversalFrame(e -> updateCurrentIterationLbl(
                                String.valueOf(tempIterationStep2)));

                        if (backtrackOption == TraverseOption.BACKTRACK_MEMORY) {

                            setLabyrinthValueAtCoordinate(currentCoordinate, -1);
//                            labyrinth[v][u] = -1;
                            addTraversalFrame(e -> setThread(currentCoordinate));
                        } else if (backtrackOption == TraverseOption.BACKTRACK1 ||
                                backtrackOption == TraverseOption.BACKTRACK) {

                            setLabyrinthValueAtCoordinate(currentCoordinate, 0);
//                            labyrinth[v][u] = 0;
//                            final int tempU = u; final int tempV = v;
                            addTraversalFrame(e -> setIdle(currentCoordinate));
                        }

                        --l;
                        prodPath.remove(prodPath.size() - 1);
                        traversalPath.remove(traversalPath.size() - 1);
                    }
                } else if (getLabyrinthValueAtCoordinate(currentCoordinate) == 1) {
                    sb.append("Siena.");
                    addTraversalFrame(e -> writeToDefaultFile(sb));
                } else {
                    sb.append("Siūlas.");
                    addTraversalFrame(e -> writeToDefaultFile(sb));
                }

                ++k;
            }
            if (!exists) {
                final int tempL = l;
                addTraversalFrame(e -> writeToDefaultFile("\t\t" + "-".repeat(Math.max(0, tempL - 1)) +
                        "Backtrack iš " + coordinate + ", L=" + tempL + ". LAB" + coordinate +
                        ":=" + (-1) + ". L:=" + (tempL - 1)));
            }
        } catch (StackOverflowError e) {
            System.err.println("ERROR: Stack overflow!");
            stackOverflowFlag = true;
        }
    }

    public void runWave() {

        // exit found!
        if (exitFound(getStartingCoordinate())) {
            setFinishCoordinates(getStartingCoordinate());
            getPath();
            addTraversalFrame(e -> controller.processEndOfTraversal());
            return;
        }

//        int[] FX = new int[getRows()*getCols()];
//        int[] FY = new int[getRows()*getCols()];
//
//        FX[0] = startingX;
//        FY[0] = startingY;
        Coordinate[] closingCoordinates = new Coordinate[getRows()*getCols()];
        for(int i = 0 ; i < closingCoordinates.length; ++i) {
            closingCoordinates[i] = new Coordinate();
        }
        closingCoordinates[0].setCoordinates(getStartingCoordinate());

        int uzd = 0; // skaitliukas uždaromai viršūnei
        int nauja = 0; // skaitliukas atidaromai viršūnei
//        int k, u, v, x, y;
        int k;

        addTraversalFrame(e -> writeToDefaultFile("BANGA 0, žymė L=" + Tile.STARTING_VALUE +
                ". Pradinė padėtis " + getStartingCoordinate() + ", NAUJA=1"));

        if (startingCoordinate.isBetweenBorders(getCols(), getRows())) {

            int currentWaveTiles = 1;
            int nextWaveTiles = 0;
            int waveIteration = 0;
            int currentWave = 1;
            int currentTileValue;

            do {
//                x = FX[uzd];
//                y = FY[uzd];
                // need to create a new instance for transition animation
                Coordinate coordinate = new Coordinate(closingCoordinates[uzd]);
                k = 0;

                if (waveIteration == 0) {
                    final int tempCurrentWave = currentWave;
                    addTraversalFrame(e -> writeToDefaultFile("BANGA "
                            + tempCurrentWave + ", žymė L="
                            + (tempCurrentWave + Tile.STARTING_VALUE)));
                    addTraversalFrame(e -> updateCurrentIterationLbl(
                            String.valueOf(tempCurrentWave + Tile.STARTING_VALUE)));
                }

                final int tempUzd = uzd;
                addTraversalFrame(e -> writeToDefaultFile("\tUždaroma UZD=" + tempUzd +
                        ", " + coordinate));

                while (k < 4 && !exists) {

//                    u = x + CX[k];
//                    v = y + CY[k];
                    // need to create new instance for transition animation
                    // - keyframes gets the value from the same reference (of the last currentCoordinate reference value)
                    Coordinate currentCoordinate = new Coordinate(coordinate);
                    currentCoordinate.moveByCoordinate(rules[k]);

//                    currentTileValue = labyrinth[v][u];
                    currentTileValue = getLabyrinthValueAtCoordinate(currentCoordinate);
                    if (currentTileValue == 0) {

                        final int tempK = k; final int tempNauja = nauja;
                        addTraversalFrame(e -> writeToDefaultFile("\t\tR " + (tempK+1) +
                                ". " + currentCoordinate + ". Laisva. NAUJA=" + tempNauja));

//                        labyrinth[v][u] = labyrinth[y][x] + 1;
                        setLabyrinthValueAtCoordinate(currentCoordinate,
                                getLabyrinthValueAtCoordinate(coordinate) + 1);

                        addTraversalFrame(e -> setCurrent(currentCoordinate,
                                getLabyrinthValueAtCoordinate(coordinate) + 1));

                        addTraversalFrame(e -> setVisited(coordinate));

                        if (exitFound(currentCoordinate)) {
                            setFinishCoordinates(currentCoordinate);
                            getPath();
                            addTraversalFrame(e -> controller.processEndOfTraversal());
                            return;
                        } else {
                            ++nextWaveTiles;
                            ++nauja;

//                            FX[nauja] = u;
//                            FY[nauja] = v;
                            closingCoordinates[nauja].setCoordinates(currentCoordinate);
                        }
                    } else if (currentTileValue == 1) {

                        final int tempK = k;
                        addTraversalFrame(e -> writeToDefaultFile("\t\tR " + (tempK+1) +
                                ". " + currentCoordinate + ". Siena"));
                    } else {

                        final int tempK = k;
                        addTraversalFrame(e -> writeToDefaultFile("\t\tR " + (tempK+1) +
                                ". " + currentCoordinate + ". UŽDARYTA arba ATIDARYTA"));
                    }
                    ++k;
                }

                ++uzd;
                ++waveIteration;
                if (currentWaveTiles <= waveIteration) { // end of wave

                    currentWaveTiles = nextWaveTiles;
                    nextWaveTiles = 0;
                    waveIteration = 0;
                    ++currentWave;
                    ++currentTransitionStep;
                }

            } while (uzd <= nauja && !exists);
        }
    }

    /********* UTILITY FUNCTIONS FOR TRAVERSAL ALGORITHMS *********/

    private boolean exitFound(Coordinate coordinate) {
//        if (coordinate.getX() == 0 || coordinate.getX() == (getCols() - 1)
//                || coordinate.getY() == 0 || coordinate.getY() == (getRows() - 1)) {
        if (coordinate.isAtBorder(getCols(), getRows())) {

            traversalPath.add(new Coordinate(coordinate));
            this.exists = true;
            return true;
        }
        return false;
    }

    private void getPath() {

//        int currentX = finishX;
//        int currentY = finishY;
        Coordinate coordinate = new Coordinate(getFinishCoordinate());
        Coordinate currentCoordinate = new Coordinate();
        int currentValue = 0;
//        int previousValue = labyrinth[currentY][currentX];
        int previousValue = getLabyrinthValueAtCoordinate(coordinate);
//        int u = -1;
//        int v = -1;
        do {
            for(int k = 0; k < rules.length; ++k) {
//                u = currentX + CX[k];
//                v = currentY + CY[k];
                currentCoordinate.setCoordinates(coordinate);
                currentCoordinate.moveByCoordinate(rules[k]);

                if (currentCoordinate.isBetweenBorders(getCols() + 1, getRows() + 1)) {

                    currentValue = getLabyrinthValueAtCoordinate(currentCoordinate);
                    if (currentValue >= Tile.STARTING_VALUE && currentValue < previousValue) {

                        previousValue = currentValue;
                        coordinate.setCoordinates(currentCoordinate);
//                        currentX = u;
//                        currentY = v;
                        prodPath.add(k);
                        traversalPath.add(new Coordinate(coordinate.getX() + 1,
                                coordinate.getY() + 1));
                        break;
                    }
                }
            }
        } while (currentValue != Tile.STARTING_VALUE);

        reversePaths();
        traversalPath.add(new Coordinate(currentCoordinate.getX() + 1,
                currentCoordinate.getY() + 1));
    }

    /********* DERIVE PRODUCTIONS AND VERTICES PATHS *********/

    public String getProdPath() {
        if (prodPath == null || prodPath.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        for(Integer prod : prodPath) {
            sb.append("R").append(prod+1).append(" -> ");
            ++i;

            if (i % 9 == 0) {
                sb.append("\n\t\t\t\t\t\t");
            }
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 4);

        return str;
    }

    public String getTraversalPath() {
        if (traversalPath == null || traversalPath.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Coordinate coordinate : traversalPath) {

            sb.append(coordinate).append(" -> ");
            ++i;

            if (i % 4 == 0) {
                sb.append("\n\t\t\t\t\t\t");
            }
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 4);

        return str;
    }

    private void reversePaths() {
        int vertexListSize = traversalPath.size();
        for(int i = 0; i < vertexListSize/2; ++i) {
            Coordinate coordinate = traversalPath.get(vertexListSize-i-1);
            traversalPath.set(vertexListSize-i-1, traversalPath.get(i));
            traversalPath.set(i, coordinate);
        }
        vertexListSize = prodPath.size();
        for(int i = 0; i < vertexListSize/2; ++i) {

            int lastIndex = vertexListSize-i-1;
            int prod = prodPath.get(lastIndex);
            prodPath.set(lastIndex, (prodPath.get(i) + 2) % 4);
            prodPath.set(i, (prod + 2) % 4);
        }
    }
}
