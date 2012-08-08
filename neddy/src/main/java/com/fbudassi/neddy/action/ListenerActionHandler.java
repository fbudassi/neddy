package com.fbudassi.neddy.action;

import com.fbudassi.neddy.action.bean.ListenerActionBean;
import com.fbudassi.neddy.action.bean.ListenerActionBean.ListenerActionEnum;
import com.fbudassi.neddy.action.bean.ResponseBean;
import com.fbudassi.neddy.action.bean.ResponseBean.ReasonEnum;
import com.fbudassi.neddy.category.CategoryManager;
import com.fbudassi.neddy.category.ChannelGroupManager;
import com.fbudassi.neddy.category.exception.CategoryBadNameException;
import com.fbudassi.neddy.category.exception.CategoryNonExistentException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener client action handler.
 *
 * @author fbudassi
 */
public class ListenerActionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListenerActionHandler.class);

    /**
     * Handles a Listener action. It expects a Json string as a parameter and
     * returns a bean with the response.
     *
     * @param request
     * @param channel
     * @return
     */
    public static ResponseBean handleRequest(String request, Channel channel) {
        Gson gson = new Gson();
        try {
            // Check if request payload is empty.
            if (StringUtils.isBlank(request)) {
                logger.debug("Request payload is null.");
                return ResponseBean.getUnsuccessfulResponse(ReasonEnum.BAD_REQUEST);
            }

            // Deserialize request
            ListenerActionBean requestBean = gson.fromJson(request, ListenerActionBean.class);

            // Get valid action.
            ListenerActionEnum action;
            try {
                // Check if request action is empty.
                if (StringUtils.isBlank(requestBean.getAction())) {
                    logger.debug("Request action is null.");
                    return ResponseBean.getUnsuccessfulResponse(ReasonEnum.BAD_REQUEST);
                }
                action = ListenerActionEnum.valueOf(requestBean.getAction());
            } catch (IllegalArgumentException iaex) {
                // Invalid action.
                return ResponseBean.getUnsuccessfulResponse(ReasonEnum.INVALID_ACTION);
            }

            // Handle the requested action.
            switch (action) {
                case SUBSCRIBE:
                    return subscribe(requestBean, channel);
                case UNSUBSCRIBE:
                    return unsubscribe(requestBean, channel);
                case GET_CATEGORIES:
                    return getCategoryList();
                case UNSUBSCRIBE_ALL:
                    return unsubscribeFromAllCategories(channel);
                default:
                    // This point should be unreachable.
                    return ResponseBean.getUnsuccessfulResponse(ReasonEnum.INVALID_ACTION);
            }
        } catch (JsonSyntaxException jse) {
            // Json badly formatted.
            logger.debug("Error processing request.", jse);
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error processing request.", e);
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.INTERNAL_ERROR);
        }
    }

    /**
     * Subscribes a listener to a specific category.
     *
     * @param actionBean
     * @param channel
     * @return
     */
    private static ResponseBean subscribe(ListenerActionBean actionBean, Channel channel) {
        try {
            ChannelGroupManager.getInstance().addListenerToCategory(channel, actionBean.getCategory());
            ResponseBean responseBean = new ResponseBean();
            responseBean.setSuccess(true);
            responseBean.setReason(ReasonEnum.MESSAGE_CATEGORY_SUBSCRIBED.toString());
            responseBean.setMessage(String.format(ReasonEnum.MESSAGE_CATEGORY_SUBSCRIBED.getMessage(), actionBean.getCategory()));
            return responseBean;
        } catch (CategoryBadNameException cbnex) {
            logger.info("Bad category name: {}", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_BAD_NAME);
        } catch (CategoryNonExistentException cneex) {
            logger.info("Category doesn't exist: {}", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_NON_EXISTENT);
        }

    }

    /**
     * Unsubscribes a listener from a specific category.
     *
     * @param actionBean
     * @param channel
     * @return
     */
    private static ResponseBean unsubscribe(ListenerActionBean actionBean, Channel channel) {
        try {
            ChannelGroupManager.getInstance().removeListenerFromCategory(channel, actionBean.getCategory());
            ResponseBean responseBean = new ResponseBean();
            responseBean.setSuccess(true);
            responseBean.setReason(ReasonEnum.MESSAGE_CATEGORY_UNSUBSCRIBED.toString());
            responseBean.setMessage(String.format(ReasonEnum.MESSAGE_CATEGORY_UNSUBSCRIBED.getMessage(), actionBean.getCategory()));
            return responseBean;
        } catch (CategoryBadNameException cbnex) {
            logger.info("Bad category name: {}", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_BAD_NAME);
        } catch (CategoryNonExistentException cneex) {
            logger.info("Category doesn't exist: {}", actionBean.getCategory());
            return ResponseBean.getUnsuccessfulResponse(ReasonEnum.CATEGORY_NON_EXISTENT);
        }
    }

    /**
     * Gets the full list of categories.
     *
     * @return
     */
    private static ResponseBean getCategoryList() {
        Gson gson = new Gson();
        Set<String> categories = CategoryManager.getInstance().getCategoryList();
        String message = gson.toJson(categories);
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setReason(ReasonEnum.MESSAGE_CATEGORY_LIST.toString());
        responseBean.setMessage(message);
        return responseBean;
    }

    /**
     * Unsubscribes a listener from all the categories in which they're
     * subscribed.
     *
     * @param channel
     * @return
     * @throws CategoryBadNameException this should never happen
     * @throws CategoryNonExistentException this should never happen
     */
    private static ResponseBean unsubscribeFromAllCategories(Channel channel) throws CategoryBadNameException, CategoryNonExistentException {
        Set<String> categories = CategoryManager.getInstance().getCategoryList();
        for (String category : categories) {
            ChannelGroupManager.getInstance().removeListenerFromCategory(channel, category);
        }
        ResponseBean responseBean = new ResponseBean();
        responseBean.setSuccess(true);
        responseBean.setReason(ReasonEnum.MESSAGE_CATEGORY_UNSUBSCRIBED_ALL.toString());
        responseBean.setMessage(ReasonEnum.MESSAGE_CATEGORY_UNSUBSCRIBED_ALL.getMessage());
        return responseBean;
    }
}
