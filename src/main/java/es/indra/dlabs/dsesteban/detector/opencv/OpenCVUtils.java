/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public final class OpenCVUtils {

    private OpenCVUtils() {
    }

    /**
     * TODO: document.
     * @param original
     *        TODO: document
     * @return TODO: document
     */
    public static BufferedImage matToBufferedImage(final Mat original) {
        final BufferedImage image;
        final int width = original.width();
        final int height = original.height();
        final int channels = original.channels();
        final byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

}
