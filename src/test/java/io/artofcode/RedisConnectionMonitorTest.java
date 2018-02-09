package io.artofcode;

import static java.lang.String.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisConnectionMonitorTest extends TestCase {

    private final Logger logger = Logger.getLogger(RedisConnectionMonitorTest.class.toString());

    public RedisConnectionMonitorTest(String testName) {
        super(testName);
    }

    public static Test suite() { return  new TestSuite(RedisConnectionMonitorTest.class); }

    private boolean isCI() {
        String ci = System.getenv("CI");
        return ci != null &&
                ci.compareToIgnoreCase("true") == 0;
    }

    private boolean isLinuxEnvironment() {
        String osName = System.getenv("TRAVIS_OS_NAME");
        return osName != null &&
            osName.compareToIgnoreCase("linux") == 0;
    }

    private void simulateNetworkInterruption() {
        try {

            Process process = Runtime.getRuntime()
                    .exec("sudo service redis-server restart");

            try(BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = err.readLine()) != null)
                    logger.severe(line);
            }
            try(BufferedReader in = new BufferedReader( new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null)
                    logger.info(line);
            }

            int returnCode = process.waitFor();
            logger.info(format("Process exited with code %d", returnCode));
            assertTrue(returnCode == 0);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error while executing 'sudo service redis-server restart'", ioe);
        } catch (InterruptedException ie) {
            logger.log(Level.SEVERE, "Error while executing 'sudo service redis-server restart'", ie);
        }
    }

    public void testReconnection() {
        /**
         * This test case will only execute in a CI environment
         * */
        if(!(isCI() && isLinuxEnvironment())) {
            logger.info("Not a CI environment so skipping...");
            return;
        }

        RedisQueueManager manager = new RedisQueueManager("default");
        manager.createWorkerId();
        simulateNetworkInterruption();
        long workerId =  manager.createWorkerId();

        assertTrue (workerId > 0);
    }
}
