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

import org.jhserv.jacks.httpservice.server.HttpServer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jhserv.jacks.httpservice.servicetracker.LogTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This class will create/update/delete our HttpServices. And also add trackers
 * for any of our dependancies.
 *
 * @author rjackson
 */
public class HttpManagedServiceFactory implements ManagedServiceFactory {

    private volatile BundleContext context;
    // our started servers. <pid (osgi service pid), HttpServer instance> for
    // Managed services only.
    private final Map<String, HttpServer> managedServices =
            new ConcurrentHashMap<String, HttpServer>();
    // our started servers <port, HttpServer instance> for unmanaged services
    // only. NOTE: When a managed service is migrated to a unmanaged service
    // the service pid will be used as the key instead of the port number.
    private final Map<String, HttpServer> unManagedServices =
            new ConcurrentHashMap<String, HttpServer>();

    // Log tracker
    private final LogTracker log = LogTracker.getInstance();
    // The service referance for our HttpManagedServiceFactory. It should be
    // noted that this can potentially change over time. This can potentially
    // happen accross multiple thread so this must be thread safe.
    private final AtomicReference<ServiceRegistration> registration = 
            new AtomicReference<ServiceRegistration>();
    // Flag that will be set by our Configuration Service tracker to indicate
    // wheather or not the Configuration Admin Service is availible. This will
    // almost always be on another thread than what this code would normally
    // be running on. So this must be thread safe.
    private final AtomicBoolean cmAvailible = new AtomicBoolean();

    // Have we started any servers yet?
    private final AtomicBoolean serversStarted = new AtomicBoolean();

    // This will be set at start up and then read only after that. Mostly just
    // to stop it.
    private ServiceTracker cmTracker = null;

    // This is the OSGi PID we will use for registring our service factory. 
    //
    private final String factoryPid = HttpManagedServiceFactory.class.getName();

    /**
     * Selector used to find our configuration data in the config admin service.
     */
    public static final String CONFIG_SELECTOR = "(service.factoryPid=" +
            HttpManagedServiceFactory.class.getName()  + ")";

    public HttpManagedServiceFactory(BundleContext context) {
        this.context = context;
    }

