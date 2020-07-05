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
public class DetectorAction {

    /**
     * TODO: document.
     * @version 0.1
     * @since 0.1
     */
    public enum DetectorActions {
        /** Se solicita que se active el detector. */
        START,
        /** Se solicita que se pare el detector. */
        STOP;
    }

    /**
     * TODO: document.
     */
    public String id;
    /**
     * TODO: document.
     */
    public DetectorActions action;

    /**
     * TODO: document.
     * @param id
     *        TODO: document
     * @param action
     *        TODO: document
     */
    public DetectorAction(final String id, final DetectorActions action) {
        super();
        this.id = id;
        this.action = action;
    }
}
