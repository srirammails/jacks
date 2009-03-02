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

import java.util.Dictionary;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.service.cm.ConfigurationException;

/**
 * Just a bunch of static utility methods for various things.
 *
 * @author rjackson
 */
public class Utils {

    /**
     * Very simple method that determins if the input string is a number and is
     * not negative. 
     * 
     * @param value
     * @return True if value is a number otherwise false.
     */
    public static boolean isANumber(String value) {
        if(value == null) return false;
        return Pattern.matches("^\\d+", value);
    }

    /**
     * Method used to validate ports. It first sees if the argument
     * is a number then if it falls within the allowable port range 0-65535 
     * 
     * @param value
     * @return True if this is a valid port number otherwise false.
     */
    public static boolean isPortNumber(String value) {
        if(value == null) return false;
        if(!Pattern.matches("^\\d+", value)) {
            return false;
        }
        int i = Integer.parseInt(value);
        if(i < 0) {
            return false;
        }
        if(i > 65535) {
            return false;
        }
        return true;
    }

    /**
     * Is a given string a IpAddress? Curretnly this only supports V4 IpAddress.
     * Also this does not fail private ip address ranges as our code may run
     * on a private ip address. 
     * 
     * @param value
     * @return
     */
    public static boolean isIpAddress(String value) {
        if(value == null) return false;
        return Pattern.matches(
                "^([1-9]|1\\d{1,2}|2[0-4]\\d|25[0-5])(\\.(\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])){3}$",
                value);
    }

    /**
     * Is the given string equal to either True or False (case insensitive) 
     * 
     * @param value
     * @return
     */
    public static boolean isTrueFalse(String value) {
        if(value == null) return false;
        return value.toUpperCase().matches("TRUE|FALSE");
    }

    /**
     * Validate a give host name
     *
     * NOTE: I pretty much copied this from this source near the bottom of the
     * thread http://regexadvice.com/forums/thread/48588.aspx
     *
     * @param value
     * @return
     */
    public static boolean isHostName(String value) {
        if(value == null) return false;
        // A valid domain can be 63 chars long with out the extension or 67
        // chars long with it. We should be getting them with extenstions so
        //  we allow the 67 length.
        if(value.length() > 67) {
            return false;
        }
        Pattern p = Pattern.compile("^(?=.*?[a-z])(?!\\.)[a-z\\d.-]*[a-z\\d]$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(value);
        return m.matches();
    }

    /**
     * This method is used to validate a configuration. This method is called
     * any time we need to validate a configuration.
     *
     * @param conf
     * @throws org.osgi.service.cm.ConfigurationException Thown if the
     * configuration is invalid.
     */
    public static void validateConf(Dictionary conf) throws ConfigurationException {

        // If set must be True or False NOTE: We will only see this key when
        // validating property files.
        String value = (String)conf.get(BundleConstants.CONFIG_OVERRIDE_ADMIN);
        if(value != null) {
            if(!isTrueFalse(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_OVERRIDE_ADMIN,
                        "This field must be True or False");
            }
        }

        // Test that at least one port is defined. and if both ports of the
        // same type are defined they must be equal.
        value = (String)conf.get(BundleConstants.CONFIG_OSGI_PORT);
        String value2 = (String)conf.get(BundleConstants.CONFIG_PORT);
        if(value == null && value2 == null) {
            // Check for SSL ports
            value2 = (String) conf.get(BundleConstants.CONFIG_SSL_PORT);
            value = (String)conf.get(BundleConstants.CONFIG_OSGI_SECURE_PORT);
            if(value == null && value2 == null) {
                throw new ConfigurationException(BundleConstants.CONFIG_PORT,
                        "At least one port must be defined.");
            } else {
                if(value != null && value2 != null) {
                    if(!value.equals(value2)) {
                        String propNames = BundleConstants.CONFIG_OSGI_SECURE_PORT +
                                " and " + BundleConstants.CONFIG_SSL_PORT;
                        throw new ConfigurationException(propNames, 
                                "These must be equal if they are both defined.");
                    }
                }
            }

        } else {
            if(value != null && value2 != null) {
                if(!value.equals(value2)) {
                    String propNames = BundleConstants.CONFIG_OSGI_PORT + " and " +
                            BundleConstants.CONFIG_PORT;
                    throw new ConfigurationException(propNames,
                            "These must be equal if they are both defined.");
                }
            }
            // At this point we have not checked the SSL ports so we do that here
            value = (String)conf.get(BundleConstants.CONFIG_SSL_PORT);
            value2 = (String)conf.get(BundleConstants.CONFIG_OSGI_SECURE_PORT);
            if(value != null && value2 != null) {
                if(!value.equals(value2)) {
                    String propNames = BundleConstants.CONFIG_SSL_PORT + " and " +
                            BundleConstants.CONFIG_OSGI_SECURE_PORT;
                    throw new ConfigurationException(propNames,
                            "These must be equal if they are both defined.");
                }
            }
        }

        // Now we must determin if the port(s) are defined correctly.
        value = (String)conf.get(BundleConstants.CONFIG_PORT);
        if(value != null) {
            if(!isPortNumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_PORT,
                        "Is not a valid port number");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_OSGI_PORT);
        if(value != null) {
            if(!isPortNumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_OSGI_PORT,
                        "Is not a valid port number");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_SSL_PORT);
        if(value != null) {
            if(!isPortNumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_SSL_PORT,
                        "Is not a valid port number");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_OSGI_SECURE_PORT);
        if(value != null) {
            if(!isPortNumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_OSGI_SECURE_PORT,
                        "Is not a valid port number");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_IP_ADDRESS);
        if(value != null) {
            if(!isIpAddress(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_IP_ADDRESS,
                        "Is not a valid IP Address");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_HOSTNAME);
        if(value != null) {
            if(!isHostName(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_HOSTNAME,
                        "Invalid Host name.");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_CONNECT_TIMEOUT);
        if(value != null) {
            if(!isANumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_CONNECT_TIMEOUT,
                        "This field must be a number.");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_WRITE_TIMEOUT);
        if(value != null) {
            if(!isANumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_WRITE_TIMEOUT,
                        "This field must be a number.");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_REUSE_ADDRESS);
        if(value != null) {
            if(!isTrueFalse(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_REUSE_ADDRESS,
                        "This field must be set to True or False");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_RECEIVE_BUFFER_SIZE);
        if(value != null) {
            if(!isANumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_RECEIVE_BUFFER_SIZE,
                        "This field must be a number.");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_KEEP_ALIVE);
        if(value != null) {
            if(!isTrueFalse(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_KEEP_ALIVE,
                        "This field must be set to True or False.");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_TCP_NODELAY);
        if(value != null) {
            if(!isTrueFalse(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_TCP_NODELAY,
                        "This field must be set to True or False.");
            }
        }

        value = (String)conf.get(BundleConstants.CONFIG_SEND_BUFFER_SIZE);
        if(value != null) {
            if(!isANumber(value)) {
                throw new ConfigurationException(BundleConstants.CONFIG_SEND_BUFFER_SIZE,
                        "This field must be set to True or False.");
            }
        }

    }

}
