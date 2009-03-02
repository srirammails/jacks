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

package org.jhserv.osgi.HttpService;

import java.util.Dictionary;
import java.util.Hashtable;
import org.jhserv.jacks.httpservice.BundleConstants;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Utility methods for our test cases.
 *
 * @author rjackson
 */
public class TestUtils {

    /**
     * This is just here to make the maven surefire plugin happy. I'm sure there
     * is some way to tell it not to test a class but I'm not sure how yet.
     */
    @Test
    public void fakeTest() {
        assertTrue(true);
    }


    /**
     * Method to generate a fully populated configuration object. All values
     * are valid. This is used by test methods to get a starting point config
     * that they can then modify for there specific test case.
     * 
     * @return
     */
    public static Dictionary<String, String> genDefaultConfig() {
        Hashtable<String,String> conf = new Hashtable<String, String>();

        conf.put(BundleConstants.CONFIG_OVERRIDE_ADMIN, "False");
        conf.put(BundleConstants.CONFIG_OSGI_PORT, BundleConstants.CONFIG_OSGI_PORT_DEFAULT);
        conf.put(BundleConstants.CONFIG_PORT, BundleConstants.CONFIG_OSGI_PORT_DEFAULT);
        conf.put(BundleConstants.CONFIG_OSGI_SECURE_PORT, BundleConstants.CONFIG_OSGI_SECURE_PORT_DEFAULT);
        conf.put(BundleConstants.CONFIG_SSL_PORT, BundleConstants.CONFIG_OSGI_SECURE_PORT_DEFAULT);
        conf.put(BundleConstants.CONFIG_IP_ADDRESS, "127.0.0.1");
        conf.put(BundleConstants.CONFIG_HOSTNAME, "localhost");
        conf.put(BundleConstants.CONFIG_CONNECT_TIMEOUT, "10000");
        conf.put(BundleConstants.CONFIG_WRITE_TIMEOUT, "20000");
        conf.put(BundleConstants.CONFIG_REUSE_ADDRESS, "true");
        conf.put(BundleConstants.CONFIG_RECEIVE_BUFFER_SIZE, "131072");
        conf.put(BundleConstants.CONFIG_SEND_BUFFER_SIZE, "131072");
        conf.put(BundleConstants.CONFIG_KEEP_ALIVE, "true");
        conf.put(BundleConstants.CONFIG_TCP_NODELAY, "true");

        return conf;
    }

}
