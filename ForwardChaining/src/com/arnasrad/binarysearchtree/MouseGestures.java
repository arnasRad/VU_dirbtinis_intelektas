package com.arnasrad.binarysearchtree;

import com.arnasrad.binarysearchtree.graph.Graph;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class MouseGestures {

    private final DragContext dragContext = new DragContext();

    private Graph graph;

    private MainController controller;

    public MouseGestures(MainController controller, Graph graph) {
        this.controller = controller;
        this.graph = graph;
    }

    public void makeDraggable( final Node node) {


        node.setOnMousePressed(onMousePressedEventHandler);
        node.setOnMouseDragged(onMouseDraggedEventHandler);
        node.setOnMouseReleased(onMouseReleasedEventHandler);
        node.setOnMouseClicked(onMouseClickedEventHandler);

    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            Node node = (Node) event.getSource();

            double scale = graph.getScale();

            dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
            dragContext.y = node.getBoundsInParent().getMinY()  * scale - event.getScreenY();


//                FXMLLoader loader = new FXMLLoader(MainController.class.getResource(" view/main-view.fxml"));
//                Pane pane = loader.load();
//                MainController controller = loader.getController();

//            MainController.State currentAppState = controller.getCurrentRunState();
//            switch (currentAppState) {
//                case START_VERTEX:
//                    controller.setStartVertex((Vertex) event.getSource());
//                    break;
//                case TARGET_VERTEX:
//                    controller.setTargetVertex((Vertex) event.getSource());
//                    break;
//            }
        }
    };

    private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            Node node = (Node) event.getSource();

            double offsetX = event.getScreenX() + dragContext.x;
            double offsetY = event.getScreenY() + dragContext.y;

            // adjust the offset in case we are zoomed
            double scale = graph.getScale();

            offsetX /= scale;
            offsetY /= scale;

            node.relocate(offsetX, offsetY);

        }
    };

    private EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

        }
    };

    private EventHandler<MouseEvent> onMouseClickedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {

//            MainController.State currentAppState = controller.getCurrentRunState();
//            switch (currentAppState) {
//                case START_VERTEX:
//                    controller.setStartVertex((Vertex) mouseEvent.getSource());
//                    break;
//                case TARGET_VERTEX:
//                    controller.setTargetVertex((Vertex) mouseEvent.getSource());
//                    break;
//            }
        }
    };

    static class DragContext {

        double x;
        double y;

    }
}