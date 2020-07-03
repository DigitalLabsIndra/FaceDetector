/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.jboss.weld.environment.se.Weld;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public class Launcher extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        initializer.addProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, Boolean.FALSE);
        final SeContainer container = initializer.initialize();
        container.select(CamMonitor.class).get().start(primaryStage);
    }

    /**
     * TODO: document.
     * @param args
     *        TODO: document
     */
    @SuppressWarnings("PMD.AvoidUsingNativeCode")
    public static void main(final String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

}
