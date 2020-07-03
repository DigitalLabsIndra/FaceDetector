/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.opencv.core.Core;

import es.indra.dlabs.dsesteban.detector.cdi.Detector;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@ApplicationScoped
public class OpenCVDetectors implements Detector {

    @Inject
    FaceDetector faceDetector;
    @Inject
    OpenCVCameraGrabber grabber;

    /**
     * TODO: document.
     */
    @SuppressWarnings("PMD.AvoidUsingNativeCode")
    public OpenCVDetectors() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    void start(@Observes @Initialized(ApplicationScoped.class) final Object init) {
    }

}
