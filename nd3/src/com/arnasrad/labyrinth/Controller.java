package com.arnasrad.labyrinth;

import com.arnasrad.labyrinth.model.Tile;
import com.arnasrad.labyrinth.model.Coordinate;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Optional;

public class Controller {

    public static final class State {
        public static final byte INPUT = 0; // 0 - start of the program; enter input filename
        public static final byte OUTPUT = 1; // 1 - enter output filename
        public static final byte COORDINATES = 2; // 2 - choose starting coordinates on the board
        public static final byte TRAVERSE = 3; // 3 - play labyrinth traversal (step-by-step or run-through)
        public static final byte FINISH = 4; // 4 - end of traversal; may input a new labyrinth
    }
    public static final class StateLbl {
        public static final String INPUT = "INPUT"; // 0 - start of the program; enter input filename
        public static final String OUTPUT = "OUTPUT"; // 1 - enter output filename
        public static final String COORDINATES = "COORDINATES"; // 2 - choose starting coordinates on the board
        public static final String TRAVERSE = "TRAVERSE"; // 3 - play labyrinth traversal (step-by-step or run-through)
        public static final String FINISH = "FINISH"; // 4 - end of traversal; may input a new labyrinth
    }

    public static final class StatesPromptTexts {
        public static final String INPUT = "Enter text input file name (without .txt extension)";
        public static final String OUTPUT = "Enter output file name (without type extension):";
        public static final String COORDINATES = "Choose the starting coordinates of traversal by clicking on board cell";
        public static final String TRAVERSE = "Step or run through the labyrinth. See help for more information";
        public static final String FINISH = "Path found! Enter a new input file or reset the current labyrinth with new starting coordinates";
    }

    private short currentRunState = State.INPUT; // program state used to determine console input processing

    private Labyrinth labyrinth;

    private int traversalSpeed = 250;

    private String outputFilename = ""; // log output filename

    private FileWriter fileWriter;

    @FXML
    Label programStateLbl;
    @FXML
    Label currentIterationLbl;
    @FXML
    Label traversalModeLbl;
    @FXML
    Label traversalSpeedLbl;
    @FXML
    Label labyrinthSizeLbl;
    @FXML
    Label currentCoordinateLbl;
    @FXML
    Label instructionsLbl;

    @FXML
    Slider traversalSpeedSlider;

    @FXML
    TextField console;
    @FXML
    Button enterBtn;
    @FXML
    Button resetBtn;
    @FXML
    Button runBtn;

    private GridPane boardGridPane = new GridPane();
    @FXML
    VBox boardVBox;

    @FXML
    ToggleGroup traverseGroup;
    @FXML
    RadioMenuItem menuBacktrack;
    @FXML
    RadioMenuItem menuBacktrack1;
    @FXML
    RadioMenuItem menuBacktrackMem;
    @FXML
    RadioMenuItem menuWave;

    @FXML
    public void initialize() {
        setProgramStateLbl("INPUT");
        traversalSpeedLbl.setText(String.valueOf((int) traversalSpeedSlider.getValue()));

        traversalSpeedSlider.valueProperty().addListener(
                (observableValue, old_val, new_val) -> {

                    traversalSpeed = new_val.intValue();
                    traversalSpeedLbl.setText(String.valueOf(traversalSpeed));
        });


        setTraversalModeLbl(((RadioMenuItem) traverseGroup
                .getSelectedToggle()).getText());

        traverseGroup.selectedToggleProperty().addListener(
                (observableValue, old_val, new_val) -> {

                    setTraversalModeLbl(((RadioMenuItem) new_val).getText());
        });
    }

    @FXML
    public void processEnterClicked(ActionEvent actionEvent) {
        switch (currentRunState) {
            case State.INPUT:
                processInputFilename();
                break;
            case State.OUTPUT:
                processOutputFilename();
                break;
            case State.FINISH:
                String tempFileName = console.getText(); // restartApplication() nullifies global fileName variable
                restartApplication();
                processInputFilename(tempFileName);
                break;
            default:
                System.err.println("UNKNOWN ERROR: incorrect run state. State code: " + currentRunState);
        }
        console.getText();
    }

    @FXML
    public void processResetClicked(ActionEvent actionEvent) {
        resetBoardContainers();
        clearProgramStateLabels();

        this.labyrinth.reset();

        changeToCoordinatesState();
    }

    @FXML
    public void processRunClicked(ActionEvent actionEvent) {

        disableButtons();
        resetBtn.setDisable(false);
        new Thread(labyrinth).start();
    }

