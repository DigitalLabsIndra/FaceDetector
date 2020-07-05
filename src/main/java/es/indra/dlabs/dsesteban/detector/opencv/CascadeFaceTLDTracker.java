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
import org.opencv.core.Rect2d;
import org.opencv.tracking.TrackerTLD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.cdi.DetectorAction;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorInfo;
import es.indra.dlabs.dsesteban.detector.face.Face;
import es.indra.dlabs.dsesteban.detector.grabber.GrabberEvent;
import es.indra.dlabs.dsesteban.detector.opencv.OpenCVDetector.DetectorActions;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class CascadeFaceTLDTracker {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CascadeFaceTLDTracker.class);

    private static final String ID = "Cascade TLD Tracker";
    private static final int TICKS_COUNT = 40;

    private static final Duration LAPSUS = Duration.ofMillis(100);

    private Instant lastProcessed = Instant.MIN;
    private Duration accumulator = Duration.ZERO;
    private int ticks;
    private boolean initialized;
    private boolean active;
    private TrackerTLD tracker;
    private Rect2d roi;
    private boolean tracking;

    @Inject
    @DetectorEvent
    Event<Face> eventFaces;
    @Inject
    @DetectorEvent
    Event<DetectorInfo> evtInfo;

    void initialize(@Observes @DetectorEvent final DetectorActions action) {
        switch (action) {
            case START:
                tracker = TrackerTLD.create();
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
                    roi = null;
                    tracking = false;
                    LOG.info("{} has been deactivated", ID);
                    break;
                default:
            }
        }
    }

    /**
     * TODO: document.
     * @param face
     *        TODO: document
     */
    public void receivedROI(@ObservesAsync @DetectorEvent final Face face) {
        if ((roi == null) && ("faceX 0".equals(face.name))) {
            roi = new Rect2d(face.x, face.y, face.width, face.height);
        }
    }

    /**
     * TODO: document.
     * @param frame
     *        TODO: document
     */
    public void processImage(@ObservesAsync @GrabberEvent final Mat frame) {
        if (initialized && active && (roi != null)) {
            if (!tracking) {
                tracking = true;
                tracker.init(frame, roi);
            }
            final Instant now = Instant.now();
            if (Duration.between(lastProcessed, now).compareTo(LAPSUS) > 0) {
                lastProcessed = now;

                @SuppressWarnings("PMD.LocalVariableCouldBeFinal")
                Rect2d rect = new Rect2d();
                final boolean found = tracker.update(frame, rect);

                final Instant after = Instant.now();
                accumulator = accumulator.plus(Duration.between(now, after));
                ticks++;
                if (ticks > TICKS_COUNT) {
                    LOG.trace("Media procesamiento: {}", accumulator.toMillis() / ticks);
                    accumulator = Duration.ZERO;
                    ticks = 0;
                }

                // TODO: un tracker por cara de un detector
                if (found) {
                    final Face face = new Face();
                    face.name = "faceV 0";
                    face.meta = "Tracking TLD Cascade";
                    face.x = rect.x;
                    face.y = rect.y;
                    face.height = rect.height;
                    face.width = rect.width;
                    eventFaces.fireAsync(face);
                }
            }
        }
    }

}
