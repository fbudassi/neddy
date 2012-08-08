package com.fbudassi.neddy.category;

import com.fbudassi.neddy.category.exception.CategoryAlreadyExistsException;
import com.fbudassi.neddy.category.exception.CategoryBadNameException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

/**
 * It manages all the categories added and their relations with subscribed
 * channels. It does it with an internal store that is freed when the application
 * shuts down (so, no persistence so far).
 *
 * @author fbudassi
 */
public class CategoryManager {

    private static CategoryManager INSTANCE = new CategoryManager();
    private Map<String, ChannelGroup> categoryMap;

    /**
     * Private constructor due to Singleton pattern usage.
     */
    private CategoryManager() {
        categoryMap = new ConcurrentHashMap<String, ChannelGroup>();
    }

    /**
     * Gets the unique CategoryManager instance.
     *
     * @return
     */
    public static CategoryManager getInstance() {
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
     * Adds a new category with an empty ChannelGroup to the Category Map.
     *
     * @param category
     * @throws CategoryAlreadyExistsException
     * @throws CategoryBadNameException
     */
    public void addCategory(String category) throws CategoryAlreadyExistsException, CategoryBadNameException {
        // First check for category name validity.
        if (isNotValidName(category)) {
            throw new CategoryBadNameException();
        }

        // Then try to add the category if it doesn't exist.
        if (!categoryExists(category)) {
            categoryMap.put(category, new DefaultChannelGroup(category));
        } else {
            throw new CategoryAlreadyExistsException();
        }
    }

    /**
     * Removes an existing from the Category Map. It returns null if the
     * category doesn't exist, or the ChannelGroup if it existed.
     *
     * @param category
     * @return
     * @throws CategoryBadNameException
     */
    public ChannelGroup removeCategory(String category) throws CategoryBadNameException {
        // First check for category name validity.
        if (isNotValidName(category)) {
            throw new CategoryBadNameException();
        }

        return categoryMap.remove(category);
    }

    /**
     * Returns a list with all the Categories loaded in the internal store.
     *
     * @return
     */
    public Set<String> getCategoryList() {
        return categoryMap.keySet();
    }

    /**
     * Gets the ChannelGroup associated to the category passed as parameter. It
     * returns null of the category doesn't exist.
     *
     * @param category
     * @return
     * @throws CategoryBadNameException
     */
    public ChannelGroup getCategory(String category) throws CategoryBadNameException {
        // First check for category name validity.
        if (isNotValidName(category)) {
            throw new CategoryBadNameException();
        }

        return categoryMap.get(category);
    }

    /**
     * Checks for a category existence.
     *
     * @param category
     * @return
     * @throws CategoryBadNameException
     */
    public boolean categoryExists(String category) throws CategoryBadNameException {
        // First check for category name validity.
        if (isNotValidName(category)) {
            throw new CategoryBadNameException();
        }

        return categoryMap.containsKey(category);
    }

    /**
     * Checks if a category name is valid or not.
     *
     * @param categoryName
     * @return
     */
    private boolean isNotValidName(String categoryName) {
        return categoryName == null || categoryName.trim().isEmpty();
    }
}
