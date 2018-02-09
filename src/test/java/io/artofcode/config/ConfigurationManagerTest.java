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
import java.util.Map;
import java.util.logging.Logger;

import io.artofcode.TestHelper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConfigurationManagerTest extends TestCase {

    public ConfigurationManagerTest( String testName ) throws IOException {
        super( testName );

        TestHelper.setupEnvironment();
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

}
