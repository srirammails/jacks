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

package org.jhserv.jacks.httpservice.servicetracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class is used to track the OSGi Log service. It is also an implementation
 * of the LogService that delegates to the tracked LogService This makes our code that
 * uses this a lot simpler as this class deals with the service and the client
 * does not have to. If the log service is not availible then no log is written.
 *
 * This class also implements the standard info, error, debug and warn methods
 * found in other logging packages again to make things simpler for the user.
 *
 * NOTE: This class must be created in our Activator and only in our Activator.
 * It also needs to be one of the first things we do so that we can be sure
 * that we don't get NPE due to the instance var being null. Yea I know this is
 * not really a singleton that is OK as long as we create this in the Activator
 * and only in the Activator every thing will be OK.
 *
 * @author rjackson
 */
public class LogTracker extends ServiceTracker implements LogService {

    private static LogTracker instance = null;

    public LogTracker(BundleContext context) {
        super(context, LogService.class.getName(), null);
        instance = this;
    }

    public static LogTracker getInstance() {
        return instance;
    }

    /*
     * Standard LogService methods that delegate to the actual service instance
     * if it is availible otherwise they go to the bit bucket...
     */

    @Override
    public void log(int level, String message) {
        LogService log = (LogService) getService();
        if(log != null) {
            log.log(level, message);
        }
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        LogService log = (LogService) getService();
        if(log != null) {
            log.log(level, message, exception);
        }
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        LogService log = (LogService) getService();
        if(log != null) {
            log.log(sr, level, message);
        }
    }

    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        LogService log = (LogService) getService();
        if(log != null) {
            log.log(sr, level, message, exception);
        }
    }

    /*
     * Methods I've added to make my life easer as I'm used to the log.info type
     * of method signatures so I added them. These methods just delegate to the
     * correct LogService method with the correct loglevel set.
     */


    /**
     * Short hand log method to log a info log message. This just makes things
     * easy for those of us that came from different logging log implementations.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        log(LogService.LOG_INFO, message);
    }

    public void info(String message, Throwable exception) {
        log(LogService.LOG_INFO, message, exception);
    }

    public void info(ServiceReference sr, String message) {
        log(sr, LogService.LOG_INFO, message);
    }

    public void info(ServiceReference sr, String message, Throwable exception) {
        log(sr, LogService.LOG_INFO, message, exception);
    }

    public void debug(String message) {
        log(LogService.LOG_DEBUG, message);
    }

    public void debug(String message, Throwable exception) {
        log(LogService.LOG_DEBUG, message, exception);
    }

    public void debug(ServiceReference sr, String message) {
        log(sr, LogService.LOG_DEBUG, message);
    }

    public void debug(ServiceReference sr, String message, Throwable exception) {
        log(sr, LogService.LOG_DEBUG, message, exception);
    }

    public void error(String message) {
        log(LogService.LOG_ERROR, message);
    }

    public void error(String message, Throwable exception) {
        log(LogService.LOG_ERROR, message, exception);
    }

    public void error(ServiceReference sr, String message) {
        log(sr, LogService.LOG_ERROR, message);
    }

    public void error(ServiceReference sr, String message, Throwable exception) {
        log(sr, LogService.LOG_ERROR, message, exception);
    }

    public void warn(String message) {
        log(LogService.LOG_WARNING, message);
    }

    public void warn(String message, Throwable exception) {
        log(LogService.LOG_WARNING, message, exception);
    }

    public void warn(ServiceReference sr, String message) {
        log(sr, LogService.LOG_WARNING, message);
    }

    public void warn(ServiceReference sr, String message, Throwable exception) {
        log(sr, LogService.LOG_WARNING, message, exception);
    }
}
