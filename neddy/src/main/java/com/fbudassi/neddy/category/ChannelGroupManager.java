package com.fbudassi.neddy.category;

import com.fbudassi.neddy.category.exception.CategoryBadNameException;
import com.fbudassi.neddy.category.exception.CategoryNonExistentException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

/**
 * It manages the relations between the WebSocket channels and the categories
 * created. It also maintains its own list of all the WebSocket opened channels
 * so far to notify from different events (like category creation or removal).
 *
 * @author fbudassi
 */
public class ChannelGroupManager {

    private static ChannelGroupManager INSTANCE = new ChannelGroupManager();
    private ChannelGroup allChannels;

    /**
     * Private constructor due to Singleton pattern usage.
     */
    private ChannelGroupManager() {
        allChannels = new DefaultChannelGroup("all-listeners");
    }

    /**
     * Gets the unique CategoryManager instance.
     *
     * @return
     */
    public static ChannelGroupManager getInstance() {
        return INSTANCE;
    }

    /**
     * Avoid object cloning by overriding the clone() method.
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Adds a new listener to a category. If the listener is already subscribed
     * to the category, nothing happens. If the category doesn't exist, an
     * exception is thrown.
     *
     * @param channel
     * @param category
     * @throws CategoryNonExistentException
     * @throws CategoryBadNameException
     */
    public void addListenerToCategory(Channel channel, String category) throws CategoryNonExistentException, CategoryBadNameException {
        if (CategoryManager.getInstance().categoryExists(category)) {
            getAllChannels().add(channel);
            CategoryManager.getInstance().getCategory(category).add(channel);
        } else {
            throw new CategoryNonExistentException();
        }
    }

    /**
     * Remove a Listener from the selected category. If the category doesn't
     * exist, an exception is thrown.
     *
     * @param channel
     * @param category
     * @throws CategoryNonExistentException
     * @throws CategoryBadNameException
     */
    public void removeListenerFromCategory(Channel channel, String category) throws CategoryNonExistentException, CategoryBadNameException {
        if (CategoryManager.getInstance().categoryExists(category)) {
            CategoryManager.getInstance().getCategory(category).remove(channel);
        } else {
            throw new CategoryNonExistentException();
        }
    }

    /**
     * Get a ChannelGroup with all the WebSocket channels currently connected.
     *
     * @return the allChannels
     */
    public ChannelGroup getAllChannels() {
        return allChannels;
    }
}
