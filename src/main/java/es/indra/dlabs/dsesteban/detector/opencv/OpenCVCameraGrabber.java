/*
 * Copyright (C) 2020 INDRA FACTORÍA TECNOLÓGICA S.L.U.
 * All rights reserved
 **/
package es.indra.dlabs.dsesteban.detector.opencv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.indra.dlabs.dsesteban.detector.VideoGrabber;
import es.indra.dlabs.dsesteban.detector.VideoPlayer;
import javafx.scene.image.Image;

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

    private ExecutorService broadcaster;
    private ExecutorService captureExec;

    private VideoCapture capture;
    private List<VideoPlayer> players;
    private ReadWriteLock playersLock;
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
            LOG.trace("Se inicia la captura activa de imágenes");
            while (!isCanceled() && capture.isOpened()) {
                final Mat frame = new Mat();
                if (!isCanceled() && capture.read(frame)) {
                    LOG.trace("Image readed");
                    if (!isCanceled()) {
                        notifyPlayers(OpenCVUtils.mat2Image(frame));
                    }
                } else {
                    LOG.trace("Grabber cancelado o no puede hacer read");
                }
            }
            LOG.trace("Se finaliza la captura activa de imágenes");
        }
    }

    /**
     * TODO: document.
     */
    @PostConstruct
    public void init() {
        capture = new VideoCapture();
        players = new ArrayList<>();
        playersLock = new ReentrantReadWriteLock();
        broadcaster = Executors.newCachedThreadPool();
        captureExec = Executors.newSingleThreadExecutor();
        grabberLock = new ReentrantReadWriteLock();
    }

    private boolean enableCapturing() {
        boolean result = false;
        grabberLock.writeLock().lock();
        try {
            if (grabber != null) {
                grabber.cancel();
                grabber = null;
            }
            if (capture.open(DEFAULT_CAMERA)) {
                grabber = new InternalGrabber();
                captureExec.execute(grabber);
                result = true;
            } else {
                LOG.warn("Camera cannot be captured by OpenCV");
            }
        } finally {
            grabberLock.writeLock().unlock();
        }
        return result;
    }

    private void cancelCapturing() {
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
    public void close() {
        cancelCapturing();
        captureExec.shutdown();
        broadcaster.shutdown();
    }

    @Override
    public void startCapturing(final VideoPlayer player) {
        LOG.trace("Se ha solicitado añadir un videoplayer: {}", player);
        boolean initCapture = false;
        playersLock.writeLock().lock();
        try {
            if (players.isEmpty()) {
                initCapture = true;
            }
            players.add(player);
        } finally {
            playersLock.writeLock().unlock();
        }
        if (initCapture) {
            enableCapturing();
        }
    }

    private void notifyPlayers(final Image image) {
        if (image != null) {
            playersLock.readLock().lock();
            try {
                players.forEach((videoPlayer) -> broadcaster.execute(() -> {
                    videoPlayer.showImage(image);
                }));
            } finally {
                playersLock.readLock().unlock();
            }
        }
    }

    @Override
    public void stopCapturing(final VideoPlayer player) {
        LOG.trace("Se ha solicitado quitar un videoplayer: {}", player);
        boolean stopCapture = false;
        playersLock.writeLock().lock();
        try {
            players.remove(player);
            if (players.isEmpty()) {
                stopCapture = true;
            }
        } finally {
            playersLock.writeLock().unlock();
        }
        if (stopCapture) {
            LOG.trace("Ningún player disponible se finaliza la captura");
            cancelCapturing();
            LOG.trace("Object OpenCV released");
        }
    }

    /**
     * TODO: document.
     */
    public void reset() {
        cancelCapturing();
        synchronized (players) {
            players.clear();
        }
    }

}
