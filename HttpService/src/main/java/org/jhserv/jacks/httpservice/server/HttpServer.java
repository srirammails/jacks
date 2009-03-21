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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
import org.jhserv.jacks.httpservice.BundleConstants;
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

    /**
     * The ServerBootstrap instance we used to create our server with.
     */
    private final AtomicReference<ServerBootstrap> serverBootstrap =
            new AtomicReference<ServerBootstrap>();

    /***
     * Internal Configuration data for our server. This is basically a copy of the
     * configuration data we got from the ConfigAdminService. We must have a
     * local copy of this so we can detect modifications to the configuration.
     */
    private final ConcurrentHashMap<String, String> config =
            new ConcurrentHashMap<String, String>();

    /**
     * What Channel's have we opened?
     */
    private final Map<Integer, Channel> openChannels =
            new ConcurrentHashMap<Integer, Channel>();

    public HttpServer(BundleContext context) {
        this.context = context;
    }

    /**
     * Method to start our service. It should be noted that this method does
     * not actually setup our server it just starts the tracker which will then
     * start our server if the Netty service is available.
     *
     * @param config Server configuration data. The HttpManagedServiceFactory
     * which calls this method must ensure that this configuration data is valid
     * before calling this method.
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
     * This just copies the supplied Dictionary into our local map. It will also
     * populate the duplicate port names if they are not defined in the Dictionary.
     * This is so the data is exposed on the service correctly.
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

        // Make sure both port properties are populated.
        String value = config.get(BundleConstants.CONFIG_PORT);
        if(value != null && !value.isEmpty()) {
        	config.put(BundleConstants.CONFIG_OSGI_PORT, value);
        } else {
        	value = config.get(BundleConstants.CONFIG_OSGI_PORT);
        	if(value != null && !value.isEmpty()) {
        		config.put(BundleConstants.CONFIG_PORT, value);
        	}
        }
        // Do ssl port now..
        value = config.get(BundleConstants.CONFIG_SSL_PORT);
        if(value != null && !value.isEmpty()) {
        	config.put(BundleConstants.CONFIG_OSGI_SECURE_PORT, value);
        } else {
        	value = config.get(BundleConstants.CONFIG_OSGI_SECURE_PORT);
        	if(value != null && !value.isEmpty()) {
        		config.put(BundleConstants.CONFIG_SSL_PORT, value);
        	}
        }
    }


    /**
     * Used to start our server. This will be called by our tracker when the
     * netty service is available.
     *
     * @param factory
     */
    private void startServer(ChannelFactory factory) {
        log.debug("Actually starting our server. ");
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        HttpServerPipelineFactory pipeline =
                new HttpServerPipelineFactory(new HttpRequestHandler());
          bootstrap.setPipelineFactory(pipeline);
          // Configure our server
          InetSocketAddress[] ipAddress = buildSocketAddress();
          setChannelOptions(bootstrap);

          if(ipAddress[0] != null) {
              log.debug("Binding ipAddress:port => " + ipAddress[0].toString());
              Channel sc = bootstrap.bind(ipAddress[0]);
              openChannels.put(new Integer(ipAddress[0].getPort()), sc);
              started.set(true);
          }

          if(ipAddress[1] != null) {
              log.debug("Binding ipAddress:port => " + ipAddress[1].toString());
              Channel sc = bootstrap.bind(ipAddress[1]);
              openChannels.put(new Integer(ipAddress[1].getPort()), sc);
              started.set(true);
          }
    }

    /**
     * Used to stop our server. This will be called by our tracker when the
     * netty service goes away.
     */
    private void stopServer() {
        log.debug("Stoping our server.");
        // Add code to stop our server here....
        if(!openChannels.isEmpty()) {
            Set<Integer> ports = openChannels.keySet();
            for(Integer key: ports) {
                Channel sc = openChannels.remove(key);
                sc.close().awaitUninterruptibly();
            }
            ChannelFactory factory = (ChannelFactory)nettyTracker.getService();
            factory.releaseExternalResources();
        }
        log.debug("Our server should be stopped now...");
        started.set(false);

    }

    /**
     * This is used to read our configuration and build up to two InetSocketAddress
     * that our server may bind to. Note: This method will be modified to at
     * some point to do other addresses as well but for now it just supports
     * two.
     *
     * @return InetSocketAddress[] where position 0 is a normal SocketAddress
     * and position 1 is the SSL SocketAddress.
     */
    private InetSocketAddress[] buildSocketAddress() {
    	InetSocketAddress[] result = new InetSocketAddress[2];

    	String value = config.get(BundleConstants.CONFIG_IP_ADDRESS);
        InetAddress ipAddress = null;
        if(value != null && !value.isEmpty()) {
      	  try {
      		  ipAddress = InetAddress.getByName(value);
      	  } catch(UnknownHostException e) {
      		  log.error("We could not a InetAddress from the supplied IP in the configuration.");
      		  log.error("We will bind to all address on this machine.");
      		  ipAddress = null;
      	  }
        }
        value = config.get(BundleConstants.CONFIG_PORT);
        int nPort = -1;
        int sslPort = -1;
        if(value != null && !value.isEmpty()) {
      	  nPort = Integer.parseInt(value);
        }
        value = config.get(BundleConstants.CONFIG_SSL_PORT);
        if(value != null && !value.isEmpty()) {
      	  sslPort = Integer.parseInt(value);
        }

        // Build our InetSocketAddress for both the normal port and the
        // SSL port if it is defined.
        if(ipAddress != null) {
      	  if(nPort != -1) {
      		  result[0] = new InetSocketAddress(ipAddress, nPort);
      	  }
      	  if(sslPort != -1) {
      		  result[1] = new InetSocketAddress(ipAddress, sslPort);
      	  }
        } else {
      	  if(nPort != -1) {
      		  result[0] = new InetSocketAddress(nPort);
      	  }
      	  if(sslPort != -1) {
      		  result[1] = new InetSocketAddress(sslPort);
      	  }
        }
    	return result;
    }

    /**
     * Set our ChannelOptions defined in our configuration.
     *
     * @param bootstrap
     */
    private void setChannelOptions(ServerBootstrap bootstrap) {

    	// set tcpNoDelay
    	String value = config.get(BundleConstants.CONFIG_TCP_NODELAY);
    	if(value != null && !value.isEmpty()) {
    		if(value.equalsIgnoreCase("true")) {
    			bootstrap.setOption("child.tcpNoDelay", true);
    		} else {
    			bootstrap.setOption("child.tcpNoDelay", false);
    		}
    	} else {
    		// Default to true
    		bootstrap.setOption("child.tcpNoDelay", true);
    	}

    	// Set keepAlive
    	value = config.get(BundleConstants.CONFIG_KEEP_ALIVE);
    	if(value != null && !value.isEmpty()) {
    		if(value.equalsIgnoreCase("true")) {
    			bootstrap.setOption("child.keepAlive", true);
    		} else {
    			bootstrap.setOption("child.keepAlive", false);
    		}
    	} else {
    		// Default to true
    		bootstrap.setOption("child.keepAlive", true);
    	}

    	// Set reuseAddress
    	value = config.get(BundleConstants.CONFIG_REUSE_ADDRESS);
    	if(value != null && !value.isEmpty()) {
    		if(value.equalsIgnoreCase("true")) {
    			bootstrap.setOption("child.reuseAddress", true);
    		} else {
    			bootstrap.setOption("child.reuseAddress", false);
    		}
    	} else {
    		// Default
    		bootstrap.setOption("child.reuseAddress", true);
    	}

    	// Set connectionTimeout
    	value = config.get(BundleConstants.CONFIG_CONNECT_TIMEOUT);
    	if(value != null && !value.isEmpty()) {
    		bootstrap.setOption("child.connectTimeoutMillis", Integer.parseInt(value));
    	}

    	// Set recieveBufferSize
    	value = config.get(BundleConstants.CONFIG_RECEIVE_BUFFER_SIZE);
    	if(value != null && !value.isEmpty()) {
    		bootstrap.setOption("child.receiveBufferSize", Integer.parseInt(value));
    	}

    	// set sendBufferSize
    	value = config.get(BundleConstants.CONFIG_SEND_BUFFER_SIZE);
        if(value != null && !value.isEmpty()) {
            bootstrap.setOption("child.sendBufferSize", Integer.parseInt(value));
        }

    }

    //************* Private inner class ***************************************
    //=========================================================================
    private class nettyTrackerCustomizer implements ServiceTrackerCustomizer {

        /**
         * Called when the Netty ServerSocketChannelFactory service is made available.
         * On bundle startup if the netty bundle is already loaded this will get called
         * Immediately. If the Netty bundle has not been started yet then this will get
         * called when the Netty bundle is started.
         *
         * @param sr Service Reference to the netty service.
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
