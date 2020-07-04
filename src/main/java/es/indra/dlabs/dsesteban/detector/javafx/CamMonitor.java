/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.javafx;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.VideoGrabber;
import es.indra.dlabs.dsesteban.detector.cdi.Detector;
import es.indra.dlabs.dsesteban.detector.cdi.Detector.PlatformActions;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.StartupScene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class CamMonitor {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CamMonitor.class);

    private static final String MAIN_WINDOW = "/design/CamMonitor.fxml";

    @Inject
    FXMLLoader fxmlLoader;

    @Inject
    VideoGrabber grabber;

    @Inject
    @DetectorEvent
    Event<Detector.PlatformActions> evtDetector;

    /**
     * TODO: document.
     * @param primaryStage
     *        TODO: document
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void start(@Observes @StartupScene final Stage primaryStage) {
        try (InputStream is = this.getClass().getResourceAsStream(MAIN_WINDOW)) {
            final StackPane root = fxmlLoader.load(is);
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
            evtDetector.fireAsync(PlatformActions.START);
        } catch (IOException ex) {
            LOG.error("Main window cannot be created: {}", ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
        }
    }
}
