/*
Copyright 2018 Neeraj Shah

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.artofcode.config;

import static java.io.File.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationManager {

    private static final String[] configKeys = {
       "NUM_RETRIES",
       "REDIS_HOST",
       "REDIS_PORT" , 
       "HEART_BEAT"
    };

    private static ConfigurationManager instance;

    private static Map<String, Map<String, String>> properties;

    private static Logger logger = Logger.getLogger(ConfigurationManager.class.toString());

    static {
        synchronized(ConfigurationManager.class) {
            validateConfigPath();
            try {
                instance = new ConfigurationManager();
                properties = loadConfigProperties();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ConfigurationManager() {

    }

    public static ConfigurationManager getInstance() {
        return instance;
    }

    public Map<String, String> get(String configName) {
        if(properties.containsKey(configName)){
            return new HashMap<>(properties.get(configName));
        }
        return new HashMap<>(properties.get("default"));
    }

    private static Map<String, Map<String, String>> loadConfigProperties() {
        Map<String, Map<String, String>> allProperties = new HashMap<>();
        
        String configPath = getDefaultConfigPath();

        File configDir = new File(configPath);
        File[] files = configDir.listFiles((file) -> file.isFile());
        
        Properties defaultProperties = getDefaultConfig();
        allProperties.put("default", propsToMap(defaultProperties));

        for(int i = 0; i<files.length; i++) {

            // Skip over default config file as we have already loaded it
            if(files[i].equals("default")) {
                continue;
            }

            Properties property = new Properties(defaultProperties);
            try {
                try(FileReader reader = new FileReader(files[i])) {
                    property.load(new FileReader(files[i]));
                    allProperties.put(
                            removeExtension(files[i].getName()),
                            propsToMap(property)
                    );
                }
            } catch(IOException ioe) {
                continue;
            }
        }
        return allProperties;
    }

    private static String getDefaultConfigPath() {
       String path = System.getProperty("JEEVES_CONFIG");

       if(path != null) {
        return path;
       }

       return System.getenv("JEEVES_CONFIG");
    }

    private static Properties getDefaultConfig() {
        Properties envConfig = getEnvironmentConfig();
        Properties defaultProps = new Properties(envConfig);
        String path = getDefaultConfigPath();

        if(path == null) {
            return defaultProps;
        }

        try {
            defaultProps.load(new FileReader(path + separator + "default"));
        } catch(IOException ioe) {
            logger.info(String.format("Unable to find default configuration file at %s", path));
            logger.log(Level.INFO, "IOException while loading default configuration", ioe);
        }

        return defaultProps;
    }

    private static Properties getEnvironmentConfig() {
        Properties envProperties = new Properties();
        for(int i=0; i<configKeys.length; i++) {
            String env = System.getenv(configKeys[i]);
            if(env == null || env.isEmpty()) {
                continue;
            }
            envProperties.setProperty(configKeys[i], env);
        }
        return envProperties;
    }

    private static Map<String, String> propsToMap(Properties properties) {
        Map<String, String> propsMap = new HashMap<>();

        // Have to do it this way because Properties.entrySet() doesn't
        // return default properties, which is crucial for us.
        Enumeration<?> keyNames = properties.propertyNames();
        while(keyNames.hasMoreElements()){
            String keyName = keyNames.nextElement().toString();
            propsMap.put(keyName, properties.getProperty(keyName));
        }

        return propsMap;
    }

    private static String removeExtension(String file) {
        int lastDot = file.lastIndexOf(".");
        if(lastDot == -1)
            return file;
        return file.substring(0, lastDot);
    }

    private static void validateConfigPath() {
        String configPath = getDefaultConfigPath();
        if(configPath == null)
            throw new RuntimeException("JEEVES_CONFIG variable not set, JEEVES_CONFIG must point to a directory"+
                    "containing configuration files for Jeeves. It can be set via environment variable or via -D option");
    }

    @SuppressWarnings("unused")
    private ConfigurationManager readResolve() {

        return instance;
    }
}