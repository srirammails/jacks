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

import java.nio.charset.Charset;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jhserv.jacks.httpservice.servicetracker.LogTracker;

/**
 * @author rjackson
 */

@ChannelPipelineCoverage("all")
public class HttpResponseHandler extends SimpleChannelHandler {

    // Log tracker
    private final LogTracker log = LogTracker.getInstance();


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpResponse response = (HttpResponse) e.getMessage();
        log.info(response.getContent().toString(Charset.defaultCharset().name()));
    }

}
