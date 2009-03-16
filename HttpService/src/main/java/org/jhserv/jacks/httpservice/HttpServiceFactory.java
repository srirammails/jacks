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

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * This factory is used to create a unique instance of our HttpService for each
 * bundle that wants to use our service. There will also be a unique factory
 * created for each of the servers that are running. We can't really just use
 * a single service instance because we have to have access to the OSGi bundle
 * object for each bundle that wants to use us and this is the only place we can
 * get it. 
 *
 * @author rjackson
 */
public class HttpServiceFactory implements ServiceFactory {

    @Override
    public Object getService(Bundle bundle, ServiceRegistration sr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration sr, Object serviceInstance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
