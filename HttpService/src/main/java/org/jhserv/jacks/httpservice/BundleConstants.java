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
     * Where to find our configuration files
     */
    public static final String CONFIG_DIR = "./conf";
    /**
     * What our configuration files must start with. NOTE: This must be all
     * lower case becuase when we are filtering the files we lowercase them first.
     */
    public static final String CONFIG_FILE_FILTER = "jackshttpservice";

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
     * This is the default HttpService Property for the port to use. This property
     * is defined in the framework system property file. And can be defined in
     * our property file as well.
     */
    public static final String CONFIG_OSGI_PORT = "org.osgi.service.http.port";

    /**
     * Per the HttpService spec (102.9) the default port to use if none are defined
     * is port 80
     */
    public static final String CONFIG_OSGI_PORT_DEFAULT = "80";


    /**
     * The SSL Port the server should listen on.
     */
    public static final String CONFIG_SSL_PORT = "ssl.port";

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
     * IP Address to bind to. If this is not defined then all IP address on the
     * machine will be bound.
     */
    public static final String CONFIG_IP_ADDRESS = "IPAddress";

    /**
     * Host name we should look for on requests. If this is defined any other
     * Host name than the one defined will get rejected.
     */
    public static final String CONFIG_HOSTNAME = "HostName";

    /**
     * This is the base name for defining vertual Host names.
     */
    public static final String CONFIG_VHOSTS = "VHostName";

    /**
     * Connection timeouts in millis.
     */
    public static final String CONFIG_CONNECT_TIMEOUT = "connectTimeoutMillis";

    /**
     * Connection Write timeouts in millis
     */
    public static final String CONFIG_WRITE_TIMEOUT = "writeTimeoutMillis";

    /**
     * Connection should reuse Addresses.
     */
    public static final String  CONFIG_REUSE_ADDRESS = "reuseAddress";

    /**
     * Socket receive buffer size.
     */
    public static final String  CONFIG_RECEIVE_BUFFER_SIZE = "receiveBufferSize";

    /**
     * Socket send buffer size.
     */
    public static final String  CONFIG_SEND_BUFFER_SIZE = "sendBufferSize";

    /**
     * should connections be kept alive?
     */
    public static final String  CONFIG_KEEP_ALIVE = "keepAlive";

    /**
     * Should tcp no delay be turned on?
     */
    public static final String  CONFIG_TCP_NODELAY = "tcpNoDelay";

}