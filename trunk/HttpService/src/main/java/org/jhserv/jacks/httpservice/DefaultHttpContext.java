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

import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * HttpService supplied default HttpContext. This is a very basic HttpContext in
 * that no security policy is implemented no custom mime types are implemented
 * and a standard resource locater is implemented. So other words nothing fancy
 * but it will get the job done.
 *
 * @author rjackson
 */
public class DefaultHttpContext implements HttpContext {

    private final Bundle bundle;

    public DefaultHttpContext(Bundle bundle) {
        this.bundle = bundle;
    }


    @Override
    public boolean handleSecurity(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        return true;
    }

    @Override
    public URL getResource(String name) {
        return bundle.getResource(name);
    }

    @Override
    public String getMimeType(String name) {
        return null;
    }

}
