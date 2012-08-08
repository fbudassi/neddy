package com.fbudassi.neddy;

/**
 * An object implementing this interface has some methods to free its resources
 * gracefully.
 *
 * @author fbudassi
 */
public interface Shutdownable {

    /**
     * Allows an object to free all the resources before the application is shut
     * down completely.
     */
    public void shutdown();
}
