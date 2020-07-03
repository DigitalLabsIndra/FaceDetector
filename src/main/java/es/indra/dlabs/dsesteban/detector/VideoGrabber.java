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
     * @param player
     *        TODO: document
     */
    void startCapturing(final VideoPlayer player);

    /**
     * TODO: document.
     * @param player
     *        TODO: document
     */
    void stopCapturing(final VideoPlayer player);

}
