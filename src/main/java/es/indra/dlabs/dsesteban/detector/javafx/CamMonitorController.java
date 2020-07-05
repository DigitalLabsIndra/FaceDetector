/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.javafx;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.event.Event;
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
import es.indra.dlabs.dsesteban.detector.cdi.DetectorAction;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorAction.DetectorActions;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorInfo;
import es.indra.dlabs.dsesteban.detector.cdi.GrabberEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class CamMonitorController {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CamMonitorController.class);

    private static final int MAX_FACES_ACTIVES = 3;
    private static final Color[] COLORS = {
        Color.AQUAMARINE, Color.BURLYWOOD, Color.CHARTREUSE, Color.CORAL, Color.GAINSBORO
    };

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
    @FXML
    VBox detectorsPane;

    @Inject
    VideoGrabber grabber;
    @Inject
    FXMLLoader fxmlLoader;
    @Inject
    @DetectorEvent
    Event<DetectorAction> evtDetectorAction;

    private final List<String> detectors = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentMap<String, FaceComponent> faces = new ConcurrentHashMap<>();
    private boolean cameraActive;
    private boolean overlaySized;
    private Rectangle2D overlaySize;
    private final List<Color> availableColors = new ArrayList<>();

    /**
     * TODO: document.
     */
    @FXML
    public void initialize() {
        LOG.trace("{} has been initialized", this.getClass());
    }

    void receiveDetectors(@Observes @DetectorEvent final DetectorInfo info) {
        if (!detectors.contains(info.id)) {
            detectors.add(info.id);
            Platform.runLater(() -> {
                final CheckBox check = new CheckBox(info.id);
                check.setSelected(false);
                check.setAllowIndeterminate(false);
                check.setOnAction((action) -> {
                    if (check.isSelected()) {
                        evtDetectorAction.fireAsync(new DetectorAction(info.id, DetectorActions.START));
                    } else {
                        evtDetectorAction.fireAsync(new DetectorAction(info.id, DetectorActions.STOP));
                    }
                });
                detectorsPane.getChildren().add(check);
            });
        }
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
            faces.values().forEach((face) -> overlayPane.getChildren().remove(face));
            faces.clear();
            availableColors.addAll(Arrays.asList(COLORS));
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

    private FaceComponent createFace(final String name, final String meta) {
        final FaceComponent face = new FaceComponent();
        face.setFaceName(name);
        face.setFaceMeta(meta);
        if (!availableColors.isEmpty()) {
            final Color color = availableColors.remove(0);
            face.setFaceColor(color);
        }
        face.setOverlaySize(overlaySize);
        Platform.runLater(() -> overlayPane.getChildren().add(face));
        return face;
    }

    /**
     * TODO: document.
     * @param faceRect
     *        TODO: document
     */
    public void showFace(@ObservesAsync @DetectorEvent final Face faceRect) {
        final FaceComponent face = faces.computeIfAbsent(faceRect.name,
            (name) -> (faces.size() < MAX_FACES_ACTIVES) ? createFace(name, null) : null);
        if (face != null) {
            face.setFaceMeta(faceRect.meta);
            face.resizeAndMove(faceRect.x, faceRect.y, faceRect.width, faceRect.height);
        }
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
