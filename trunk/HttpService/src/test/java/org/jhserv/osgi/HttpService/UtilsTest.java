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
import org.jhserv.jacks.httpservice.BundleConstants;
import org.jhserv.jacks.httpservice.Utils;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import static org.junit.Assert.*;


/**
 * Unit tests for the Utils class.
 *
 * @author rjackson
 */
public class UtilsTest {


    /**
     * String must be a number and must not be negative
     */
    @Test
    public void isANumberTest() {
        assertTrue("isANumber Returned False on 123456", Utils.isANumber("123456"));
        assertFalse("isANumber returned True on test", Utils.isANumber("test"));
        assertFalse("isANumber returned True on -123456", Utils.isANumber("-123456"));
        assertFalse("isANumber returned True on 123a45s", Utils.isANumber("123a45s"));
    }

    /**
     * A port number must be a non negative number in the range 0-65535
     */
    @Test
    public void isPortNumberTest() {
        assertTrue("isPortNumber returned false on 0", Utils.isPortNumber("0"));
        assertTrue("isPortNumber returned false on 65535", Utils.isPortNumber("65535"));
        assertTrue("isPortNumber returned false on 80", Utils.isPortNumber("80"));
        assertFalse("isPortNumber returned True on -1", Utils.isPortNumber("-1"));
        assertFalse("isPortNumber returned True on 65536", Utils.isPortNumber("65536"));
        assertFalse("isPortNumber returned True on 69900", Utils.isPortNumber("69900"));
        assertFalse("isPortNumber returned True on 6s54", Utils.isPortNumber("6s54"));
        assertFalse("isPortNumber returned True on 127.0.0.1:80", Utils.isPortNumber("127.0.0.1:80"));
        
    }

    /**
     * I'm sure I missed something here but testing everything that I know or
     * understand about IP Address.
     */
    @Test
    public void isIpAddressTest() {

        assertTrue("isIpAddress returned false on 127.0.0.1", Utils.isIpAddress("127.0.0.1"));
        assertTrue("isIpaddress returned false on 255.255.255.255", Utils.isIpAddress("255.255.255.255"));
        assertTrue("isIpAddress returned false on 1.0.0.1", Utils.isIpAddress("1.0.0.1"));
        // Last octet can be 0
        assertTrue("isIpAddress returned false for 1.1.1.0", Utils.isIpAddress("1.1.1.0"));

        // First octet can't be 0
        assertFalse("isIpAddress returned True for 0.1.1.1", Utils.isIpAddress("0.1.1.1"));
        assertFalse("isIpAddress returned True for 256.2.1.1", Utils.isIpAddress("256.2.1.1"));
        assertFalse("isIpAddress returned True for 1.256.2.1", Utils.isIpAddress("1.256.2.1"));
        assertFalse("isIpAddress returned True for 1.1.256.1", Utils.isIpAddress("1.1.256.1"));
        assertFalse("isIpAddress returned True for 1.1.1.256", Utils.isIpAddress("1.1.1.256"));

    }

    /**
     * The isTrueFalse method should validate or return true for any value that
     * is equal to true or false and case does not matter. Any thing else should
     * cause a false result.
     */
    @Test
    public void isTrueFalseTest() {

        assertTrue("isTrueFalse returned false on false", Utils.isTrueFalse("false"));
        assertTrue("isTrueFalse returned false on true", Utils.isTrueFalse("true"));
        assertTrue("isTrueFalse returned false on False", Utils.isTrueFalse("False"));
        assertTrue("isTrueFalse returned false on True", Utils.isTrueFalse("True"));
        assertTrue("isTrueFlase returned false on FALSE", Utils.isTrueFalse("FALSE"));
        assertTrue("isTrueFalse returned false on TRUE", Utils.isTrueFalse("TRUE"));
        assertTrue("isTrueFalse returned false on faLse", Utils.isTrueFalse("faLse"));
        assertTrue("isTrueFalse returned false on trUe", Utils.isTrueFalse("trUe"));

        assertFalse("isTrueFalse returned true on yes", Utils.isTrueFalse("yes"));
        assertFalse("isTrueFalse returned true on no", Utils.isTrueFalse("no"));
        assertFalse("isTrueFalse returned true on 1", Utils.isTrueFalse("1"));
        assertFalse("isTrueFalse returned true on 0", Utils.isTrueFalse("0"));
        assertFalse("isTrueFalse returned true on trues", Utils.isTrueFalse("trues"));
        assertFalse("isTrueFalse returned true on falsem", Utils.isTrueFalse("flasem"));
        assertFalse("isTureFalse returned true on y", Utils.isTrueFalse("y"));
        assertFalse("isTrueFalse returned true on n", Utils.isTrueFalse("n"));
        assertFalse("isTrueFalse returned true on t", Utils.isTrueFalse("t"));
        assertFalse("isTrueFalse returned trueo on f", Utils.isTrueFalse("f"));
    }

