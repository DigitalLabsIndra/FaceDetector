/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.javafx;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import javafx.fxml.FXMLLoader;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public class FmxlLoaderProducer {

    @Inject
    Instance<Object> instance;

    /**
     * TODO: document.
     * @return TODO: document
     */
    @Produces
    public FXMLLoader createLoader() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(param -> instance.select(param).get());
        return loader;
    }
}
