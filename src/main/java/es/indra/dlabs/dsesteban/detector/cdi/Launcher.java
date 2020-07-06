/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.cdi;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.Weld;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
public class Launcher extends Application {

    @SuppressWarnings({
        "PMD.CloseResource", "serial"
    })
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        initializer.addProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, Boolean.FALSE);
        final SeContainer container = initializer.initialize();
        container.getBeanManager().fireEvent(primaryStage, new AnnotationLiteral<StartupScene>() {});
    }

    /**
     * TODO: document.
     * @param args
     *        TODO: document
     */
    public static void main(final String[] args) {
        Metrics.addRegistry(new SimpleMeterRegistry());
        launch(args);
    }

}
