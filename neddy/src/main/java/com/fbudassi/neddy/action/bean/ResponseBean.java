package com.fbudassi.neddy.action.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Default fields for a Neddy answer to both Listeners and Pushers.
 *
 * @author fbudassi
 */
public class ResponseBean {

    @SerializedName("success")
    private boolean success;
    @SerializedName("reason")
    private String reason;
    @SerializedName("message")
    private String message;
    @SerializedName("category")
    private String category;

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
     * Enumeration with all the possible errors when the request is being
     * processed.
     */
    public enum ReasonEnum {

        // Error Reasons.
        OK("Operation successful."),
        BAD_REQUEST("Request is incorrectly formatted or inconsistent."),
        INTERNAL_ERROR("Internal server error."),
        INVALID_ACTION("The action is invalid or it doesn't exists."),
        CATEGORY_BAD_NAME("The name of the category doesn't fit to the requirements"),
        CATEGORY_NON_EXISTENT("Selected category doesn't exist."),
        CATEGORY_ALREADY_EXISTS("The category that you're trying to add already exists in the server"),
        // Message Reasons.
        MESSAGE_NEW(""),
        MESSAGE_CATEGORY_REMOVED("Category %s was removed."),
        MESSAGE_CATEGORY_ADDED("Category %s was added."),
        MESSAGE_CATEGORY_SUBSCRIBED("Subscribed to category %s."),
        MESSAGE_CATEGORY_UNSUBSCRIBED("Unsubscribed from category %s."),
        MESSAGE_CATEGORY_UNSUBSCRIBED_ALL("Unsubscribed from all categories."),
        MESSAGE_CATEGORY_LIST("");
        // Variable to store the error message once an enum is instantiated.
        private final String message;

        ReasonEnum(String messsage) {
            this.message = messsage;
        }

        /**
         * A getter for the reason message.
         *
         * @return
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Utility method to get a successful response bean with no errors;
     *
     * @return
     */
    public static ResponseBean getSuccessfulResponse() {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setReason(ReasonEnum.OK.toString());
        responseBean.setMessage(ReasonEnum.OK.getMessage());
        return responseBean;
    }

    /**
     * Utility method to get an unsuccessful response bean based in the reason
     * passed as parameter.
     *
     * @param reasonEnum
     * @return
     */
    public static ResponseBean getUnsuccessfulResponse(ReasonEnum reasonEnum) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(false);
        responseBean.setReason(reasonEnum.toString());
        responseBean.setMessage(reasonEnum.getMessage());
        return responseBean;
    }

    /**
     * Utility method to generate a New Push Message response to be sent to the
     * listeners.
     *
     * @param message
     * @return
     */
    public static ResponseBean getNewMessageResponse(String message, String category) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setReason(ReasonEnum.MESSAGE_NEW.toString());
        responseBean.setMessage(message);
        responseBean.setCategory(category);
        return responseBean;
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
}