    /**
     * This tests our hostName validation. I'm sure this test can be expanded 
     * like crazy but for now I'm going to just test the general rules and if 
     * there are specific issues I will add them. 
     */
    @Test
    public void isHostNameTest() {
        assertTrue("isHostName returned false for localhost", Utils.isHostName("localhost"));
        assertTrue("isHostName returned false for 1gig.net", Utils.isHostName("igig.net"));
        assertTrue("isHostName returned false for svn.test.com", Utils.isHostName("svn.test.com"));
        assertTrue("isHostName returned false for svn-test.test.com", Utils.isHostName("svn-test.test.com"));
        assertTrue("isHostName returned false for -test.test.com", Utils.isHostName("-test.test.com"));
        // host name not longer than 67
        String testString = "thisdgeuthendbfgjeyshdhb.fjdhghg4hdigyueyhdhdutyeteiwoeruir85flf.com";
        assertFalse("isHostName returned true for a string  > 67 " + testString.length(),
                Utils.isHostName(testString));
        // Host name can't end in -
        assertFalse("isHostName returned true for test.test.co-", Utils.isHostName("test.test-"));
        
        
    }

    /**
     * Just a little utility to print out a configuration exception data as we 
     * will ge doing that allot. 
     * @param e
     */
    private void printConfigException(ConfigurationException e) {
        System.out.println("ConfigurationException Caught.\n Property Name: " +
                e.getProperty() + "\nReason: " + e.getReason());
    }

    /**
     * Test the port configuration options. 
     * 1) At least one port must be defined
     * 2) If both properties for the same port type are defined then they must be equal
     */
    @Test
    public void validateConfTest1() {
        boolean caughtException = false;

        // Condition 1 both sets of port properties are set and they match
        Dictionary<String, String> conf = TestUtils.genDefaultConfig();
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertFalse(caughtException);

        // Condition 2 normal port valid, ssl ports do not match
        String oldPort = conf.get(BundleConstants.CONFIG_OSGI_SECURE_PORT);
        assertTrue(Utils.isANumber(oldPort));
        int port = Integer.parseInt(oldPort);
        port++;
        oldPort = String.format("%d", port);
        conf.put(BundleConstants.CONFIG_OSGI_SECURE_PORT, oldPort);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertTrue(caughtException);
        caughtException = false;

        // Condition 3 normal ports do not match, ssl ports match
        conf = TestUtils.genDefaultConfig();
        oldPort = conf.get(BundleConstants.CONFIG_PORT);
        assertTrue(Utils.isANumber(oldPort));
        port = Integer.parseInt(oldPort);
        port++;
        oldPort = String.format("%d", port);
        conf.put(BundleConstants.CONFIG_PORT, oldPort);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertTrue(caughtException);
        caughtException = false;

        // Condition 4 No ports defined.
        conf.remove(BundleConstants.CONFIG_PORT);
        conf.remove(BundleConstants.CONFIG_OSGI_PORT);
        conf.remove(BundleConstants.CONFIG_SSL_PORT);
        conf.remove(BundleConstants.CONFIG_OSGI_SECURE_PORT);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertTrue(caughtException);
        caughtException = false;

        // Condition 5 normal short hand port defined no other port defined.
        conf.put(BundleConstants.CONFIG_PORT, "80");
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertFalse(caughtException);

        // Condition 6 longhand normal port defined no other port defined.
        conf.remove(BundleConstants.CONFIG_PORT);
        conf.put(BundleConstants.CONFIG_OSGI_PORT, "80");
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertFalse(caughtException);

        // Condition 7 short form ssl port defined no other port defined.
        conf.remove(BundleConstants.CONFIG_OSGI_PORT);
        conf.put(BundleConstants.CONFIG_SSL_PORT, "443");
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertFalse(caughtException);

        // Condition 8 long form ssl port no other defined.
        conf.remove(BundleConstants.CONFIG_SSL_PORT);
        conf.put(BundleConstants.CONFIG_OSGI_SECURE_PORT, "443");
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertFalse(caughtException);
        
    }

