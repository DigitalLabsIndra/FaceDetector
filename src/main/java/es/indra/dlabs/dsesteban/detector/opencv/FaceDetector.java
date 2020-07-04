/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.time.Duration;
import java.time.Instant;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.Face;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.GrabberEvent;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class FaceDetector {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FaceDetector.class);

    private static final String FACE_HAARCASCADE = "data/haarcascades/haarcascade_frontalface_alt.xml";

    private static final Duration LAPSUS = Duration.ofMillis(100);
    private static final double SCALE = 0.25;

    private CascadeClassifier faceCascade;
    private Instant lastProcessed = Instant.MIN;
    private Duration accumulator = Duration.ZERO;
    private int ticks;

    @Inject
    @DetectorEvent
    Event<Face> eventFaces;

    CascadeClassifier getClassifier() {
        if (faceCascade == null) {
            faceCascade = new CascadeClassifier(FACE_HAARCASCADE);
        }
        return faceCascade;
    }

    /**
     * TODO: document.
     * @param frame
     *        TODO: document
     */
    public void processImage(@ObservesAsync @GrabberEvent final Mat frame) {
        final Instant now = Instant.now();
        if (Duration.between(lastProcessed, now).compareTo(LAPSUS) > 0) {
            lastProcessed = now;

            final Mat matRed = new Mat();
            Imgproc.resize(frame, matRed, new Size(), SCALE, SCALE, Imgproc.INTER_AREA);
            final Mat grayFrame = new Mat();
            Imgproc.cvtColor(matRed, grayFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);
            final MatOfRect faces = new MatOfRect();
            getClassifier().detectMultiScale(grayFrame, faces, 1.1, 2, 0);

            final Instant after = Instant.now();
            accumulator = accumulator.plus(Duration.between(now, after));
            ticks++;
            if (ticks > 10) {
                LOG.trace("Media procesamiento: {}", accumulator.toMillis() / ticks);
                accumulator = Duration.ZERO;
                ticks = 0;
            }

            faces.toList().forEach((rect) -> {
                final Face face = new Face();
                face.name = "faceX";
                face.x = rect.x / SCALE;
                face.y = rect.y / SCALE;
                face.height = rect.height / SCALE;
                face.width = rect.width / SCALE;
                eventFaces.fireAsync(face);
            });
        }
    }

}
