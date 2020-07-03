/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public class CamMonitorController implements VideoPlayer {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CamMonitorController.class);

    @FXML
    private Button cameraButton;

    @FXML
    private ImageView camView;

    @Inject
    VideoGrabber grabber;

    private boolean cameraActive;

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
            // TODO: desabilitar botón mientras se obtiene handle de cámara
            grabber.stopCapturing(this);
        } else {
            cameraActive = true;
            cameraButton.setText("Stop Camera");
            grabber.startCapturing(this);
            // TODO: desabilitar botón mientras se suelta el handlde de cámara
        }
    }

    @Override
    public void showImage(final Image image) {
        Platform.runLater(() -> {
            camView.setImage(image);
        });
    }

}
