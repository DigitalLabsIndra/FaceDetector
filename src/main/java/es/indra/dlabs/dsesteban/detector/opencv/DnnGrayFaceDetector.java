/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
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
public class DnnGrayFaceDetector {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DnnGrayFaceDetector.class);

    private static final String FACE_CONFIG = "data/dnn/opencv_face_detector.pbtxt";
    private static final String FACE_MODEL = "data/dnn/opencv_face_detector_uint8.pb";
    private static final String ID = "Dnn Uint8 Detector";
    private static final double CONFIDENCE_THRESHOLD = 0.7;
    private static final int TICKS_COUNT = 20;

    private static final Duration LAPSUS = Duration.ofMillis(200);

    private Net net;
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
                net = Dnn.readNetFromTensorflow(FACE_MODEL, FACE_CONFIG);
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

                final Mat frameCp = new Mat();
                frame.copyTo(frameCp);
                net.setInput(Dnn.blobFromImage(frameCp, 1.0d, new Size(300, 300), new Scalar(104.0, 177.0, 123.0, 0),
                    true, false));
                final Mat output = net.forward();

                final Instant after = Instant.now();
                accumulator = accumulator.plus(Duration.between(now, after));
                ticks++;
                if (ticks > TICKS_COUNT) {
                    LOG.trace("Media procesamiento: {}", accumulator.toMillis() / ticks);
                    accumulator = Duration.ZERO;
                    ticks = 0;
                }

                final Mat detection = output.reshape(1, (int)output.total() / 7);
                for (int i = 0; i < detection.rows(); ++i) {
                    final double confidence = detection.get(i, 2)[0];
                    if (confidence > CONFIDENCE_THRESHOLD) {
                        final Face face = new Face();
                        face.name = "faceZ";
                        face.x = detection.get(i, 3)[0] * frame.width();
                        face.y = detection.get(i, 4)[0] * frame.height();
                        face.width = detection.get(i, 5)[0] * frame.width() - face.x;
                        face.height = detection.get(i, 6)[0] * frame.height() - face.y;
                        face.meta = String.format(Locale.US, "conf: %f", confidence);
                        eventFaces.fireAsync(face);
                    }
                }
            }
        }
    }

}
