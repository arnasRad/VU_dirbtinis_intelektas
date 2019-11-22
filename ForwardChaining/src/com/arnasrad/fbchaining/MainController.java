package com.arnasrad.fbchaining;

import com.arnasrad.fbchaining.model.vertex.Vertex;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class MainController {

    public enum  State {
        INPUT, // start of the program; enter input filename
        OUTPUT, // enter output filename
        TRAVERSE, // play graph traversal (step-by-step or run-through)
        FINISH // end of traversal; may input a new graph
    }

    public enum OutputType {

        SEMANTIC_GRAPH,
        VERIFICATION_GRAPH
    }

    public static final class StatesPromptTexts {
        public static final String INPUT = "Enter text input file name (without .txt extension)";
        public static final String OUTPUT = "Enter output file name (without type extension):";
        public static final String TRAVERSE = "Step or run through the graph. See help for more information";
        public static final String FINISH = "Path found! Enter a new input file or reset the current graph with new path search";
    }

    private State currentRunState = State.INPUT; // program state used to determine console input processing

//    private Labyrinth labyrinth;

    private Chaining chaining;
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
    Label factsCountLbl;
    @FXML
    Label rulesCountLbl;
    @FXML
    Label currentVertexLbl;
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

//    private Graph graph;
    @FXML
    VBox boardVBox;

    @FXML
    ToggleGroup traverseGroup;
    @FXML
    RadioMenuItem menuForward;
    @FXML
    RadioMenuItem menuBackward;

    @FXML
    ToggleGroup outputGroup;
    @FXML
    RadioMenuItem menuVerificationGraph;
    @FXML
    RadioMenuItem menuSemanticGraph;

    @FXML
    public void initialize() {
        setProgramStateLbl(State.INPUT);
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
            case INPUT:
                processInputFilename();
                break;
            case OUTPUT:
                processOutputFilename();
                break;
            case FINISH:
                String tempFileName = console.getText(); // restartApplication() nullifies global fileName variable
                restartApplication();
                processInputFilename(tempFileName);
                break;
            default:
                System.err.println("UNKNOWN ERROR: incorrect run state. State code: " + currentRunState);
        }
    }

    @FXML
    public void processResetClicked(ActionEvent actionEvent) {
        resetBoardContainers();
        clearProgramStateLabels();

        try {
            this.chaining.reset();
            changeToTraversalState();
        } catch (Exception e) {
            setInstructionsLbl(e.getMessage());
        }
//        this.labyrinth.reset();

//        setupGraphContainers();
    }

    @FXML
    public void processRunClicked(ActionEvent actionEvent) {

        disableButtons();
        resetBtn.setDisable(false);

        initializeFileWriter();

        new Thread(chaining).start();
//        new Thread(labyrinth).start();
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
        alert.setHeaderText("About Graph Search");
        alert.setContentText("<...>");

        alert.showAndWait();
    }

