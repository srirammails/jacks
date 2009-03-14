/*
 * Copyright 2008 Richard Jackson <richard.jackson@gmail.com>
 *
 * This file is part of the org.jhserv.osgi.HttpService OSGi bundle
 *
 * This OSGi bundle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License as well
 * as a copy of the additional permissions granted by the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.jhserv.jacks.httpservice.server;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jhserv.jacks.httpservice.servicetracker.LogTracker;

/**
 * @author rjackson
 */

@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelHandler {

    // Log tracker
    private final LogTracker log = LogTracker.getInstance();
    private volatile HttpRequest request;
    private volatile boolean readingChunks;
    private final StringBuilder responseContent = new StringBuilder();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpRequest request = this.request = (HttpRequest) e.getMessage();
            responseContent.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            responseContent.append("===================================\r\n");

            if (request.containsHeader(HttpHeaders.Names.HOST)) {
                responseContent.append("HOST: " + request.getHeader(HttpHeaders.Names.HOST) + "\r\n");
            }

            responseContent.append("REQUEST_URI: " + request.getUri() + "\r\n\r\n");

            if (!request.getHeaderNames().isEmpty()) {
                for (String name : request.getHeaderNames()) {
                    for (String value : request.getHeaders(name)) {
                        responseContent.append("HEADER: " + name + " = " + value + "\r\n");
                    }
                }
                responseContent.append("\r\n");
            }

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> params = queryStringDecoder.getParameters();
            if (!params.isEmpty()) {
                for (Entry<String, List<String>> p : params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals) {
                        responseContent.append("PARAM: " + key + " = " + val + "\r\n");
                    }
                }
                responseContent.append("\r\n");
            }

            if (request.isChunked()) {
                readingChunks = true;
                return;
            } else {
                ChannelBuffer content = request.getContent();
                if (content.readable()) {
                    responseContent.append("CONTENT: " + content.toString("UTF-8") + "\r\n");
                }
                writeResponse(e);
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                responseContent.append("END OF CONTENT\r\n");
                writeResponse(e);
                return;
            } else {
                responseContent.append("CHUNK: " + chunk.getContent().toString("UTF-8") + "\r\n");
            }
        }
    }

    private void writeResponse(MessageEvent e) {
        // Convert the response content to a ChannelBuffer.
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent.toString(), "UTF-8");
        responseContent.setLength(0);

        // Decide whether to close the connection or not.
        boolean close =
                HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) ||
                request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) &&
                !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));

/**
        CookieDecoder cookieDecoder = new CookieDecoder();
        Map<String, Cookie> cookies = cookieDecoder.decode(request.getHeader(HttpHeaders.Names.COOKIE));
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            CookieEncoder cookieEncoder = new CookieEncoder();
            for (Cookie cookie : cookies.values()) {
                cookieEncoder.addCookie(cookie);
            }
            response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
        }
*/
        // Write the response.
        ChannelFuture future = e.getChannel().write(response);

        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
