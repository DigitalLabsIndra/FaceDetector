/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.cdi.Detector;
import es.indra.dlabs.dsesteban.detector.cdi.DetectorEvent;
import es.indra.dlabs.dsesteban.detector.opencv.OpenCVDetector.DetectorActions;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class OpenCVDetectors implements Detector {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(OpenCVDetectors.class);

    private boolean platformInit;

    @Inject
    CascadeFaceDetector faceDetector;
    @Inject
    OpenCVCameraGrabber grabber;
    @Inject
    @DetectorEvent
    Event<Detector.PlatformStatus> evtPlatform;
    @Inject
    @DetectorEvent
    Event<OpenCVDetector.DetectorActions> evtDetector;

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    void start(@ObservesAsync @DetectorEvent final PlatformActions event) {
        LOG.trace("Evento de plataforma recibido en detectores OpenCV: {}", event);
        switch (event) {
            case START:
                if (!platformInit) {
                    LOG.debug("Inicializando plataforma OpenCV");
                    evtPlatform.fire(PlatformStatus.INITIALIZING);
                    try {
                        Loader.load(opencv_java.class);
                        evtDetector.fire(DetectorActions.START);
                        platformInit = true;
                        LOG.debug("Plataforma OpenCV inicializada");
                        evtPlatform.fire(PlatformStatus.READY);
                    } catch (Throwable t) {
                        LOG.error("Error cargando plataforma: {}", t.getMessage());
                        LOG.debug(t.getMessage(), t);
                        platformInit = false;
                        evtPlatform.fire(PlatformStatus.ERROR);
                    }
                }
                break;
            case STOP:
            default:
        }
    }

}
