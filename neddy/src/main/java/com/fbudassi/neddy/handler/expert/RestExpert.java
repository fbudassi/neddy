package com.fbudassi.neddy.handler.expert;

import com.fbudassi.neddy.action.SpeakerActionHandler;
import com.fbudassi.neddy.action.bean.ResponseBean;
import com.fbudassi.neddy.action.bean.ResponseBean.ReasonEnum;
import com.fbudassi.neddy.action.bean.SpeakerActionBean.SpeakerActionEnum;
import com.fbudassi.neddy.config.Config;
import com.fbudassi.neddy.handler.HandlerUtil;
import com.fbudassi.neddy.util.DateUtil;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that understand how to speak with Speakers in a REST way.
 *
 * @author fbudassi
 */
public class RestExpert extends CommunicationExpert {

    private static final Logger logger = LoggerFactory.getLogger(RestExpert.class);

    /**
     * Resources and paths enumeration.
     */
    private enum ResourceEnum {

        CATEGORY(Config.getValue(Config.KEY_RESOURCE_CATEGORY)),
        LISTENERS(Config.getValue(Config.KEY_RESOURCE_LISTENERS));
        //Internal methods and variables to search by resource path.
        String resourcePath;
        private static final Map<String, ResourceEnum> lookup = new HashMap<String, ResourceEnum>();

        /**
         * The Classloader will load the enum at compilation time. With this
         * static constructor we initialize this map with the resourcePath as
         * key and the enum as the value, so when we want to obtain the enum
         * from a resourcePath this task will be quicker.
         */
        static {
            for (ResourceEnum resourceEnum : values()) {
                lookup.put(resourceEnum.getResourcePath(), resourceEnum);
            }
        }

        private ResourceEnum(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        private String getResourcePath() {
            return resourcePath;
        }

        /**
         * It returns an enum based on a resource path. If it doesn't find
         * anything that matches the path, it returns null.
         *
         * @param resourcePath
         * @return
         */
        public static ResourceEnum getResourceEnumByResourcePath(String resourcePath) {
            ResourceEnum result = null;
            if (lookup.containsKey(resourcePath)) {
                result = lookup.get(resourcePath);
            }
            return result;
        }
    }

    // Allowed Http Methods for resources.
    private enum CategoryHttpMethodEnum {

        OPTIONS, PUT, DELETE, POST
    }

    private enum ListenersHttpMethodEnum {

        OPTIONS, POST
    }

    /**
     * It handles REST requests.
     *
     * @param ctx
     * @param messageEvent
     * @throws Exception
     */
    @Override
    public void handleRequest(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        HttpRequest request = (HttpRequest) messageEvent.getMessage();

        // Log debug info.
        logRequestInfo(logger, ctx.getChannel(), request);

        // First we need to analyze the URI to look for an existing REST resource.
        final String resourcePath = getRequestedResourcePath(request.getUri());
        ResourceEnum resource = ResourceEnum.getResourceEnumByResourcePath(resourcePath);

        // Let's check if the resource is valid.
        if (resource == null) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.BAD_REQUEST);
            return;
        }