//    public ScrollPane getGraphScrollPane() {
//        return this.graph.getScrollPane();
//    }

    public FileWriter getFileWriter() {
        return this.fileWriter;
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

    public State getCurrentRunState() {
        return this.currentRunState;
    }

    public void resetConsole() {
        this.console.setText(null);
    }

    public Chaining.ChainingType getTraversalOption() {

        String selectedToggleId = ((RadioMenuItem) traverseGroup
                .getSelectedToggle()).getId();

        switch (selectedToggleId) {
            case "menuForward":
                return Chaining.ChainingType.FORWARD;
            case "menuBackward":
                return Chaining.ChainingType.BACKWARD;
            default:
                return null;
        }
    }

    // TODO: implement output types (choose which graph to output)
    public OutputType getOutputTypeOption() {

        String selectedToggleId = ((RadioMenuItem) outputGroup
                .getSelectedToggle()).getId();

        switch (selectedToggleId) {
            case "menuVerificationGraph":
                return OutputType.VERIFICATION_GRAPH;
            case "menuSemanticGraph":
                return OutputType.SEMANTIC_GRAPH;
            default:
                return null;
        }
    }

    public int getTraversalSpeed() {
        return this.traversalSpeed;
    }

    private void restartApplication() {
        changeState(State.INPUT);
        clearProgramStateLabels();

//        this.labyrinth.restart();

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

    public void setProgramStateLbl(State state) {

        programStateLbl.setText(state.toString());
    }

    public void setCurrentIterationLbl(int currentIteration) {

        currentIterationLbl.setText(String.valueOf(currentIteration));
    }

    public void setTraversalModeLbl(String traversalMode) {

        traversalModeLbl.setText(traversalMode);
    }

    public void setFactsCountLbl(int count) {

        factsCountLbl.setText(String.valueOf(count));
    }

    public void setRulesCountLbl(int count) {

        rulesCountLbl.setText(String.valueOf(count));
    }

    public void setCurrentVertexLbl(Vertex vertex) {

        currentVertexLbl.setText(vertex.getVertexId());
    }

//    public void setCurrentCoordinateLbl(Coordinate currentVertex) {
//
//        currentCoordinateLbl.setText("(x, y) = (" + currentVertex.getX() +
//                ", " + currentVertex.getY() + ")");
//    }

    public void setInstructionsLbl(String instructions) {

        instructionsLbl.setText(instructions);
    }

    public void clearProgramStateLabels() {

        currentIterationLbl.setText(null);
        traversalModeLbl.setText(null);
        factsCountLbl.setText(null);
        currentVertexLbl.setText(null);
        instructionsLbl.setText(null);
    }

    private void resetBoardContainers() {

        boardVBox.getChildren().clear();
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
            this.chaining = new Chaining(this, fileName);

            // user will be prompted to specify the output file next
            changeState(State.OUTPUT);
        } catch (Exception e) {
            resetConsole();
            setInstructionsLbl(e.getMessage());
        }
    }

    private void setupGraphContainers() {
//        boardGridPane.prefWidthProperty().bind(boardVBox.widthProperty());
//        boardGridPane.prefHeightProperty().bind(boardVBox.heightProperty());

        boardVBox.getChildren().add(chaining.getSynthesizedGraph().getScrollPane());
//        graph.getGraph().getModel().setBTVertexPositions();
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
        // user will be asked to click on the starting vertex on the board
        changeToTraversalState();
        setupGraphContainers();
//        changeState(State.START_VERTEX);
        // disable "Enter" button; not used in this step
//        enterBtn.setDisable(true);
//        enterBtn.setDefaultButton(false);
//        console.setDisable(true);
    }

//    public void setStartVertex(Vertex vertex) {
//
//        graph.setStartVertex(vertex);
//        changeToTargetVertexState();
//    }
//
//    public void setTargetVertex(Vertex vertex) {
//
//        graph.setTargetVertex(vertex);
//        changeToTraversalState();
//    }

//    private void changeToStartVertexState() {
//
//        // update console for UI
//        // user will be asked to click on the starting vertex on the board
//        setupGraphContainers();
//        changeState(State.START_VERTEX);
//        // disable all buttons; not used in this step
//        disableButtons();
//    }

//    private void changeToTargetVertexState() {
//
//        changeState(State.TARGET_VERTEX);
//    }

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

        changeState(State.TRAVERSE);
//        labyrinth.getStartingTile().setValue(Tile.STARTING_VALUE);
//        labyrinth.getStartingTile().setCurrent();
        resetBtn.setDisable(false);
        runBtn.setDisable(false);
        runBtn.setDefaultButton(true);
    }

    public void processEndOfTraversal() {
        changeState(State.FINISH);
        enterBtn.setDisable(false);
        resetBtn.setDisable(false);
        resetBtn.setDefaultButton(true);
        console.setDisable(false);

        chaining.printResults();

        try {
            this.fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeState(State state) {

        // update console for UI and program state
        console.setText(null);
        instructionsLbl.setText(getStatePromptText(state));
        setProgramStateLbl(state);
        this.currentRunState = state;

    }

    private String getStatePromptText(State state) {
        switch (state) {
            case INPUT:
                return StatesPromptTexts.INPUT;
            case OUTPUT:
                return StatesPromptTexts.OUTPUT;
            case TRAVERSE:
                return StatesPromptTexts.TRAVERSE;
            case FINISH:
                return StatesPromptTexts.FINISH;
        }

        return null;
    }
}
