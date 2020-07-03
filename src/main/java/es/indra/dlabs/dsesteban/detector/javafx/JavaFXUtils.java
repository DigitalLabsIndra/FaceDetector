/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.javafx;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public final class JavaFXUtils {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(JavaFXUtils.class);

    private JavaFXUtils() {
    }

    /**
     * TODO: document.
     * @param frame
     *        TODO: document
     * @return TODO: document
     */
    public static Image mat2Image(final BufferedImage frame) {
        try {
            return SwingFXUtils.toFXImage(frame, null);
        } catch (IllegalArgumentException ex) {
            LOG.error("Cannot convert the BufferedImage object: {}", ex.getMessage());
            return null;
        }
    }

}
