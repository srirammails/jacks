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

import java.net.InetSocketAddress;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
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

    private final AtomicReference<ServerBootstrap> serverBootstrap =
            new AtomicReference<ServerBootstrap>();


    /***
     * Internal Configuration data for our server. This is basicly a copy of the
     * configuration data gotton from the ConfigAdminService. We must have a
     * local copy of this so we can detect modifications to the configuration.
     */
    private final ConcurrentHashMap<String, String> config =
            new ConcurrentHashMap<String, String>();

    private final Map<String, Channel> openChannels =
            new ConcurrentHashMap<String, Channel>();

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
        // NOTE: A side affect of stopping our tracker is that our server will
        // be stoped as well.
        nettyTracker.close();
        serverBootstrap.set(null);
        config.clear();
        openChannels.clear();
    }

    /**
     * Called by the HttpManagedServiceFactory when our servers configuration
     * needs to be changed. Some changes will require us to stop the service
     * for a short time so that we can reconfigure it. This will not require use
     * to unregister the underlying HttpService.
     * @param config New configuration data.
     */
    public void update(Dictionary config) {

    }

    /**
     * Get a list of ports used by this server. 
     * @return
     */
    public List<String> getPorts() {
        return null;
    }

    /**
     * Get the current configuration of this server.
     * @return
     */
    public Map<String, String> getConfig() {
        return config;
    }

    /**
     * This method will only be called by the start method and then only if 
     * we have not been already started. 
     * 
     * This just copies the supplied Dictionary into our local map. 
     */
    private void copyConfig(Dictionary<String, String> cmConfig) {
        config.clear();

        Enumeration<String> keys = cmConfig.keys();
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            log.debug("Found key : " + key);
            log.debug("Value : " + cmConfig.get(key));
            // Configuration Admin inserts some keys that we don't really care
            // about but it sets the value to null so we want to catch those.
            String value = cmConfig.get(key);
            if(value != null) {
                config.put(key, value);
            }
        }
        if(cmConfig.size() != config.size()) {
            log.error("When copying the Dictionary configiration data the element sizes did not match!");
        }
    }


    /**
     * Used to start our server. This will be called by our tracker when the
     * netty service is availible.
     *
     * @param factory
     */
    private void startServer(ChannelFactory factory) {
        log.debug("Actually starting our server. ");
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        HttpServerPipelineFactory pipeline =
                new HttpServerPipelineFactory(new HttpRequestHandler());
          bootstrap.setPipelineFactory(pipeline);
          // Need to fix this to configure our options..
          bootstrap.setOption("child.tcpNoDelay", true);
          bootstrap.setOption("child.keepAlive", true);

          // Bind and start to accept incoming connections.
          Channel sc = bootstrap.bind(new InetSocketAddress(8080));
          openChannels.put("8080", sc);
          started.set(true);
    }

    private void stopServer() {
        log.debug("Stoping our server.");
        // Add code to stop our server here....
        if(!openChannels.isEmpty()) {
            Set<String> ports = openChannels.keySet();
            for(String key: ports) {
                Channel sc = openChannels.remove(key);
                sc.close().awaitUninterruptibly();
            }
            ChannelFactory factory = (ChannelFactory)nettyTracker.getService();
            factory.releaseExternalResources();
        }
        log.debug("Our server should be stopped now...");
        started.set(false);

    }

    //************* Private inner class ****************
    private class nettyTrackerCustomizer implements ServiceTrackerCustomizer {

        /**
         * Called when the Netty ServerSocketChannelFactory service is made availible.
         * On bundle startup if the netty bundle is already loaded this will get called
         * imidiatly. If the Netty bundle has not been started yet then this will get
         * called when the Netty bundle is started.
         *
         * @param sr Service Referance to the netty service.
         * @return
         */
        @Override
        public Object addingService(ServiceReference sr) {
            log.debug("NETTY Service added to our tracker.");
            ServerSocketChannelFactory sscFactory =
                    (ServerSocketChannelFactory)context.getService(sr);
            if(started.get()) {
                log.debug("nettyTrackerCustomizer.addingService called when our server has already been started." +
                        "This should not have happend! We are not restarting the server!");
            } else {
                startServer(sscFactory);
            }
            return sscFactory;
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
            log.debug("nettyTrackerCustomizer.modified called not sure what I should do....");
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
            log.debug("nettyTrackerCustomizer.removedService called Shutting down our server.");
            if(started.get()) {
                stopServer();
            } else {
                log.debug("nettyTrackerCustomizer.removedService called when the server has not been started!");
            }
        }

    }

}
