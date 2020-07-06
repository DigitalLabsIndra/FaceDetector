/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import javax.annotation.PostConstruct;
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
public class DnnCaffeFaceDetector {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DnnCaffeFaceDetector.class);

    private static final String FACE_PROTO = "data/dnn/deploy.prototxt";
    private static final String FACE_CAFFE = "data/dnn/res10_300x300_ssd_iter_140000.caffemodel";
    private static final String ID = "Detector - Dnn Caffe";
    private static final double CONFIDENCE_THRESHOLD = 0.6;

    private static final Duration LAPSUS = Duration.ofMillis(500);

    private Net net;
    private Instant lastProcessed = Instant.MIN;
    private boolean initialized;
    private boolean active;
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
        timer = registry.timer("detector.face.dnn.tensorflow");
    }

    void initialize(@Observes @DetectorEvent final DetectorActions action) {
        switch (action) {
            case START:
                net = Dnn.readNetFromCaffe(FACE_PROTO, FACE_CAFFE);
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
    public void processImage(@ObservesAsync @GrabberEvent final OpenCVVideoFrame frame) {
        if (initialized && active) {
            final Instant now = Instant.now();
            if (Duration.between(lastProcessed, now).compareTo(LAPSUS) > 0) {
                lastProcessed = now;

                // Según el modelo de entrenamiento hay que filtrar con ese escalar y en ese escalado
                // TOOD: pasar estos datos a constantes asociadas al modelo y no directament en esta partde del código
                net.setInput(Dnn.blobFromImage(frame.image, 1.0d, new Size(300, 300),
                    new Scalar(104.0, 177.0, 123.0, 0), false, false));
                final Mat output = net.forward();

                timer.record(Duration.between(now, Instant.now()));

                int j = 0;
                final Mat detection = output.reshape(1, (int)output.total() / 7);
                for (int i = 0; i < detection.rows(); ++i) {
                    final double confidence = detection.get(i, 2)[0];
                    if (confidence > CONFIDENCE_THRESHOLD) {
                        final Face face = new Face();
                        face.name = "face " + j + " Y";
                        face.x = detection.get(i, 3)[0] * frame.image.width();
                        face.y = detection.get(i, 4)[0] * frame.image.height();
                        face.width = detection.get(i, 5)[0] * frame.image.width() - face.x;
                        face.height = detection.get(i, 6)[0] * frame.image.height() - face.y;
                        face.meta = String.format(Locale.US, "conf: %f", confidence);
                        face.frameId = frame.id;
                        face.processor = FaceProcessors.DETECTOR;
                        eventFaces.fireAsync(face);
                        j++;
                        final OpenCVFace cvFace = new OpenCVFace();
                        cvFace.face = face;
                        cvFace.frame = frame;
                        cvEventFaces.fireAsync(cvFace);
                    }
                }
            }
        }
    }

}
