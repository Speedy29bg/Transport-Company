package bg.company.transport.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static Properties properties;

    private ConfigLoader() {
    }

    public static Properties load() {
        Properties props = new Properties();
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration", e);
        }
        log.debug("Loaded configuration with keys: {}", props.stringPropertyNames());
        return props;
    }

    public static Properties getProperties() {
        if (properties == null) {
            properties = load();
        }
        return properties;
    }

    public static void loadDatabaseProperties() {
        getProperties();
    }
}
