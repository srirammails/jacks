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

package org.jhserv.jacks.httpservice.servlet;

import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 *
 * @author rjackson
 */
public class HttpSessionImpl implements HttpSession {

    /**
     * Our session ID
     */
    private final String sessionID;
    /**
     * The time that this session was created.
     */
    private final long creationTime;

    /**
     * The last time this session was accessed.
     */
    private final AtomicLong lastAccessedTime = new AtomicLong();

    public HttpSessionImpl(String sessionID) {
        this.sessionID = sessionID;
        creationTime = new Date().getTime();
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return sessionID;
    }

    @Override
    public long getLastAccessedTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getValue(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration getAttributeNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getValueNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeValue(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void invalidate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
