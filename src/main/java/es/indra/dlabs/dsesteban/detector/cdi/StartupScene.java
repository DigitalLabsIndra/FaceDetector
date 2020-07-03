/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.cdi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER
})
public @interface StartupScene {

}