        // Then we can process the request for every resource.
        switch (resource) {
            case CATEGORY:
                handleCategory(ctx, request);
                break;
            case LISTENERS:
                handleListeners(ctx, request);
                break;
        }

    }

    /**
     * It takes care of handling REST requests to "/category".
     *
     * @param ctx
     * @param request
     */
    private void handleCategory(ChannelHandlerContext ctx, HttpRequest request) {
        // Get the HTTP Method from the request, check if it's allowed and execute an action.
        CategoryHttpMethodEnum method;
        try {
            method = CategoryHttpMethodEnum.valueOf(request.getMethod().getName());
        } catch (IllegalArgumentException iaex) {
            // Method is not allowed.
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        switch (method) {
            case OPTIONS:
                handleOptions(ctx, request, CategoryHttpMethodEnum.values());
                break;
            case PUT:
                handleMethod(ctx, request, SpeakerActionEnum.ADD_CATEGORY);
                break;
            case DELETE:
                handleMethod(ctx, request, SpeakerActionEnum.REMOVE_CATEGORY);
                break;
            case POST:
                handleMethod(ctx, request, SpeakerActionEnum.SEND_MESSAGE_TO_CATEGORY);
                break;
            default:
                //This should never happen (it's impossible to), but just in case :)
                HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    /**
     * It takes care of handling REST requests to "/listeners".
     *
     * @param ctx
     * @param request
     */
    private void handleListeners(ChannelHandlerContext ctx, HttpRequest request) {
        // Get the HTTP Method from the request, check if it's allowed and execute an action.
        CategoryHttpMethodEnum method;
        try {
            method = CategoryHttpMethodEnum.valueOf(request.getMethod().getName());
        } catch (IllegalArgumentException iaex) {
            // Method is not allowed.
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        switch (method) {
            case OPTIONS:
                handleOptions(ctx, request, ListenersHttpMethodEnum.values());
                break;
            case POST:
                handleMethod(ctx, request, SpeakerActionEnum.SEND_MESSAGE_TO_ALL);
                break;
            default:
                //This should never happen (it's impossible to), but just in case :)
                HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    /**
     * It decodes and cleans the URI.
     *
     * @param uri
     * @return
     */
    private static String getRequestedResourcePath(String uri) throws UnsupportedEncodingException {
        String decodedUri;
        // Decode the path.
        try {
            decodedUri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            decodedUri = URLDecoder.decode(uri, "ISO-8859-1");
        }
        return decodedUri.substring(1);
    }

    /**
     * It's in charge of generating a response for an OPTIONS request.
     *
     * @param ctx
     * @param request
     * @param allowedMethodEnum
     * @param allowedMethods
     */
    private void handleOptions(ChannelHandlerContext ctx, HttpRequest request, Enum[] allowedMethods) {
        // Set some HTTP Headers.
        HttpResponse response = buildOptionsResponseHeaders(request, allowedMethods);

        // Get client channel to write the response.
        Channel ch = ctx.getChannel();

        // Write the initial line and the header.
        ChannelFuture writeFuture = ch.write(response);

        // Decide whether to close the connection or not.
        if (!HttpHeaders.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * It handles the rest of the HTTP Methods but OPTIONS.
     *
     * @param ctx
     * @param request
     */
    private void handleMethod(ChannelHandlerContext ctx, HttpRequest request, SpeakerActionEnum action) {
        // First we need the request payload (a json formatted string).
        ChannelBuffer content = request.getContent();
        String jsonRequest = content.toString(org.jboss.netty.util.CharsetUtil.UTF_8);
        logger.debug("Json request content:\n{}", jsonRequest);

        // Then we can process the json request and generate a ResponseBean with the answer.
        ResponseBean responseBean = SpeakerActionHandler.handleRequest(jsonRequest, action);

        // Get client channel to write the response.
        Channel ch = ctx.getChannel();

        // Generate and write the response.
        ChannelFuture writeFuture = ch.write(buildMethodResponse(request, responseBean));

        // Decide whether to close the connection or not.
        if (!HttpHeaders.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Builds the basic HTTP headers for the REST response.
     *
     * @param request
     * @param contentLength
     * @return
     */
    private static HttpResponse buildMethodResponse(HttpRequest request, ResponseBean responseBean) {
        // Bean to json string.
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(responseBean);

        // First response line.
        HttpResponse response;

        switch (ReasonEnum.valueOf(responseBean.getReason())) {
            case OK:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                break;
            case BAD_REQUEST:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                break;
            case INTERNAL_ERROR:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                break;
            case INVALID_ACTION:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                break;
            case CATEGORY_BAD_NAME:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                break;
            case CATEGORY_NON_EXISTENT:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                break;
            case CATEGORY_ALREADY_EXISTS:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONFLICT);
                break;
            default:
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        // Set some necessary HTTP Headers.
        HttpHeaders.setHeader(response, HttpHeaders.Names.DATE, DateUtil.getCurrent());
        HttpHeaders.setHeader(response, HttpHeaders.Names.SERVER, SERVERNAME);
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "application/json");
        HttpHeaders.setHeader(response, HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE); // We don't want the Rest client to cache things.
        HttpHeaders.setContentLength(response, jsonResponse.length());

        // Set content.
        response.setContent(ChannelBuffers.copiedBuffer(jsonResponse, org.jboss.netty.util.CharsetUtil.UTF_8));

        // Set keepalive headers.
        if (HttpHeaders.isKeepAlive(request)) {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } else {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        }

        return response;
    }
}
