/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opencv.core.Rect2d;
import org.opencv.tracking.TrackerTLD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.cdi.DetectorAction;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorInfo;
import es.indra.dlabs.dsesteban.detector.face.Face;
import es.indra.dlabs.dsesteban.detector.face.FaceProcessors;
import es.indra.dlabs.dsesteban.detector.grabber.GrabberEvent;
import es.indra.dlabs.dsesteban.detector.opencv.OpenCVDetector.DetectorActions;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class FaceTLDTracker {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FaceTLDTracker.class);

    private static final String ID = "Tracker - TLD";

    private static final Duration LAPSUS = Duration.ofMillis(100);

    private Instant lastProcessed = Instant.MIN;
    private boolean initialized;
    private boolean active;
    private TrackerTLD tracker;
    private boolean tracking;
    private Timer timer;

    @Inject
    @DetectorEvent
    Event<Face> eventFaces;
    @Inject
    @DetectorEvent
    Event<OpenCVFace> cvEventFaces;
    @Inject
    @DetectorEvent
    Event<DetectorInfo> evtInfo;
    @Inject
    MeterRegistry registry;

    @PostConstruct
    void init() {
        timer = registry.timer("tracker.tld");
    }

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
                    tracking = false;
                    LOG.info("{} has been deactivated", ID);
                    break;
                default:
            }
        }
    }

    /**
     * TODO: document.
     * @param evtFace
     *        TODO: document
     */
    public void receivedROI(@ObservesAsync @DetectorEvent final OpenCVFace evtFace) {
        if (initialized && active && !tracking) {
            if (FaceProcessors.DETECTOR.equals(evtFace.face.processor) && (evtFace.face.name.startsWith("face 0"))) {
                LOG.debug("Tracker recibe su primer ROI con el que trabajar");
                tracker.init(evtFace.frame.image, new Rect2d(evtFace.face.x, evtFace.face.y, evtFace.face.width,
                    evtFace.face.height));
                tracking = true;
            }
        }
    }

    /**
     * TODO: document.
     * @param frame
     *        TODO: document
     */
    public void processImage(@ObservesAsync @GrabberEvent final OpenCVVideoFrame frame) {
        if (initialized && active && tracking) {
            final Instant now = Instant.now();
            if (Duration.between(lastProcessed, now).compareTo(LAPSUS) > 0) {
                lastProcessed = now;

                @SuppressWarnings("PMD.LocalVariableCouldBeFinal")
                Rect2d rect = new Rect2d();
                final boolean found = tracker.update(frame.image, rect);

                timer.record(Duration.between(now, Instant.now()));

                // TODO: un tracker por cara de un detector
                if (found) {
                    final Face face = new Face();
                    face.name = "face 0V";
                    face.meta = "Tracking TLD Cascade";
                    face.x = rect.x;
                    face.y = rect.y;
                    face.height = rect.height;
                    face.width = rect.width;
                    face.frameId = frame.id;
                    face.processor = FaceProcessors.TRACKING;
                    eventFaces.fireAsync(face);
                    final OpenCVFace cvFace = new OpenCVFace();
                    cvFace.face = face;
                    cvFace.frame = frame;
                    cvEventFaces.fireAsync(cvFace);
                }
            }
        }
    }

}
