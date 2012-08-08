package com.fbudassi.neddy.action.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Holds all the parsed information related to a Speaker's request.
 *
 * @author fbudassi
 */
public class SpeakerActionBean {

    @SerializedName("category")
    private String category;
    @SerializedName("message")
    private String message;

    /**
     * Allowed actions for a Speaker request.
     */
    public enum SpeakerActionEnum {

        ADD_CATEGORY, REMOVE_CATEGORY, SEND_MESSAGE_TO_CATEGORY, SEND_MESSAGE_TO_ALL
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

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
