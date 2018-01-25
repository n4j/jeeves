package io.artofcode;

import junit.framework.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

public class TestHelper {

    private final static String CONFIG_PATH = "config";
    
    private static Logger logger;

    static {
        try{
            logger = Logger.getLogger(TestHelper.class.toString());
            Path configDir = getConfigDir();
            System.setProperty("JEEVES_CONFIG", configDir.toString());

            copyConfigFile(configDir, "default");
            copyConfigFile(configDir, "url-crawlers");
            copyConfigFile(configDir, "url-crawlers-test");
        }catch (IOException ioe) {
            logger.log(Level.SEVERE, "Unable to setup environment. Tests may not run correctly", ioe);
        }
    }

    private TestHelper() {

    }

    public static void setupEnvironment() {
        // Stub method as this needs to be done only once
        // when JVM starts. This method basically ensures that
        // environment is setup no matter which test is executed first
    }

    private static Path getConfigDir() throws IOException {
        Path configDir = Files.createTempDirectory(CONFIG_PATH, new FileAttribute[0]);
        return configDir;
    }

    private static void copyConfigFile(Path configDir, String configFileName) {
        try {
            URL configURL = ClassLoader.getSystemResource(configFileName);
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
