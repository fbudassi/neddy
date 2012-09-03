package com.fbudassi.neddy.benchmark.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Holds all the parsed information related to a Listener's request.
 *
 * @author fbudassi
 */
public class ListenerActionBean {

    @SerializedName("action")
    private String action;
    @SerializedName("category")
    private String category;

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