    /**
     * This method is used to register this ManagedServiceFactory into the 
     * framework and to setup our default configuration. This method must be 
     * called to Register our factory into the framework. This should be called 
     * in a thread other than the one that called our bundle activator Because
     * we are doing some things that could potentually block (reading files).
     *
     */
    public void start() {
        log.info("Starting the HttpManagedServiceFactory...... ");

        cmTracker = new ServiceTracker(context, ConfigurationAdmin.class.getCanonicalName(),
                new cmTrackerCustomizer() );
        cmTracker.open();

        Map<String, Properties> confMap = loadConfigFiles();

        if(confMap.size() == 0) {
            confMap = loadDefaultConfig();
        }

        // Setup as a managed service factory
        if(cmAvailible.get()) {
            cleanAndLoadCM(confMap);
            // As soon as we do the below we go multithreaded because the
            // ConfiguationAdmin service will start to spawn threads and
            // call our updated method which will then register our
            // HttpService stuff..
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put( Constants.SERVICE_PID, factoryPid);
            registration.set(context.registerService(
                    ManagedServiceFactory.class.getName(), this, properties));
            log.debug("HttpManagedService registered and started...");
            
        } else {
            // set ourselfs up as a unmanaged service. And later if the Admin
            // service shows up we will add our self as a managed service.
            log.debug("ConfigurationAdmin service unavalible starting service as a" +
                    " unmanaged service.");
            if(confMap.isEmpty()) {
                // Should not get this because of default config. but Still
                log.debug("No servers defined at this time. Waitting for Configuration" +
                        " Admin service to start and pass us our configuration.");
                return;
            }

            Set<String> keySet = confMap.keySet();
            for(String key: keySet) {
                Properties props = confMap.get(key);
                List<String> reqPorts = findReqPorts(props);
                if(arePortsAvailable(reqPorts)) {
                    HttpServer server = new HttpServer(context);
                    server.start(props);
                    unManagedServices.put(reqPorts.get(0), server);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The requested port(s) ( ");
                    for(String port : reqPorts) {
                        sb.append(port + ",");
                    }
                    sb.append(" ) defined in configuration file ");
                    sb.append(key);
                    sb.append(" are already in use. Skipping this configuration file.");
                }
            }
            if(!unManagedServices.isEmpty()) {
                serversStarted.set(true);
            }
        }
    }

    /**
     * This method is used to clean ourself up and shut down the Factory.
     */
    public void stop() {
        log.info("Stopping the HttpManagedServiceFactory....");
        cmTracker.close();
        context.ungetService(registration.get().getReference());
        registration.set(null);
        // Stop our servers/Services
        if(!managedServices.isEmpty()) {
            Set<String> pids = managedServices.keySet();
            for(String key: pids) {
                HttpServer server = managedServices.remove(key);
                server.stop();
                server = null;
            }
            if(!managedServices.isEmpty()) {
                log.debug("When shutting down we did not clean our managedServices map out completly!");
                log.debug("We have " + managedServices.size()+ " services still defined.");
            }
        }
        if(!unManagedServices.isEmpty()) {
            Set<String> pids = unManagedServices.keySet();
            for(String key: pids) {
                HttpServer server = unManagedServices.remove(key);
                server.stop();
                server = null;
            }
            if(!unManagedServices.isEmpty()) {
                log.debug("When shutting down we did not clean our unManagedServices map out completly!");
                log.debug("We have " + unManagedServices.size() + " services still defined.");
            }
        }
        serversStarted.set(false);
    }

    @Override
    public String getName() {
        return "jhserv.org HTTPService ManagedService Factory";
    }

    /**
     * Called when we have new or updated configuration data. This method will
     * start are servers when we are running as a managed service (ConfigAdmin
     * service is running).
     *
     * @param pid
     * @param config
     * @throws org.osgi.service.cm.ConfigurationException
     */
    @Override
    public void updated(String pid, Dictionary config) throws ConfigurationException {
        // Validate configuration first... Throws ConfigurationException if
        // validation fails.
        Utils.validateConf(config);

        // existing service?
        if(managedServices.containsKey(pid)) {
            managedServices.get(pid).update(config);
            return;
        }

        // was service migrated to unManagedService?
        if(unManagedServices.containsKey(pid)) {
            HttpServer server = unManagedServices.remove(pid);
            managedServices.put(pid, server);
            server.update(config);
            return;
        }

        // NOTE: At this point there should be at least one port defined the
        // validateConf call above checks that and will throw an exception if
        // at least one port is not defined.
        List<String> reqPorts = findReqPorts(config);

        // did service start out as a unmanaged service?
        if(!unManagedServices.isEmpty()) {
            for(String port: reqPorts) {
                if(unManagedServices.containsKey(port)) {
                    HttpServer server = unManagedServices.remove(port);
                    managedServices.put(pid, server);
                    server.update(config);
                    return;
                }
            }
            
        }

        // If we got this far then a server for this configuration has not
        // been started so lets do that.
        log.debug("A new configuration was recieved from the Configuration Admin service." +
                " With a PID of : " + pid);

        // Are the requested ports availible?
        if(arePortsAvailable(reqPorts))  {
            log.debug("The requested ports are already in use. Throwing ConfigurationException.");
            log.debug("Requested ports : " + reqPorts.toString());
            ConfigurationException ce = new ConfigurationException(BundleConstants.CONFIG_PORT,
                    "The requested ports for this configuration are already in use.");
        }
        
        log.debug("Starting new Server.");
        HttpServer server = new HttpServer(context);
        server.start(config);
        managedServices.put(pid, server);

        if(!serversStarted.get()) {
            serversStarted.set(true);
        }

    }

    /**
     * Called when one of our configurations have been deleted. In this case we
     * will stop the server that is asciated with this configuration.
     * 
     * @param pid
     */
    @Override
    public void deleted(String pid) {
        log.info("Stoping HttpService asciated with PID : " + pid
                + " because configuration was deleted.");
        if(managedServices.containsKey(pid)) {
            HttpServer server = managedServices.remove(pid);
            server.stop();
        } else {
            // We really should not get here but I can think of a case that it
            // might happen so I put this here to handle it.
            if(unManagedServices.containsKey(pid)) {
                HttpServer server = unManagedServices.remove(pid);
                server.stop();
            }
        }
        if(managedServices.isEmpty() && unManagedServices.isEmpty()) {
            serversStarted.set(false);
        }
        
    }

    /**
     * This method is used to load our configuration files. It will load all
     * configuration files that meat the search constraints. Currently this is
     * defined as starting with the constant CONFIG_FILE_FILTER.
     *
     * @return map of found properties files with the file name as the key
     * (The extension is removed just the first part of the file name is used for
     * the key). The return type will never be null. In the case that no config
     * files were found the map will be empty.
     */
    private Map<String, Properties> loadConfigFiles() {
        Map<String, Properties> confMap = new ConcurrentHashMap<String, Properties>();

        File confDir = new File(BundleConstants.CONFIG_DIR);
        log.debug("Configuration File path : " + confDir.getAbsolutePath());

        File[] confFiles = confDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if(f.getName().toLowerCase().startsWith(BundleConstants.CONFIG_FILE_FILTER)) {
                    return true;
                }
                return false;
            }
        });

        if((confFiles != null) && (confFiles.length > 0)) {
            for(File f: confFiles) {
                log.debug("Found configuration file: " + f.getName());
                FileInputStream inFile = null;
                try {
                    Properties props = new Properties();
                    inFile = new FileInputStream(f);
                    props.load(inFile);
                    Utils.validateConf(props);
                    String key = f.getName().split(".")[0];
                    confMap.put(key, props);
                } catch(IOException e) {
                    log.warn("Reading " + f.getAbsoluteFile() + " caused a IOException.", e);
                } catch(ConfigurationException e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Configuration File: " + f.getName() + " is invalid!\n");
                    sb.append("Property: " + e.getProperty() + "\n");
                    sb.append("Reason  : " + e.getReason() + "\n");
                    sb.append("This configuration file will be skipped.");
                    log.warn(sb.toString());
                } finally {
                    if(inFile != null) {
                        try {
                            inFile.close();
                        } catch(IOException e) {
                            log.warn("Closing " + f.getAbsoluteFile() +
                                    " caused a IOException." , e);
                        }
                        inFile = null;
                    }
                }
            }
        }
        return confMap;
    }

    /**
     * This method will load a default configuration for us if no configuration
     * files are found. The key for this will allways be default. It should be
     * noted that if no config file is found the default config will not override
     * what is defined in the Config Admin service. 
     *
     * @return
     */
    private Map<String, Properties> loadDefaultConfig() {
        Map<String, Properties> confMap = new ConcurrentHashMap<String, Properties>();

        Properties props = new Properties();
        // See if the defined OSGi properties are defined in the system config.
        String tempProp = context.getProperty(BundleConstants.CONFIG_OSGI_PORT);
        if(tempProp != null && !tempProp.trim().isEmpty()) {
            props.setProperty(BundleConstants.CONFIG_PORT, tempProp);
        } else  {
            props.setProperty(BundleConstants.CONFIG_PORT, BundleConstants.CONFIG_OSGI_PORT_DEFAULT);
        }
        tempProp = context.getProperty(BundleConstants.CONFIG_OSGI_SECURE_PORT);
        if(tempProp != null && !tempProp.trim().isEmpty()) {
            props.setProperty(BundleConstants.CONFIG_SSL_PORT, tempProp);
        }
        // If the above is not set we will not set it by default.
        confMap.put(BundleConstants.CONFIG_PROPS_DEFAULT, props);

        return confMap;
    }

    /**
     * This method will determin if the current instance of the configuration 
     * admin service has any configuration data already loaded if it does and 
     * the config files we read in have the flag set to blow it away we will do 
     * so. Then set our current configuration data into the service. 
     * 
     * We need to do this before registering our factory or we could end up running
     * in circles. 
     */
    private boolean cleanAndLoadCM(Map<String, Properties> confData) {
        try {

            ConfigurationAdmin ca = (ConfigurationAdmin)cmTracker.getService();
            Configuration[] caConfigArray = ca.listConfigurations(CONFIG_SELECTOR);

            if(caConfigArray == null || caConfigArray.length == 0) {
                // Load default data....
                log.debug("No Configuration data found in ConfigurationAdmin service. " +
                        "Adding configuration data found in config files or our default config.");
                for(String confKey: confData.keySet()) {
                    Properties props = confData.get(confKey);
                    Configuration caData = ca.createFactoryConfiguration(factoryPid);
                    caData.update(props);
                    log.debug("created new CM configuration...");
                    StringBuilder sb = new StringBuilder();
                    sb.append("What we added to ConfigAdmin;\n");
                    for(String key: props.stringPropertyNames()) {
                        String value = props.getProperty(key);
                        sb.append(key + " => " + value + "\n");
                    }
                    log.debug(sb.toString());
                }
            } else {
                // Validate existing Configuration data. Either leave it as is
                // Or replace it depending on flags in the configuration file.
                log.debug("ConfigurationAdmin already has configuration data for us.");
                for(Configuration caConf: caConfigArray) {
                    Properties foundProp = findMatch(caConf, confData);
                    if(foundProp == null) {
                        log.debug("Found ConfigurationAdmin Configuration without matching loaded properties file." +
                                "Leaving as is.");
                        continue;
                    }
                    if(foundProp.isEmpty()) {
                        log.debug("Found invalid CM Configuration we are going to blow it away.");
                        caConf.delete();
                        continue;
                    }
                    String blowAway = foundProp.getProperty(BundleConstants.CONFIG_OVERRIDE_ADMIN);
                    if(blowAway != null && blowAway.trim().equalsIgnoreCase("true")) {
                        log.debug("Replacing existing configuration with new configuration.");
                        // Need to test this the OSGi documentation is vauge on
                        // How this is supposed to work. Need to verify if
                        // keys not defined in the prop we are using for updating
                        // will be removed from the configuration object.
                        caConf.update(foundProp);
                    } else {
                        log.debug("Found matching configuration with out blowAway flag set. " +
                                "Leaving as is.");
                    }

                }
            }
        } catch (IOException e) {
            log.error("Caught a IOException when calling listConfigurations on " +
                    "the ConfigurationAdmin Service.", e);
            return false;
        } catch (InvalidSyntaxException e) {
            log.error("Caught a InvalidSyntaxException when calling listConfigurations " +
                    "on the ConfigurationAdmin service.", e);
            return false;
        }
        return true;
    }

    /**
     * Simple method to find a matching Properties for a given OSGi Configuration
     * object. If a matching properties could not be found null will be returned.
     *
     * @param cmConf
     * @param confData
     * @return
     */
    private Properties findMatch(Configuration cmConf, Map<String, Properties> confData) {

        String port = (String)cmConf.getProperties().get(BundleConstants.CONFIG_PORT);
        if(port == null || port.trim().isEmpty()) {
            // We should not see this but just the same..
            return new Properties();    // send back a empty property object.
        }
        for(String mapKey: confData.keySet()) {
            Properties props = confData.get(mapKey);
            String confPort = props.getProperty(BundleConstants.CONFIG_PORT);
            if(confPort == null || confPort.trim().isEmpty()) {
                // Again we should not see this but just the same...
                continue;
            }
            if(port.equals(confPort)) {
                return props;
            }
        }
        return null;
    }

    /**
     * register the managed service factory if it is not already registered. This is 
     * primarily used to start/restart the factory when the configuration admin 
     * service is registered after we have already started. 
     */
    private void registerFactory() {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, factoryPid);
        registration.set(context.registerService(
                ManagedServiceFactory.class.getName(), this, properties));
        log.debug("HttpManagedService registered and started...");
    }

    /**
     * unregister the managed service factory in the case that the configuration
     * admin service goes away while we are running.
     */
    private void unregisterFactory() {
        context.ungetService(registration.get().getReference());
        registration.set(null);
        // Move our managed services to unmanaged services..
        if(!managedServices.isEmpty()) {
            Set<String> pids = managedServices.keySet();
            for(String key: pids) {
                HttpServer server = managedServices.remove(key);
                unManagedServices.put(key, server);
            }
            if(!managedServices.isEmpty()) {
                log.debug("In unregisterFactory we did not migrate all of our servers.");
                log.debug("We had " + managedServices.size() + " servers left.");
            }
        }
    }

    /**
     * Query all the running servers and verify that the requested port(s) are not 
     * already in use. 
     * 
     * @param conf
     * @return
     */
    private boolean arePortsAvailable(List<String> reqPorts) {
        // Gather the ports alread in use.
        List<String> usedPorts = new ArrayList<String>();
        if(!managedServices.isEmpty()) {
            Set<String> pids = managedServices.keySet();
            for(String key: pids) {
                usedPorts.addAll(managedServices.get(key).getPorts());
            }
        }
        if(!unManagedServices.isEmpty()) {
            Set<String> pids = unManagedServices.keySet();
            for(String key: pids) {
                usedPorts.addAll(unManagedServices.get(key).getPorts());
            }
        }
        // Really should not see this but if we have not used any ports then 
        // all ports are availible.
        if(usedPorts.isEmpty()) {
            return true;
        }

        for(String rPort: reqPorts) {
            if(usedPorts.contains(rPort)) {
                log.debug("The requested port " + rPort + " is already in use.");
                return false;
            }
        }
        return true;
    }

    /**
     * Get all requested port(s) defined in the configuration dictionary. 
     * 
     * @param conf
     * @return
     */
    private List<String> findReqPorts(Dictionary conf) {
        List<String> reqPorts = new ArrayList<String>();
        
        String value = (String)conf.get(BundleConstants.CONFIG_PORT);
        if(value != null) {
            reqPorts.add(value);
        } else {
            // Only put one of these in because they have to be equal.
            value = (String)conf.get(BundleConstants.CONFIG_OSGI_PORT);
            if(value != null) {
                reqPorts.add(value);
            }
        }
        value = (String)conf.get(BundleConstants.CONFIG_SSL_PORT);
        if(value != null) {
            reqPorts.add(value);
        } else {
            // Only put one of these in because they have to be equal.
            value = (String)conf.get(BundleConstants.CONFIG_OSGI_SECURE_PORT);
            if(value != null) {
                reqPorts.add(value);
            }
        }

        return reqPorts;
    }

    /**************************************************************************
     * Inner classes
     */

    /**
     * ServiceTrackerCustomizer implementation so that we can Correctly react 
     * to the Configuration Admin service starts and stops. 
     */
    private class cmTrackerCustomizer implements ServiceTrackerCustomizer {

        @Override
        public Object addingService(ServiceReference sr) {
            cmAvailible.set(true);
            log.debug("Configuration Admin Service added to our tracker. ");
            // If we have started some servers and factory not running..
            // or otherwords we are out of our setup stage.
            if(serversStarted.get() && registration.get() == null) {
                log.debug("Registering our HttpManagedServiceFactory..");
                registerFactory();
            }
            return context.getService(sr);
        }

        @Override
        public void modifiedService(ServiceReference sr, Object notused) {
            log.debug("Our tracked Configuration Admin Service has been modified.");
        }

        @Override
        public void removedService(ServiceReference sr, Object notused) {
            cmAvailible.set(false);
            log.debug("The Configuration Admin Service has been removed from our tracker");
            if(registration.get() != null) {
                log.debug("Unregistering our HttpManagedServiceFactory");
                unregisterFactory();
            }
        }

    }

}
