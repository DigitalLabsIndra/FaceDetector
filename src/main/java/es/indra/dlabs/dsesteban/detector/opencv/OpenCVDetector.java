/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public interface OpenCVDetector {

    /**
     * TODO: document.
     * @version 0.1
     * @since 0.1
     */
    enum DetectorActions {
        /** Se solicita que se inicialice el detector. */
        START,
        /** Se solicita que se finalice el detector. */
        STOP;
    }

}
