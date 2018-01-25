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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.String.format;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConfigurationManagerTest extends TestCase {

    private final static String CONFIG_PATH = "config";

    private final Logger logger = Logger.getLogger(ConfigurationManagerTest.class.toString());

    public ConfigurationManagerTest( String testName ) throws IOException {
        super( testName );
        Path configDir = Files.createTempDirectory(CONFIG_PATH, new FileAttribute[0]);
        setupEnvironment(configDir);
        copyConfigFile(configDir, "default");
        copyConfigFile(configDir, "url-crawlers");
    }

    public static Test suite() {
        return new TestSuite( ConfigurationManagerTest.class );
    }

    public void testDefaultConfig() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        assertTrue(configManager != null);

        Map<String, String> defaultProps = configManager.get("default");
        assertTrue(defaultProps.get("NUM_RETRIES").equals("5"));
        assertTrue(defaultProps.get("REDIS_HOST").equals("localhost"));
        assertTrue(defaultProps.get("REDIS_PORT").equals("6379"));
        assertTrue(defaultProps.get("HEART_BEAT").equals("5000s"));
    }

    public void testOverlappingConfig() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        assertTrue(configManager != null);

        Map<String, String> defaultProps = configManager.get("url-crawlers");
        assertTrue(defaultProps.get("NUM_RETRIES").equals("5"));
        assertTrue(defaultProps.get("REDIS_HOST").equals("ec2-10-10-142-132.aws.amazon.com"));
        assertTrue(defaultProps.get("REDIS_PORT").equals("6379"));
        assertTrue(defaultProps.get("HEART_BEAT").equals("2500s"));
    }

    private void setupEnvironment(Path configDir) {
            System.setProperty("JEEVES_CONFIG", configDir.toString());
    }

    private void copyConfigFile(Path configDir, String configFileName) {
        try {
            URL configURL = getClass().getClassLoader().getResource(configFileName);
            if(configURL == null) {
                logger.severe(format("Unable to locate config file %s. Tests will not pass.", configFileName));
            } else {
                logger.info(format("Config directory is %s", configDir.toString()));
                Path configPath = Paths.get(configDir.toString(), configFileName);
                Files.copy(configURL.openStream(), configPath);
                logger.info(format("Config file is %s", configPath.toString()));
            }
        }catch(NullPointerException | IOException e) {
            logger.log(Level.SEVERE, format("Error while copying resources to path %s", configDir), e);
        }
    }

}
