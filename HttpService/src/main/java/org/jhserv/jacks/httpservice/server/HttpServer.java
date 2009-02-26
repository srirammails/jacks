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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jhserv.jacks.httpservice.servicetracker.LogTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This is our actual HttpServer implementation. It will automaticly track
 * our service requirment and either start the server or stop the server if that
 * service goes away. It is also responsible for registering the actial
 * HttpService that is asciated with this server.
 *
 * @author rjackson
 */
public class HttpServer {

    // To avoid Threading issues all class vars must be initilized either in
    // the constructor or in the start method. After that everything must be
    // Read access only as we will be multi-threaded at after that. So if you
    // Modify anything be aware of this restriction.

    private final LogTracker log = LogTracker.getInstance();
    private volatile BundleContext context;
    /**
     * Service tracker tracking the Netty Server socket factory service. This will
     * be set in the start method and then read only after that. Actually it is
     * only used in the stop method to stop the tracker.
     */
    private ServiceTracker nettyTracker;

    // The service Name that the netty bundle provides.
    public static final String NETTY_SERVICE = ServerSocketChannelFactory.class.getName();

    /**
     * Are we started?
     */
    private final AtomicBoolean started = new AtomicBoolean();

    /***
     * Internal Configuration data for our server. This is basicly a copy of the
     * configuration data gotton from the ConfigAdminService. We must have a
     * local copy of this so we can detect modifications to the configuration.
     */
    private final ConcurrentHashMap<String, String> config =
            new ConcurrentHashMap<String, String>();

    public HttpServer(BundleContext context) {
        this.context = context;
    }

    /**
     * Method to start our service. It should be noted that this method does 
     * not actually setup our server it just starts the tracker which will then 
     * start our server if the Netty service is avalilible.
     *
     * @param config Server configuration data. The HttpManagedServiceFactory 
     * which calls this method must ensure that this configuration data is valid
     * befor calling this method. 
     */
    public void start(Dictionary config) {

        copyConfig(config);
        // Start our tracker
        nettyTracker = new ServiceTracker(context, NETTY_SERVICE, new nettyTrackerCustomizer());
        nettyTracker.open();
    }


    /**
     * Called by the HttpManagedServiceFactory to stop this server/service.
     */
    public void stop() {
        nettyTracker.close();
        // Add code to stop our server here....

    }

    /**
     * Called by the HttpManagedServiceFactory when our servers configuration
     * needs to be changed. Some changes will require us to stop the service
     * for a short time so that we can reconfigure it. This will not require use
     * to unregister the underlying HttpService.
     * @param config New configuration data.
     */
    public void modifiedConfig(Dictionary config) {

    }

    /**
     * This method will only be called by the start method and then only if 
     * we have not been already started. 
     * 
     * This just copies the supplied Dictionary into our local map. 
     */
    private void copyConfig(Dictionary cmConfig) {
        config.clear();

        Enumeration<String> keys = cmConfig.elements();
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            config.put(key, (String)cmConfig.get(key));
        }
        if(cmConfig.size() != config.size()) {
            log.error("When copying the Dictionary configiration data the element sizes did not match!");
        }
    }

    //************* Private inner class ****************
    private class nettyTrackerCustomizer implements ServiceTrackerCustomizer {

        /**
         * Called when the Netty ServerSocketChannelFactory service is made availible.
         * On bundle startup if the netty bundle is already loaded this will get called
         * imidiatly. If the Netty bundle has not been started yet then this will get
         * called when the Netty bundle is started.
         *
         * This is where all of the real work gets done for starting the server.
         *
         * @param sr Service Referance to the netty service.
         * @return
         */
        @Override
        public Object addingService(ServiceReference sr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * This is called when the Netty ServerSocketChannelFactory we are traking
         * is modified. We should not see this but if we do we log it so we can
         * track it down and understand what needs to go here.
         *
         * @param sr
         * @param service
         */
        @Override
        public void modifiedService(ServiceReference sr, Object notused) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * This is called if the Netty ServerSocketCannelFactory we are tracking
         * is going out of service. In this case we must stop our service and server.
         *
         * This and the stop method are where the service and the server get
         * stoped.
         *
         * @param sr
         * @param service
         */
        @Override
        public void removedService(ServiceReference sr, Object notused) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
