package com.fbudassi.neddy.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread is executed when the server is going to be closed. It calls the
 * shutdown method in the object passed as an argument to the constructor.
 *
 * @author federico
 */
public class ShutdownThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownThread.class);
    // The object to shutdown.
    private Shutdownable shutdownable;

    public ShutdownThread(Shutdownable object, String name) {
        super();
        this.shutdownable = object;
        this.setName(name);
    }

    @Override
    public void run() {
        logger.info("Executing shutdown on {}.", this.shutdownable.getClass().getCanonicalName());
        this.shutdownable.shutdown();
        logger.info("Shutdown successful on {}.", this.shutdownable.getClass().getCanonicalName());
    }
}