    /**
     * In this one we are again testing the port number fields but a differant
     * type of test this time:
     * 1) In the case of port validation failure is the property name correct 
     * for the failling port. This is important as the OSGi framework uses this 
     * to generate an error message that means something to the end user. 
     */
    @Test
    public void validateConfTest2() {
        // some port values to test with..
        String toBigPort = "65536";
        String notAPort = "0x4b";
        boolean caughtException = false;
        String propName = null;

        Dictionary<String, String> conf = TestUtils.genDefaultConfig();
        // Validate our new conf...
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertFalse(caughtException);

        // Wack the port config from the configuration
        conf.remove(BundleConstants.CONFIG_PORT);
        conf.remove(BundleConstants.CONFIG_OSGI_PORT);
        conf.remove(BundleConstants.CONFIG_SSL_PORT);
        conf.remove(BundleConstants.CONFIG_OSGI_SECURE_PORT);

        // Test one set normal port to fail
        conf.put(BundleConstants.CONFIG_PORT, notAPort);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            propName = e.getProperty();
            assertTrue(propName.equals(BundleConstants.CONFIG_PORT));
        }
        assertTrue(caughtException);
        caughtException = false;
        conf.remove(BundleConstants.CONFIG_PORT);

        // test two set normal port (long version) to fail
        conf.put(BundleConstants.CONFIG_OSGI_PORT, toBigPort);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            propName = e.getProperty();
            assertTrue(propName.equals(BundleConstants.CONFIG_OSGI_PORT));
        }
        assertTrue(caughtException);
        caughtException = false;
        conf.remove(BundleConstants.CONFIG_OSGI_PORT);

        // Test ssl port short version
        conf.put(BundleConstants.CONFIG_SSL_PORT, notAPort);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            propName = e.getProperty();
            assertTrue(propName.equals(BundleConstants.CONFIG_SSL_PORT));
        }
        assertTrue(caughtException);
        caughtException = false;
        conf.remove(BundleConstants.CONFIG_SSL_PORT);

        // Test ssl port long version
        conf.put(BundleConstants.CONFIG_OSGI_SECURE_PORT, toBigPort);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            propName = e.getProperty();
            assertTrue(propName.equals(BundleConstants.CONFIG_OSGI_SECURE_PORT));
        }
        assertTrue(caughtException);

    }

    /**
     * Little method to set the supplied property key to the specified value of
     * the supplied Dictionary then run the Utils.validateConf method on the
     * Dictionary. This method will set the property key's value back to what it
     * was before the test was ran.
     *
     * @param conf The configuraton file testing 
     * @param propKey The property name we are testing
     * @param newVal A value to set on the property that you are testing
     * @param shouldPass if the validateConf method is not supposed to throw a
     * ConfigurationException then set this to true. If it will throw the exception
     * then set to false.
     */
    private void testProp(Dictionary<String, String> conf, String propKey,
            String newVal, boolean shouldPass) {
        boolean caughtException = false;
        String oldValue = null;

        oldValue = conf.get(propKey);
        conf.put(propKey, newVal);
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            assertTrue(propKey.equals(e.getProperty()));
        }
        if(shouldPass) {
            assertFalse(caughtException);
        } else {
            assertTrue(caughtException);
        }
        conf.put(propKey, oldValue);
    }

    /**
     * Now that the ports are tested we are going to do all the rest. All we
     * are really testing here is wheather or not on failure if the Property
     * name on the exception is correct. It would be really bad to tell the user
     * that some property was set wrong and we told them the wrong property name.
     */
    @Test
    public void validateConfTest3() {
        boolean caughtException = false;

        Dictionary<String, String> conf = TestUtils.genDefaultConfig();
        // Validate our new conf...
        try {
            Utils.validateConf(conf);
        } catch(ConfigurationException e) {
            caughtException = true;
            printConfigException(e);
        }
        assertFalse(caughtException);

        // Test one ConfigOverride
        testProp(conf, BundleConstants.CONFIG_OVERRIDE_ADMIN, "no", false);

        // Test ipAddress
        testProp(conf, BundleConstants.CONFIG_IP_ADDRESS, "0.1.1.1", false);

        // test Host name
        testProp(conf, BundleConstants.CONFIG_HOSTNAME, "badhost-", false);

        // Test connection Time out mills..
        testProp(conf, BundleConstants.CONFIG_CONNECT_TIMEOUT, "453ter", false);

        // Write time out
        testProp(conf, BundleConstants.CONFIG_WRITE_TIMEOUT, "test", false);

        // reuse address
        testProp(conf, BundleConstants.CONFIG_REUSE_ADDRESS, "no", false);

        // Receive buffer
        testProp(conf, BundleConstants.CONFIG_RECEIVE_BUFFER_SIZE, "notanumber", false);

        // Keep alive
        testProp(conf, BundleConstants.CONFIG_KEEP_ALIVE, "yes", false);

        // tcpNoDelay
        testProp(conf, BundleConstants.CONFIG_TCP_NODELAY, "no", false);

        // Send buffer size
        testProp(conf, BundleConstants.CONFIG_SEND_BUFFER_SIZE, "NotANumber", false);
        
    }

}
