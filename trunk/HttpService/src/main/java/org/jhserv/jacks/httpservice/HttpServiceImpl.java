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

package org.jhserv.jacks.httpservice;

import java.util.Dictionary;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * This is our HttpService Implementation. An instance of this class will be
 * created by our HttpServiceFactory for each bundle that wants to use the
 * HttpService
 *
 * @author rjackson
 */
public class HttpServiceImpl implements HttpService {

    @Override
    public void registerServlet(String alias, Servlet servlet, Dictionary intparams,
            HttpContext context) throws ServletException, NamespaceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void registerResources(String alias, String name, HttpContext context)
            throws NamespaceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unregister(String alias) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpContext createDefaultHttpContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
