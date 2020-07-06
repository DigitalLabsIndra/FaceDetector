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

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.cdi.DetectorAction;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorInfo;
import es.indra.dlabs.dsesteban.detector.face.FaceAttribute;
import es.indra.dlabs.dsesteban.detector.face.FaceAttributesEnum;
import es.indra.dlabs.dsesteban.detector.opencv.OpenCVDetector.DetectorActions;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class MouthAttributeDetector {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MouthAttributeDetector.class);

    private static final String MOUTH_HAARCASCADE = "data/haarcascades/haarcascade_mcs_mouth.xml";
    private static final String ID = "Attribute - Cascade Mouth";

    private static final double SCALE = 1.0d;
    private static final double MARGIN = 0.5d;

    private CascadeClassifier mouthCascade;
    private boolean initialized;
    private boolean active;
    private Timer timer;

    @Inject
    @DetectorEvent
    Event<FaceAttribute> evtAttribute;
    @Inject
    @DetectorEvent
    Event<DetectorInfo> evtInfo;
    @Inject
    MeterRegistry registry;

    @PostConstruct
    void init() {
        timer = registry.timer("detector.attribute.mouth.cascade");
    }

    void initialize(@Observes @DetectorEvent final DetectorActions action) {
        switch (action) {
            case START:
                mouthCascade = new CascadeClassifier(MOUTH_HAARCASCADE);
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

    private Rect getRoi(final OpenCVFace evtFace) {
        final int x = (int)Math.max(0, evtFace.face.x - evtFace.face.width * MARGIN);
        final int y = (int)Math.max(0, evtFace.face.y - evtFace.face.height * MARGIN);
        final int width = (int)Math.min(evtFace.frame.image.width(), (x + evtFace.face.width * (1d + MARGIN))) - x;
        final int height = (int)Math.min(evtFace.frame.image.height(), (y + evtFace.face.height * (1d + MARGIN))) - y;
        return new Rect(x, y, width, height);
    }

    /**
     * TODO: document.
     * @param evtFace
     *        TODO: document
     */
    public void processImage(@ObservesAsync @DetectorEvent final OpenCVFace evtFace) {
        if (initialized && active) {
            final Instant start = Instant.now();

            final Mat cropped = new Mat(evtFace.frame.image, getRoi(evtFace));
            final Mat matRed = new Mat();
            Imgproc.resize(cropped, matRed, new Size(), SCALE, SCALE, Imgproc.INTER_AREA);
            final Mat grayFrame = new Mat();
            Imgproc.cvtColor(matRed, grayFrame, Imgproc.COLOR_BGR2GRAY);
            final MatOfRect mouths = new MatOfRect();
            mouthCascade.detectMultiScale(grayFrame, mouths, 1.7, 11);

            timer.record(Duration.between(start, Instant.now()));

            // TODO: si no está la parte de abajo del roi sería un falso positivo
            final FaceAttribute attr = new FaceAttribute();
            attr.name = evtFace.face.name;
            if (mouths.total() > 0) {
                attr.attribute = FaceAttributesEnum.MOUTH;
            } else {
                attr.attribute = FaceAttributesEnum.NO_MOUTH;
            }
            evtAttribute.fireAsync(attr);
            // temp.x = rect.x / SCALE;
            // temp.y = rect.y / SCALE;
            // temp.height = rect.height / SCALE;
            // temp.width = rect.width / SCALE;
            // temp.y = temp.y - temp.height * 0.15;
            // temp.height -= temp.height * 0.15;
        }
    }

}
