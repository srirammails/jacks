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

/**
 * This contains all of our static constants used through out our service.
 * I really hate to hunt to find them so I put them all in one place unless
 * a particular constant belongs in one particular class then it will go there.
 *
 * @author rjackson
 */
public class BundleConstants {

    /**
     * Netty Server socket factory service
     */
    public static final String NETTY_SERVER_SOCKET_FACTORY_CLASS =
            "org.netty.jboss.channel.socket.ServerSocketChannelFactory";

    /**
     * Netty Client socket factory service
     */
    public static final String NETTY_CLIENT_SOCKET_FACTORY_CLASS =
            "org.jboss.netty.channel.socket.ClientSocketChannelFactory";

    /**
     * Where to find our configuration files
     */
    public static final String CONFIG_DIR = "./conf";
    /**
     * What our configuration files must start with. NOTE: This must be all
     * lower case becuase when we are filtering the files we lowercase them first.
     */
    public static final String CONFIG_FILE_FILTER = "jhservhttpservice";

    

    /*
     * This section contains our configuration keys for both the properties
     * files and for the Configuration Admin service.
     */

    /**
     * Our key for the default configuration data if it is loaded.
     */
    public static final String CONFIG_PROPS_DEFAULT = "default";

    /**
     * True/False property that tells the service wheather or not to override
     * what is stored in the Configuration Admin Service. For this configuration.
     *
     */
    public static final String CONFIG_OVERRIDE_ADMIN =
            "OverRideConfigAdmin";
    
    /**
     * The port that the server should connect to. 
     */
    public static final String CONFIG_PORT = "port";

    /**
     * The SSL Port the server should listen on.
     */
    public static final String CONFIG_SSL_PORT = "ssl.port";


    /**
     * IP Address to bind to. If this is not defined then all IP address on the
     * machine will be bound.
     */
    public static final String CONFIG_IP_ADDRESS = "ip.address";

    /**
     * This is the default HttpService Property for the port to use. This property
     * is defined in the framework system property file.
     */
    public static final String CONFIG_OSGI_PORT = "org.osgi.service.http.port";

    /**
     * Per the HttpService spec (102.9) the default port to use if none are defined
     * is port 80
     */
    public static final String CONFIG_OSGI_PORT_DEFAULT = "80";

    /**
     * This is the default HttpService Property for the Secured port address.
     * This property is defined in the framework system property file.
     */
    public static final String CONFIG_OSGI_SECURE_PORT = "org.osgi.service.http.port.secure";

    /**
     * Per the HttpService spec (102.9) the default port to use of none are defined
     * is port 443
     */
    public static final String CONFIG_OSGI_SECURE_PORT_DEFAULT = "443";

    /**
     * True/False property to indicate wheather or not SSL is enabled on this port.
     */
    public static final String CONFIG_ENABLE_SSL = "ssl";

    /**
     * You set this if this server to be bound to another service. The service
     * is identified by the port it is running. What this means is that lets say
     * we have setup a service on port 80 and now we also want to setup another
     * port that all servlets registerd on the port 80 service should also
     * be serviced on this port. 
     */
    public static final String  CONFIG_BIND_TO_PORT = "bind.to.port";
}
