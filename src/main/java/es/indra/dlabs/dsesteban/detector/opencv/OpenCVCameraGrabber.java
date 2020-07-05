/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.grabber.GrabberEvent;
import es.indra.dlabs.dsesteban.detector.grabber.VideoFrame;
import es.indra.dlabs.dsesteban.detector.grabber.VideoGrabber;

/**
 * TODO: document.
 * @version 0.1
 * @since 0.1
 */
@SuppressWarnings("PMD.DoNotUseThreads")
@ApplicationScoped
public class OpenCVCameraGrabber implements VideoGrabber {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(OpenCVCameraGrabber.class);

    private static final int DEFAULT_CAMERA = 0;

    private ExecutorService captureExec;
    @Inject
    @GrabberEvent
    Event<VideoFrame> eventPlayers;
    @Inject
    @GrabberEvent
    Event<Mat> eventProcessors;
    @Inject
    @GrabberEvent
    Event<GrabberStatus> statusNotifier;

    private VideoCapture capture;
    private InternalGrabber grabber;
    private ReadWriteLock grabberLock;

    private class InternalGrabber implements Runnable {
        private final ReadWriteLock cancelLock = new ReentrantReadWriteLock();
        private boolean canceled;

        private void cancel() {
            cancelLock.writeLock().lock();
            try {
                canceled = true;
            } finally {
                cancelLock.writeLock().unlock();
            }
        }

        private boolean isCanceled() {
            boolean result;
            cancelLock.readLock().lock();
            try {
                result = canceled;
            } finally {
                cancelLock.readLock().unlock();
            }
            return result;
        }

        @Override
        public void run() {
            long id = 0;
            if (capture.open(DEFAULT_CAMERA)) {
                statusNotifier.fire(GrabberStatus.READY);
                LOG.trace("Se inicia la captura activa de imágenes");
                while (!isCanceled() && capture.isOpened()) {
                    final Mat frame = new Mat();
                    if (!isCanceled() && capture.read(frame)) {
                        if (!isCanceled()) {
                            notifyPlayers(id++, OpenCVUtils.matToBufferedImage(frame));
                            notifyProcessors(frame);
                        }
                    } else {
                        LOG.trace("Grabber cancelado o no puede hacer read");
                    }
                }
            } else {
                LOG.warn("Camera cannot be captured by OpenCV");
            }
            statusNotifier.fire(GrabberStatus.STOPPED);
            LOG.trace("Se finaliza la captura activa de imágenes");
        }
    }

    /**
     * TODO: document.
     */
    @PostConstruct
    public void init() {
        capture = new VideoCapture();
        captureExec = Executors.newSingleThreadExecutor();
        grabberLock = new ReentrantReadWriteLock();
    }

    private void enableCapturing() {
        statusNotifier.fire(GrabberStatus.INITIALIZING);
        grabberLock.writeLock().lock();
        try {
            if (grabber != null) {
                grabber.cancel();
                grabber = null;
            }
            grabber = new InternalGrabber();
            captureExec.execute(grabber);
        } finally {
            grabberLock.writeLock().unlock();
        }
    }

    private void cancelCapturing() {
        statusNotifier.fire(GrabberStatus.STOPING);
        grabberLock.writeLock().lock();
        try {
            if (grabber != null) {
                grabber.cancel();
                grabber = null;
            }
            capture.release();
        } finally {
            grabberLock.writeLock().unlock();
        }
    }

    /**
     * TODO: document.
     */
    @Override
    @PreDestroy
    public void close() {
        cancelCapturing();
        captureExec.shutdown();
    }

    @Override
    public void startCapturing() {
        LOG.trace("Se ha solicitado iniciar la captura de imágenes");
        enableCapturing();
    }

    private void notifyPlayers(final long id, final BufferedImage image) {
        if (image != null) {
            final VideoFrame videoFrame = new VideoFrame();
            videoFrame.id = id;
            videoFrame.image = image;
            eventPlayers.fireAsync(videoFrame);
        }
    }

    private void notifyProcessors(final Mat image) {
        if (image != null) {
            eventProcessors.fireAsync(image);
        }
    }

    @Override
    public void stopCapturing() {
        LOG.trace("Se ha solicitado parar la captura de imágenes");
        cancelCapturing();
    }
}
