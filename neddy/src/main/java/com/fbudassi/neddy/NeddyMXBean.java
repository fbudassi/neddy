package com.fbudassi.neddy;

import javax.management.MXBean;

/**
 * Java Management Extensions Bean (MXBean) useful for monitoring the server
 * state.
 *
 * @author fbudassi
 */
@MXBean
public interface NeddyMXBean {

    /**
     * Gets the number of clients open connections currently opened.
     *
     * @return
     */
    int getOpenClientConnectionsNumber();
}
