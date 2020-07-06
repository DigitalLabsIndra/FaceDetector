/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.metrics;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Singleton
public class MetricsRegistryProducer {

    private MeterRegistry registry;

    @PostConstruct
    void init() {
        registry = new SimpleMeterRegistry();
    }

    /**
     * TODO: document.
     * @return TODO: document
     */
    @Produces
    public MeterRegistry getRegistry() {
        return registry;
    }

}