    public GridPane getBoardGridPane() {
        return this.boardGridPane;
    }

    public FileWriter getFileWriter() {
        return this.fileWriter;
    }

    public int getCurrentRunState() {
        return this.currentRunState;
    }

    public void resetConsole() {
        this.console.setText(null);
    }

    public String getTraversalOption() {
        return ((RadioMenuItem) traverseGroup
                .getSelectedToggle()).getId();
    }

    public void initializeFileWriter() {
        try {
            fileWriter = new FileWriter(new File(outputFilename));
        } catch (IOException e) {
            console.setText(null);
            console.setPromptText("> ERROR: IOException while trying to create output file.");
            e.printStackTrace();
        }

    }

    public int getTraversalSpeed() {
        return this.traversalSpeed;
    }

    @FXML
    public void menuOpenClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Open input file");
        File inputFile = fileChooser.showOpenDialog(boardVBox.getScene().getWindow());
        if (inputFile == null) {
            return;
        }

        String inputFileName = inputFile.getAbsolutePath();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Open File");
        alert.setContentText("Load a new input file? Current application setup will be restarted and all progress lost.");
        alert.setHeaderText(null);
        alert.setGraphic(null);

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (currentRunState != State.INPUT) {
                restartApplication();
            }

            setupInputFile(inputFileName);
        }

    }

    @FXML
    public void menuRestartClicked(ActionEvent actionEvent) {
        restartApplication();
    }

    @FXML
    public void menuCloseClicked(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit application?");
        alert.setContentText("Close the application?");
        alert.setHeaderText(null);
        alert.setGraphic(null);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    @FXML
    public void menuAboutClicked(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About Labyrinth");
        alert.setContentText("<...>");

        alert.showAndWait();
    }

    private void restartApplication() {
        changeState(StatesPromptTexts.INPUT, State.INPUT);
        clearProgramStateLabels();

        this.labyrinth.restart();

        this.outputFilename = "";
        this.fileWriter = null;

        console.setText(null);
        console.setDisable(false);

        enterBtn.setDefaultButton(true);
        enterBtn.setDisable(false);
        resetBtn.setDefaultButton(false);
        resetBtn.setDisable(true);
        runBtn.setDisable(true);

        resetBoardContainers();
    }

//    public void setProgramStateLabels(String currentIteration, String traversalMode, String labyrinthSize,
//                                         String currentCoordinate, String instructions) {
//
//        if (currentIteration != null) {
//            currentIterationLbl.setText(currentIteration);
//        }
//
//        if (traversalMode != null) {
//           traversalModeLbl.setText(traversalMode);
//        }
//
//        if (labyrinthSize != null) {
//            labyrinthSizeLbl.setText(labyrinthSize);
//        }
//
//        if (currentCoordinate != null) {
//            currentCoordinateLbl.setText(currentCoordinate);
//        }
//
//        if (instructions != null) {
//            instructionsLbl.setText(instructions);
//        }
//    }
//
//    public void setProgramStateLabels(String currentIteration, String traversalMode,
//                                      Coordinate labyrinthSize, Coordinate currentCoordinate,
//                                      String instructions) {
//
//        if (currentIteration != null) {
//            currentIterationLbl.setText(currentIteration);
//        }
//
//        if (traversalMode != null) {
//           traversalModeLbl.setText(traversalMode);
//        }
//
//        if (labyrinthSize != null) {
//            labyrinthSizeLbl.setText("[n, m] = [" + labyrinthSize.getX() +
//                    ", " + labyrinthSize.getY() + "]");
//        }
//
//        if (currentCoordinate != null) {
//            currentCoordinateLbl.setText("(x, y) = (" + currentCoordinate.getX() +
//                    ", " + currentCoordinate.getY() + ")");
//        }
//
//        if (instructions != null) {
//            instructionsLbl.setText(instructions);
//        }
//    }

    public void setProgramStateLbl(String programState) {

        programStateLbl.setText(programState);
    }

    public void setCurrentIterationLbl(String currentIteration) {

        currentIterationLbl.setText(currentIteration);
    }

    public void setTraversalModeLbl(String traversalMode) {

        traversalModeLbl.setText(traversalMode);
    }

    public void setLabyrinthSizeLbl(String labyrinthSize) {

        labyrinthSizeLbl.setText(labyrinthSize);
    }

    public void setLabyrinthSizeLbl(Coordinate labyrinthSize) {

        labyrinthSizeLbl.setText("[n, m] = [" + labyrinthSize.getX() +
                ", " + labyrinthSize.getY() + "]");
    }

    public void setCurrentCoordinateLbl(String currentCoordinate) {

        currentCoordinateLbl.setText(currentCoordinate);
    }

    public void setCurrentCoordinateLbl(Coordinate currentCoordinate) {

        currentCoordinateLbl.setText("(x, y) = (" + currentCoordinate.getX() +
                ", " + currentCoordinate.getY() + ")");
    }

    public void setInstructionsLbl(String instructions) {

        instructionsLbl.setText(instructions);
    }

    public void clearProgramStateLabels() {

        currentIterationLbl.setText(null);
        traversalModeLbl.setText(null);
        labyrinthSizeLbl.setText(null);
        currentCoordinateLbl.setText(null);
        instructionsLbl.setText(null);
    }

    private void resetBoardContainers() {
        boardVBox.getChildren().remove(boardGridPane);
        boardGridPane = new GridPane();
    }

    private void processInputFilename() {

        String fileName = console.getText();
        processInputFilename(fileName);
    }

    private void processInputFilename(String fileName) {
        if (fileName == null) {
            console.setText(null);
            return;
        }
        if (fileName.trim().equals("")) {
            console.setText(null);
            return;
        }

        fileName = fileName.concat(".txt");

        setupInputFile(fileName);
    }

    private void setupInputFile(String fileName) {

        try {
            this.labyrinth = new Labyrinth(this, fileName);

            // user will be prompted to specify the output file next
            changeState(StatesPromptTexts.OUTPUT, State.OUTPUT);
        } catch (Exception e) {
            resetConsole();
            console.setPromptText(e.getMessage());
        }
    }

    private void setupBoardContainers() {
        boardGridPane.prefWidthProperty().bind(boardVBox.widthProperty());
        boardGridPane.prefHeightProperty().bind(boardVBox.heightProperty());
        boardVBox.getChildren().add(boardGridPane);
    }

    private void processOutputFilename() {
        outputFilename = console.getText();
        if (outputFilename == null) {
            console.setText(null);
            return;
        }
        if (outputFilename.trim().equals("")) {
            console.setText(null);
            return;
        }
        outputFilename = outputFilename.concat(".txt");

        // update console for UI
        // user will be asked to click on the starting coordinate on the board
        setupBoardContainers();
        changeState(StatesPromptTexts.COORDINATES, State.COORDINATES);
        // disable "Enter" button; not used in this step
        enterBtn.setDisable(true);
        enterBtn.setDefaultButton(false);
        console.setDisable(true);
    }

    private void changeToCoordinatesState() {

        // update console for UI
        // user will be asked to click on the starting coordinate on the board
        setupBoardContainers();
        changeState(StatesPromptTexts.COORDINATES, State.COORDINATES);
        // disable all buttons; not used in this step
        disableButtons();
    }

    private void disableButtons() {
        enterBtn.setDisable(true);
        enterBtn.setDefaultButton(false);
        resetBtn.setDisable(true);
        resetBtn.setDefaultButton(false);
        runBtn.setDisable(true);
        runBtn.setDefaultButton(false);
        console.setDisable(true);
    }

    public void changeToTraversalState() {

        changeState(StatesPromptTexts.TRAVERSE, State.TRAVERSE);
        labyrinth.getStartingTile().setValue(Tile.STARTING_VALUE);
        labyrinth.getStartingTile().setCurrent();
        resetBtn.setDisable(false);
        runBtn.setDisable(false);
        runBtn.setDefaultButton(true);
    }

    public void processEndOfTraversal() {
        changeState(StatesPromptTexts.FINISH, State.FINISH);
        enterBtn.setDisable(false);
        resetBtn.setDisable(false);
        resetBtn.setDefaultButton(true);
        console.setDisable(false);

        labyrinth.printResults();
        try {
            this.fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeState(String promptText, byte state) {

        // update console for UI and program state
        console.setText(null);
        instructionsLbl.setText(promptText);
        setProgramStateLbl(getStateLbl(state));
        this.currentRunState = state;

    }

    private String getStateLbl(int state) {
        switch (state) {
            case State.INPUT:
                return StateLbl.INPUT;
            case State.OUTPUT:
                return StateLbl.OUTPUT;
            case State.COORDINATES:
                return StateLbl.COORDINATES;
            case State.TRAVERSE:
                return StateLbl.TRAVERSE;
            case State.FINISH:
                return StateLbl.FINISH;
        }

        return null;
    }
}
