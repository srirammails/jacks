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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.Servlet;

/**
 * This class takes care of the actuall registration of servlets into the server.
 * Our HttpServiceImpl delegates much of its work to this class. An instance of
 * this will be created for each server we have running.
 *
 * This class is responsable for creating and installing our ServletExecuter
 * objects into the server. It is also responsable for ensuring that all registrations
 * are valid and will not clash with each other.
 *
 * It should be noted that this class and the ServletRequestHandler share a url
 * to servlet map with each other. This class does not create or destroy this
 * map. The ServletRequestHandler is responsable for that.
 *
 * @author rjackson
 */
public class ServletRegistrations {

    /**
     * Shared map for holding our servlets alias and its execution object. 
     */
    private final Map<String, ServletExecuter> requestMap;

    /**
     * List of all servlet instances registered. The OSGi HttpService spec states
     * that a servlet instance can only be registered one time. This is used to
     * enforce that. See section 102.2 of the OSGi HttpService spec.
     */
    private final List<Servlet> registeredServlets = new CopyOnWriteArrayList<Servlet>();

    

    public ServletRegistrations(Map<String, ServletExecuter> requestMap) {
        this.requestMap = requestMap;
    }
}
