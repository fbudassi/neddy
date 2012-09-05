package com.fbudassi.neddy.benchmark.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Default fields for a Neddy message to both Listeners and Pushers.
 *
 * @author fbudassi
 */
public class NeddyBean {

    @SerializedName("success")
    private boolean success;
    @SerializedName("reason")
    private String reason;
    @SerializedName("message")
    private String message;
    @SerializedName("category")
    private String category;

    /**
     * Enumeration with all the possible reasons when the request is being
     * processed.
     */
    public enum ReasonEnum {

        // Error Reasons.
        OK,
        BAD_REQUEST,
        INTERNAL_ERROR,
        INVALID_ACTION,
        CATEGORY_BAD_NAME,
        CATEGORY_NON_EXISTENT,
        // Message Reasons.
        MESSAGE_NEW,
        MESSAGE_CATEGORY_REMOVED,
        MESSAGE_CATEGORY_ADDED,
        MESSAGE_CATEGORY_SUBSCRIBED,
        MESSAGE_CATEGORY_UNSUBSCRIBED,
        MESSAGE_CATEGORY_UNSUBSCRIBED_ALL,
        MESSAGE_CATEGORY_LIST;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the errorReason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
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
