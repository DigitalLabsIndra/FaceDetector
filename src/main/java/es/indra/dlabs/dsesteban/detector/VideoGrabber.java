/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public interface VideoGrabber extends AutoCloseable {
    
    /**
     * TODO: document.
     * @version 0.1
     * @since 0.1
     */
    enum GrabberStatus {
        /** El grabber está inicializándose y puede no responder en un periodo. */
        INITIALIZING,
        /** El grabber funcionando y extrayendo imágenes. */
        READY,
        /** El grabber está parando la obtención de imágenes y puede no responder en un periodo. */
        STOPING,
        /** El grabber está parado. */
        STOPPED;
    }

    /**
     * TODO: document.
     * @param player
     *        TODO: document
     */
    void startCapturing();

    /**
     * TODO: document.
     * @param player
     *        TODO: document
     */
    void stopCapturing();

}
