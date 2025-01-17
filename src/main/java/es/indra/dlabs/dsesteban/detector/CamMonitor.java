/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public class CamMonitor {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CamMonitor.class);

    private static final String MAIN_WINDOW = "/design/CamMonitor.fxml";

    @Inject
    FXMLLoader fxmlLoader;

    @Inject
    VideoGrabber grabber;

    /**
     * TODO: document.
     * @param primaryStage
     *        TODO: document
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void start(final Stage primaryStage) {
        try (InputStream is = this.getClass().getResourceAsStream(MAIN_WINDOW)) {
            final BorderPane root = fxmlLoader.load(is);
            final Scene scene = new Scene(root);
            primaryStage.setTitle("Face Detector");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest((event) -> {
                if (WindowEvent.WINDOW_CLOSE_REQUEST.equals(event.getEventType())) {
                    try {
                        grabber.close();
                    } catch (Exception ex) {
                        LOG.warn("Error closing video grabber: {}", ex.getMessage());
                    }
                }
            });
            primaryStage.show();
        } catch (IOException ex) {
            LOG.error("Main window cannot be created: {}", ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
        }

    }
}
