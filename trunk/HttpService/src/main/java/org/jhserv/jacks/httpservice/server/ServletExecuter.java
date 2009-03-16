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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.jhserv.jacks.httpservice.servicetracker.LogTracker;
import org.osgi.service.http.HttpContext;

/**
 * This is our main servlet execution class. The ServletRequestHandler will call
 * this for every request that maps to the resource location for this FilterChain.
 *
 * This class also holds all data asciated with a particular servlet. It is up
 * to the ServletRegistrations class to ensure that this is completly populated
 * before it is placed into service. The ServletRequestHandler will call our
 * place in service method before placing this into service.
 *
 * @author rjackson
 */
public class ServletExecuter implements FilterChain {

    private final Servlet servlet;
    private final ServletConfig servletConfig;
    private final String alias;
    private final HttpContext httpContext;
    private final LogTracker log = LogTracker.getInstance();

    /**
     * Is this class in service?
     */
    private AtomicBoolean  inService = new AtomicBoolean();

    /**
     * All filters asciated with this particular servlet.
     */
    private final List<Filter> filters = new CopyOnWriteArrayList<Filter>();

    /**
     * SessionFactory to use for getting a session. 
     */
    private SessionFactory sessionFactory;
    
    /**
     * This constructor just takes the basic requirements needed to execute
     * a particular servlet. It should be noted that there are other fields that
     * have to be set as well before this executer can be placed into service.
     * The server/handler will call our isReadyForService method to verify that
     * this executer is ready for service before placing it into service. 
     *
     * @param servlet The servlet this executer is responsable for.
     * @param servletConfig The servlets config object
     * @param alias The alias this servlet is registered under.
     * @param httpContext The OSGi HttpContext this servlet was registered with.
     */
    public ServletExecuter(Servlet servlet, ServletConfig servletConfig, String alias,
            HttpContext httpContext) {
        this.servlet = servlet;
        this.servletConfig = servletConfig;
        this.alias = alias;
        this.httpContext = httpContext;
    }

    /**
     * Is this executer ready to be placed into service?
     * @return
     */
    public boolean isReadyForService() {

        return true;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    

}
