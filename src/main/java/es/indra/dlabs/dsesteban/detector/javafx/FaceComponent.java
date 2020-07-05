/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.javafx;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
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
public class FaceComponent extends StackPane {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FaceComponent.class);

    private static final String FACE_TEMPLATE = "/design/Face.fxml";
    private static final Duration LATENCY_DURATION = Duration.millis(60);

    @FXML
    Text nameLbl;
    @FXML
    Text metaLbl;
    @FXML
    Rectangle frameRct;
    private Rectangle2D overlaySize = Rectangle2D.EMPTY;

    /**
     * TODO: document.
     * @throws RuntimeException
     *         TODO: document
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public FaceComponent() {
        // TODO: obtener el Inputstream cacheado y reusar
        try (InputStream is = this.getClass().getResourceAsStream(FACE_TEMPLATE)) {
            final FXMLLoader loader = new FXMLLoader();
            loader.setRoot(this);
            loader.setController(this);
            loader.load(is);
        } catch (IOException ex) {
            LOG.error("Face component cannot be created: {}", ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * TODO: document.
     * @param name
     *        TODO: document
     */
    public void setFaceName(final String name) {
        nameLbl.setText(name);
    }

    /**
     * TODO: document.
     * @param meta
     *        TODO: document
     */
    public void setFaceMeta(final String meta) {
        if (meta != null) {
            metaLbl.setText(meta);
        } else {
            metaLbl.setText("");
        }
    }

    /**
     * TODO: document.
     * @param color
     *        TODO: document
     */
    public void setFaceColor(final Color color) {
        nameLbl.setFill(color);
        metaLbl.setFill(color);
        frameRct.setStroke(color);
    }

    /**
     * TODO: document.
     * @param size
     *        TODO: document
     */
    public void setOverlaySize(final Rectangle2D size) {
        overlaySize = size;
    }

    /**
     * TODO: document.
     * @param x
     *        TODO: document
     * @param y
     *        TODO: document
     * @param width
     *        TODO: document
     * @param height
     *        TODO: document
     */
    public void resizeAndMove(final double x, final double y, final double width, final double height) {
        final Timeline wt = new Timeline(new KeyFrame(LATENCY_DURATION,
            new KeyValue(frameRct.widthProperty(), width),
            new KeyValue(frameRct.heightProperty(), height)));
        final TranslateTransition tt = new TranslateTransition(LATENCY_DURATION, this);
        tt.setToX((overlaySize.getWidth() - width) / 2d - x);
        tt.setToY(y - (overlaySize.getHeight() - height) / 2d);
        final ParallelTransition pt = new ParallelTransition(tt, wt);
        pt.play();
    }

}
