package com.fbudassi.neddy.handler.expert;

import com.fbudassi.neddy.config.Config;
import com.fbudassi.neddy.config.DirectoryIndex;
import com.fbudassi.neddy.handler.HandlerUtil;
import com.fbudassi.neddy.util.DateUtil;
import java.io.*;
import java.net.URLDecoder;
import java.util.List;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It concentrates all the methods related to the static content requests.
 *
 * @author fbudassi
 */
public class StaticContentExpert extends CommunicationExpert {

    private static final Logger logger = LoggerFactory.getLogger(StaticContentExpert.class);
    // Some configuration variables.
    private static final String WWWROOT = Config.getValue(Config.KEY_WWWROOT);
    // Collection with mime types.
    private static final FileTypeMap MIME_TYPES = MimetypesFileTypeMap.getDefaultFileTypeMap();
    // List of default names of index files.
    private static final List<String> DEFAULT_NAMES = DirectoryIndex.getFileNames();

    //Allowed Http Methods.
    private enum AllowedHttpMethodEnum {

        OPTIONS, GET, HEAD
    }

    /**
     * It handles static content requests.
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

        // Get the HTTP Method from the request, check if it's allowed and execute an action.
        AllowedHttpMethodEnum method;
        try {
            method = AllowedHttpMethodEnum.valueOf(request.getMethod().getName());
        } catch (IllegalArgumentException iaex) {
            // Method is not allowed.
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        switch (method) {
            case GET:
                handleGet(ctx, request);
                break;
            case OPTIONS:
                handleOptions(ctx, request);
                break;
            case HEAD:
                handleHead(ctx, request);
                break;
            default:
                //This should never happen (it's impossible to), but just in case :)
                HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    /**
     * It's the method in charge of generating a response for GET requests.
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    private static void handleGet(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        // Check for file validity.
        final String path = WWWROOT + sanitizeUri(request.getUri());
        if (path == null) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.FORBIDDEN);
            return;
        }

        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            // Look for a valid index file in the directory (e.g. index.html).
            for (String fileName : DEFAULT_NAMES) {
                File indexFile = new File(path + fileName);
                if (indexFile.exists() && indexFile.isFile() && !indexFile.isHidden()) {
                    file = indexFile;
                    break;
                }
            }
        }

        if (!file.isFile()) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.FORBIDDEN);
            return;
        }

        // Open file.
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        // Set some HTTP Headers.
        HttpResponse response = buildGetResponseHeaders(request, file, fileLength);

        // Get client channel to write the response.
        Channel ch = ctx.getChannel();

        // Write the initial line and the header.
        ch.write(response);

        // Use zero-copy (no need to spend time copying buffers) through java.nio filechannels.
        // The associated file is closed after transfer is complete.
        // It may use DMA to do the transfer or take advantage of another SO capability.
        // See transferTo in: http://download.oracle.com/javase/6/docs/api/java/nio/channels/FileChannel.html
        final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, fileLength, true);
        ChannelFuture writeFuture = ch.write(region);

        // Decide whether to close the connection or not.
        if (!HttpHeaders.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * It's in charge of generating a response for an OPTIONS request.
     *
     * @param ctx
     * @param request
     */
    private void handleOptions(ChannelHandlerContext ctx, HttpRequest request) {
        // Set some HTTP Headers.
        HttpResponse response = buildOptionsResponseHeaders(request, AllowedHttpMethodEnum.values());

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
     * It's in charge of generating a response for a HEAD request.
     *
     * @param ctx
     * @param request
     */
    private void handleHead(ChannelHandlerContext ctx, HttpRequest request) throws IOException {
        // Check for file validity.
        final String path = WWWROOT + sanitizeUri(request.getUri());
        if (path == null) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.FORBIDDEN);
            return;
        }

        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            // Look for a valid index file in the directory (e.g. index.html).
            for (String fileName : DEFAULT_NAMES) {
                File indexFile = new File(path + fileName);
                if (indexFile.exists() && indexFile.isFile() && !indexFile.isHidden()) {
                    file = indexFile;
                    break;
                }
            }
        }

        if (!file.isFile()) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.FORBIDDEN);
            return;
        }

        // Open file.
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        // Set some HTTP Headers.
        HttpResponse response = buildGetResponseHeaders(request, file, fileLength);

        // Get client channel to write the response.
        Channel ch = ctx.getChannel();

        // Write the initial line and the header, but not the content of the file, due to a HEAD request.
        ChannelFuture writeFuture = ch.write(response);

        // Decide whether to close the connection or not.
        if (!HttpHeaders.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Builds the basic HTTP headers for the GET and HEAD methods.
     *
     * @param request
     * @param file
     * @param fileLength
     * @return
     */
    private static HttpResponse buildGetResponseHeaders(HttpRequest request, File file, long fileLength) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        // Get mime type.
        String mimeType = MIME_TYPES.getContentType(file);
        if ("text/plain".equals(mimeType)) {
            mimeType += "; charset=utf-8";
        }

        // Set some HTTP Headers.
        HttpHeaders.setHeader(response, HttpHeaders.Names.DATE, DateUtil.getCurrent());
        HttpHeaders.setHeader(response, HttpHeaders.Names.SERVER, SERVERNAME);
        HttpHeaders.setHeader(response, HttpHeaders.Names.LAST_MODIFIED, DateUtil.formatDate(file.lastModified()));
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, mimeType);
        HttpHeaders.setContentLength(response, fileLength);

        // Workaround for Apache Benchmark bug.
        // See http://blog.lolyco.com/sean/2009/11/25/ab-apache-bench-hanging-with-k-keep-alive-switch/
        if (HttpHeaders.isKeepAlive(request)) {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } else {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        }

        return response;
    }

    /**
     * Cleans the request Uri.
     *
     * @param uri
     * @return
     */
    private static String sanitizeUri(String uri) throws UnsupportedEncodingException {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            uri = URLDecoder.decode(uri, "ISO-8859-1");
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Security checks.
        if (uri.contains(File.separator + ".")
                || uri.contains("." + File.separator)
                || uri.startsWith(".") || uri.endsWith(".")) {
            return null;
        }

        return uri;
    }
}
