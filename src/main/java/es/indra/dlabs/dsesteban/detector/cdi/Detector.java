/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.cdi;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public interface Detector {

    /**
     * TODO: document.
     * @version 0.1
     * @since 0.1
     */
    enum PlatformStatus {
        /** La plataforma está inicializándose y puede no responder en un periodo. */
        INITIALIZING,
        /** La plataforma está lista para ser usada. */
        READY,
        /** La plataforma se ha cargado con errores y no puede ser usada. */
        ERROR,
        /** La plataforma está parando. */
        STOPING,
        /** El plataforma está parada. */
        STOPPED;
    }

    /**
     * TODO: document.
     * @version 0.1
     * @since 0.1
     */
    enum PlatformActions {
        /** Se solicita que se inicialice la plataforma. */
        START,
        /** Se solicita que se finalice la plataforma. */
        STOP;
    }

}
