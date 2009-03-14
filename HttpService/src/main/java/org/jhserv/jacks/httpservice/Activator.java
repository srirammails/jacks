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

package org.jhserv.jacks.httpservice;

import org.jhserv.jacks.httpservice.servicetracker.LogTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This is our service activator class.
 *
 * @author rjackson
 */
public class Activator implements BundleActivator {

    private volatile BundleContext context;

    private LogTracker logTracker = null;
    private HttpManagedServiceFactory serviceFactory;

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        logTracker = new LogTracker(context);
        logTracker.open();
        serviceFactory = new HttpManagedServiceFactory(context);
        // Must run our serviceFactory start method in another thread because
        // it may block/run slow.
        Thread t = new Thread(new Runnable() {
            public void run() {
                serviceFactory.start();
            }
        });
        t.start();

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // this must be stoped before the logTracker is stoped
        serviceFactory.stop();
        logTracker.close();
        serviceFactory = null;
    }

}
