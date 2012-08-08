package com.fbudassi.neddy.action;

import com.fbudassi.neddy.action.bean.ResponseBean;
import com.fbudassi.neddy.action.bean.ResponseBean.ReasonEnum;
import com.fbudassi.neddy.action.bean.SpeakerActionBean;
import com.fbudassi.neddy.action.bean.SpeakerActionBean.SpeakerActionEnum;
import com.fbudassi.neddy.category.CategoryManager;
import com.fbudassi.neddy.category.ChannelGroupManager;
import com.fbudassi.neddy.category.exception.CategoryAlreadyExistsException;
import com.fbudassi.neddy.category.exception.CategoryBadNameException;
import com.fbudassi.neddy.handler.expert.WebSocketExpert;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Speaker actions handler.
 *
 * @author fbudassi
 */
public class SpeakerActionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SpeakerActionHandler.class);

    /**
     * Handles a Speaker's action. It expects a Json string as a parameter and
     * returns a json answer.
     *
     * @param request
     * @param action
     * @return
     */
    public static ResponseBean handleRequest(String request, SpeakerActionEnum action) {
        Gson gson = new Gson();
        try {
            // Check if request payload in empty.
            if (StringUtils.isBlank(request)) {
                logger.debug("Request payload is null");
                return ResponseBean.getUnsuccessfulResponse(ReasonEnum.BAD_REQUEST);
            }

            // Deserialize request
            SpeakerActionBean requestBean = gson.fromJson(request, SpeakerActionBean.class);

            // Handle the requested action.
            switch (action) {
                case ADD_CATEGORY:
                    return addCategory(requestBean);
                case REMOVE_CATEGORY:
                    return removeCategory(requestBean);
                case SEND_MESSAGE_TO_CATEGORY:
                    return sendMessageToCategory(requestBean);
                case SEND_MESSAGE_TO_ALL:
                    return sendMessageToAll(requestBean);
                default:
                    // This point should be unreachable.
                    return ResponseBean.getUnsuccessfulResponse(ResponseBean.ReasonEnum.INVALID_ACTION);
            }
        } catch (JsonSyntaxException jse) {
            // Json badly formatted.
            logger.debug("Error processing request.", jse);
            return ResponseBean.getUnsuccessfulResponse(ResponseBean.ReasonEnum.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error processing request.", e);
            return ResponseBean.getUnsuccessfulResponse(ResponseBean.ReasonEnum.INTERNAL_ERROR);
        }
    }

    /**
     * Adds a new category to the list.
     *
     * @param actionBean
     * @return
     */
    private static ResponseBean addCategory(SpeakerActionBean actionBean) {
        try {
            // Try to add the new category.
            CategoryManager.getInstance().addCategory(actionBean.getCategory());

            // Send a New Category notification to all the listeners.
            sendCategoryEventMessage(ChannelGroupManager.getInstance().getAllChannels(),
                        ReasonEnum.MESSAGE_CATEGORY_ADDED, actionBean.getCategory());

            return ResponseBean.getSuccessfulResponse();
        } catch (CategoryAlreadyExistsException caeex) {
            logger.info("Category {} already exists.", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_ALREADY_EXISTS);
        } catch (CategoryBadNameException cbnex) {
            logger.info("Bad category name: {}", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_BAD_NAME);
        }
    }

    /**
     * Removes a category from the list, together with all its listeners.
     *
     * @param actionBean
     * @return
     */
    private static ResponseBean removeCategory(SpeakerActionBean actionBean) {
        try {
            // Try to remove the category.
            ChannelGroup group = CategoryManager.getInstance().removeCategory(actionBean.getCategory());
            if (group == null) {
                // Category doesn't exist.
                return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_NON_EXISTENT);
            } else {
                // Send a notification to all the WebSocket channels.
                sendCategoryEventMessage(ChannelGroupManager.getInstance().getAllChannels(),
                        ReasonEnum.MESSAGE_CATEGORY_REMOVED, actionBean.getCategory());
            }
            return ResponseBean.getSuccessfulResponse();
        } catch (CategoryBadNameException cbnex) {
            logger.info("Bad category name: {}", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_BAD_NAME);
        }
    }

    /**
     * Sends a message to only one category.
     *
     * @param actionBean
     * @return
     */
    private static ResponseBean sendMessageToCategory(SpeakerActionBean actionBean) {
        try {
            // Check if category exists.
            if (!CategoryManager.getInstance().categoryExists(actionBean.getCategory())) {
                return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_NON_EXISTENT);
            }

            ChannelGroup group = CategoryManager.getInstance().getCategory(actionBean.getCategory());
            ResponseBean responseBean = ResponseBean.getNewMessageResponse(actionBean.getMessage(), actionBean.getCategory());
            sendMessage(group, responseBean);
            return ResponseBean.getSuccessfulResponse();
        } catch (CategoryBadNameException cbnex) {
            logger.info("Bad category name: {}", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_BAD_NAME);
        }
    }

    /**
     * Sends a message to all the registered listeners in any category.
     *
     * @param actionBean
     * @return
     */
    private static ResponseBean sendMessageToAll(SpeakerActionBean actionBean) {
        ResponseBean responseBean = ResponseBean.getNewMessageResponse(actionBean.getMessage(), "All Categories");
        sendMessage(ChannelGroupManager.getInstance().getAllChannels(), responseBean);
        return ResponseBean.getSuccessfulResponse();
    }

    /**
     * Sends a Category Event message to all the listeners in the group passed
     * as parameter.
     *
     * @param group
     */
    private static void sendCategoryEventMessage(ChannelGroup group, ReasonEnum reasonEnum, String category) {
        // Only allow some ReasonEnum values.
        if (reasonEnum != ReasonEnum.MESSAGE_CATEGORY_ADDED && reasonEnum != ReasonEnum.MESSAGE_CATEGORY_REMOVED) {
            throw new UnsupportedOperationException();
        }

        //Send Category event message to group.
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setReason(reasonEnum.toString());
        responseBean.setMessage(String.format(reasonEnum.getMessage(), category));
        responseBean.setCategory(category);
        sendMessage(group, responseBean);
    }

    /**
     * It sends a message t
     *
     * @param group
     * @param message
     */
    private static void sendMessage(ChannelGroup group, ResponseBean responseBean) {
        Gson gson = new Gson();
        group.write(WebSocketExpert.getTextWebSocketFrame(gson.toJson(responseBean)));
    }
}
