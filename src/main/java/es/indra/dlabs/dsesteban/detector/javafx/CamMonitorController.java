/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.javafx;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.Face;
import es.indra.dlabs.dsesteban.detector.VideoGrabber;
import es.indra.dlabs.dsesteban.detector.VideoGrabber.GrabberStatus;
import es.indra.dlabs.dsesteban.detector.cdi.Detector;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.GrabberEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class CamMonitorController {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CamMonitorController.class);

    private static final String FACE_TEMPLATE = "/design/Face.fxml";
    private static final int MAX_FACES_ACTIVES = 3;

    @FXML
    Button cameraButton;
    @FXML
    ImageView camView;
    @FXML
    StackPane overlayPane;
    @FXML
    BorderPane waitingPane;
    @FXML
    BorderPane operPane;

    @Inject
    VideoGrabber grabber;
    @Inject
    FXMLLoader fxmlLoader;

    final ConcurrentMap<String, StackPane> faces = new ConcurrentHashMap<>();
    private boolean cameraActive;
    private boolean overlaySized;
    private Rectangle2D overlaySize;

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
                overlaySize = new Rectangle2D(0, 0, image.getWidth(), image.getHeight());
            }
            final Image imageFx = JavaFXUtils.mat2Image(image);
            if (imageFx != null) {
                camView.setImage(imageFx);
            }
        });
    }

    /**
     * TODO: document.
     * @param status
     *        TODO: document
     */
    public void loadingState(@Observes @DetectorEvent final Detector.PlatformStatus status) {
        switch (status) {
            case READY:
                Platform.runLater(() -> {
                    waitingPane.setVisible(false);
                    cameraButton.setDisable(false);
                    operPane.setVisible(true);
                });
                break;
            case ERROR:
                Platform.runLater(() -> {
                    // TODO: poner un mnesaje de error en condiciones
                    waitingPane.setVisible(false);
                    cameraButton.setDisable(true);
                    operPane.setVisible(false);
                });
                break;
            case INITIALIZING:
                Platform.runLater(() -> {
                    waitingPane.setVisible(true);
                    cameraButton.setDisable(true);
                    operPane.setVisible(false);
                });
                break;
            default:
        }
    }

    private static final Color[] COLORS = {
        Color.AQUAMARINE, Color.BLACK, Color.BURLYWOOD, Color.CHARTREUSE
    };

    private StackPane createFace(final String name) {
        StackPane face = null;
        // TODO: obtener el Inputstream cacheado y reusar
        try (InputStream is = this.getClass().getResourceAsStream(FACE_TEMPLATE)) {
            face = fxmlLoader.load(is);
            final Text text = (Text)face.getChildrenUnmodifiable().get(0);
            text.setText(name);

            final Color color = COLORS[ThreadLocalRandom.current().nextInt(COLORS.length)];
            text.setFill(color);
            final Rectangle rect = (Rectangle)face.getChildrenUnmodifiable().get(1);
            rect.setStroke(color);
            overlayPane.getChildren().add(face);
        } catch (IOException ex) {
            LOG.error("Face component cannot be created: {}", ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
        }
        return face;
    }

    /**
     * TODO: document.
     * @param faceRect
     *        TODO: document
     */
    public void showFace(@ObservesAsync @DetectorEvent final Face faceRect) {
        Platform.runLater(() -> {
            final StackPane face = faces.computeIfAbsent(faceRect.name,
                (name) -> (faces.size() < MAX_FACES_ACTIVES) ? createFace(name) : null);
            if (face != null) {
                final Rectangle rect = (Rectangle)face.getChildrenUnmodifiable().get(1);
                final Timeline wt = new Timeline(new KeyFrame(Duration.millis(100),
                    new KeyValue(rect.widthProperty(), faceRect.width),
                    new KeyValue(rect.heightProperty(), faceRect.height)));
                final TranslateTransition tt = new TranslateTransition(Duration.millis(100), face);
                tt.setToX((overlaySize.getWidth() - faceRect.width) / 2d - faceRect.x);
                tt.setToY(faceRect.y - (overlaySize.getHeight() - faceRect.height) / 2d);
                final ParallelTransition pt = new ParallelTransition(tt, wt);
                pt.play();
            }
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
