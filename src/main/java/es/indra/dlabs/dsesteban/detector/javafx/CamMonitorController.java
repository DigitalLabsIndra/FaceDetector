/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.javafx;

import java.awt.image.BufferedImage;

import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.Face;
import es.indra.dlabs.dsesteban.detector.VideoGrabber;
import es.indra.dlabs.dsesteban.detector.VideoGrabber.GrabberStatus;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.GrabberEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class CamMonitorController {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CamMonitorController.class);

    @FXML
    Button cameraButton;
    @FXML
    ImageView camView;
    @FXML
    Canvas overlay;

    @Inject
    VideoGrabber grabber;

    private boolean cameraActive;
    private boolean overlaySized;

    /**
     * TODO: document.
     */
    @FXML
    public void initialize() {
        LOG.trace("{} has been initialized", this.getClass());
    }

    /**
     * TODO: document.
     */
    @FXML
    protected void startCamera() {
        if (cameraActive) {
            cameraActive = false;
            cameraButton.setText("Start Camera");
            grabber.stopCapturing();
        } else {
            cameraActive = true;
            cameraButton.setText("Stop Camera");
            grabber.startCapturing();
        }
    }

    /**
     * TODO: document.
     * @param image
     *        TODO: document
     */
    @SuppressWarnings("PMD.MissingOverride")
    public void showImage(@ObservesAsync @GrabberEvent final BufferedImage image) {
        Platform.runLater(() -> {
            if (!overlaySized) {
                overlaySized = true;
                overlay.setHeight(image.getHeight());
                overlay.setWidth(image.getWidth());
            }
            final Image imageFx = JavaFXUtils.mat2Image(image);
            if (imageFx != null) {
                camView.setImage(imageFx);
            }
        });
    }

    /**
     * TODO: document.
     * @param face
     *        TODO: document
     */
    public void showFace(@ObservesAsync @DetectorEvent final Face face) {
        Platform.runLater(() -> {
            final GraphicsContext gc = overlay.getGraphicsContext2D();
            gc.clearRect(0, 0, overlay.getWidth(), overlay.getHeight());
            gc.setLineWidth(2d);
            double x = 640 - face.x - face.width;
            gc.strokeRect(x, face.y, face.width, face.height);
        });
    }

    /**
     * TODO: document.
     * @param status
     *        TODO: document
     */
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public void showStatus(@Observes @GrabberEvent final GrabberStatus status) {
        LOG.trace("Se recibe evento de estado: {}", status);
        boolean disabled;
        switch (status) {
            case INITIALIZING:
            case STOPING:
                disabled = true;
                break;
            case READY:
            case STOPPED:
            default:
                disabled = false;
        }
        Platform.runLater(() -> cameraButton.setDisable(disabled));
    }

}
