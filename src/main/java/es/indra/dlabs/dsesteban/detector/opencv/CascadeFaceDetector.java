/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.time.Duration;
import java.time.Instant;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
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
import es.indra.dlabs.dsesteban.detector.cdi.DetectorAction;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorInfo;
import es.indra.dlabs.dsesteban.detector.cdi.GrabberEvent;
import es.indra.dlabs.dsesteban.detector.opencv.OpenCVDetector.DetectorActions;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class CascadeFaceDetector {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CascadeFaceDetector.class);

    private static final String FACE_HAARCASCADE = "data/haarcascades/haarcascade_frontalface_alt.xml";
    private static final String ID = "Cascade Detector";
    private static final int TICKS_COUNT = 40;

    private static final Duration LAPSUS = Duration.ofMillis(100);
    private static final double SCALE = 0.25;

    private CascadeClassifier faceCascade;
    private Instant lastProcessed = Instant.MIN;
    private Duration accumulator = Duration.ZERO;
    private int ticks;
    private boolean initialized;
    private boolean active;

    @Inject
    @DetectorEvent
    Event<Face> eventFaces;
    @Inject
    @DetectorEvent
    Event<DetectorInfo> evtInfo;

    void initialize(@Observes @DetectorEvent final DetectorActions action) {
        switch (action) {
            case START:
                faceCascade = new CascadeClassifier(FACE_HAARCASCADE);
                LOG.info("{} has been initialized", ID);
                evtInfo.fire(new DetectorInfo(ID));
                initialized = true;
                break;
            case STOP:
                initialized = false;
                break;
            default:
        }
    }

    void activate(@ObservesAsync @DetectorEvent final DetectorAction signal) {
        if (ID.equals(signal.id)) {
            // TODO: hacerlo threadsafe
            switch (signal.action) {
                case START:
                    active = true;
                    LOG.info("{} has been activated", ID);
                    break;
                case STOP:
                    active = false;
                    LOG.info("{} has been deactivated", ID);
                    break;
                default:
            }
        }
    }

    /**
     * TODO: document.
     * @param frame
     *        TODO: document
     */
    public void processImage(@ObservesAsync @GrabberEvent final Mat frame) {
        if (initialized && active) {
            final Instant now = Instant.now();
            if (Duration.between(lastProcessed, now).compareTo(LAPSUS) > 0) {
                lastProcessed = now;

                final Mat matRed = new Mat();
                Imgproc.resize(frame, matRed, new Size(), SCALE, SCALE, Imgproc.INTER_AREA);
                final Mat grayFrame = new Mat();
                Imgproc.cvtColor(matRed, grayFrame, Imgproc.COLOR_BGR2GRAY);
                Imgproc.equalizeHist(grayFrame, grayFrame);
                final MatOfRect faces = new MatOfRect();
                faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0);

                final Instant after = Instant.now();
                accumulator = accumulator.plus(Duration.between(now, after));
                ticks++;
                if (ticks > TICKS_COUNT) {
                    LOG.trace("Media procesamiento: {}", accumulator.toMillis() / ticks);
                    accumulator = Duration.ZERO;
                    ticks = 0;
                }

                faces.toList().forEach((rect) -> {
                    final Face face = new Face();
                    face.name = "faceX";
                    face.meta = "Cascade";
                    face.x = rect.x / SCALE;
                    face.y = rect.y / SCALE;
                    face.height = rect.height / SCALE;
                    face.width = rect.width / SCALE;
                    eventFaces.fireAsync(face);
                });
            }
        }
    }

}
