/*
 * Copyright 2009 Richard Jackson <richard.jackson@gmail.com>
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Servlet execution request handler. This handler will take a request and
 * map it to the appropriate servlet for servicing. To reduce the code in this
 * class it does not validate any of the servlets or there asciated data. It is
 * up to our ServletRegistrations class to do that work. This class just locates
 * and executes the filter chain for a particular mount point.
 *
 * @author rjackson
 */

@ChannelPipelineCoverage("all")
public class ServletRequestHandler extends SimpleChannelHandler {

    /**
     * This holds our mapping of URI and the servlet to be executed. It should
     * be noted that this map will be shared with the ServletRegistrations
     * class asciated with this particular server.
     */
    private final Map<String, ServletExecuter> requestMap =
            new ConcurrentHashMap<String, ServletExecuter>();

    /**
     * The ServletRegistrations asciated with this handler.
     */
    private final ServletRegistrations servletRegistrations =
            new ServletRegistrations(requestMap, this);
}
