package systems.whitestar;

import lombok.extern.log4j.Log4j;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * @author Tom Paulus
 * Created on 9/29/17.
 */
@Log4j
public class Init implements ServletContextListener {
    private static final String DEFAULTS_FILE = "defaults.properties";
    private static final String CONTACT_TOPIC_NAME = "WSSContactFormSubmissions"; // Camel Case & No Special Characters
    private static final String DEFAULT_SUBSCRIBER = "admin@whitestar.systems";

    public void contextInitialized(ServletContextEvent sce) {
        try {
            loadDefaults();
        } catch (IOException e) {
            log.error("Could not load Default Properties File");
        } catch (RuntimeException e) {
            log.error("Problem Loading Defaults to Vault", e);
        }

        initSNS();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // TODO
    }

    private void loadDefaults() throws IOException {
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream(DEFAULTS_FILE));

        for (String property : properties.stringPropertyNames()) {
            String propertyValue = null;

            try {
                log.debug("Getting value of property - " + property);
                propertyValue = Secret.getInstance().getSecret(property);
                log.debug("Value of property " + property + " is - " + propertyValue);
            } catch (RuntimeException e) {
                log.warn("Problem getting value of property - " + property, e);
            }

            if (propertyValue == null) {
                log.info("Value for property " + property + " is not set. Setting to default value.");
                log.debug("Default value for property " + property + " is \"" + properties.getProperty(property) + "\"");

                Map<String, String> secrets;
                try {
                    secrets = Secret.getInstance().getApplication();
                } catch (RuntimeException e1) {
                    log.warn("Application does not yet exist. Creating.");
                    secrets = new TreeMap<>();
                }

                secrets.put(property, properties.getProperty(property));

                final TreeMap<String, Object> map = new TreeMap<>();
                map.putAll(secrets);
                Secret.getInstance().setSecret(map);
            }
        }
    }

    private void initSNS() {
        String topicName = null;
        String topicArn = null;

        try {
            topicName = Secret.getInstance().getSecret("sns.contact_topic.name");
            topicArn = Secret.getInstance().getSecret("sns.contact_topic.arn");

            log.debug("SNS Topic for Contact Form: " + topicName);
            log.debug("SNS Topic ARN for Contact Form: " + topicArn);
        } catch (RuntimeException e) {
            log.warn("Problem getting value of property SNS Topic and ARN", e);
        }

        if (topicName == null || topicArn == null) {
            log.warn("No SNS Topic has been set for Contact Form. Creating new Topic");
            final String topicARN = SNS.getInstance().createTopic(CONTACT_TOPIC_NAME);

            final Map<String, String> secrets = Secret.getInstance().getApplication();
            secrets.put("sns.contact_topic.arn", topicARN);
            secrets.put("sns.contact_topic.name", CONTACT_TOPIC_NAME);

            final TreeMap<String, Object> map = new TreeMap<>();
            map.putAll(secrets);
            Secret.getInstance().setSecret(map);

            log.info("Subscribing default subscriber to newly created topic");
            log.debug("Default Subscriber - " + DEFAULT_SUBSCRIBER);

            SNS.getInstance().subscribeToTopic(topicARN, "email", DEFAULT_SUBSCRIBER);
        } else {
            Map<String, String> attributes = SNS.getInstance().getTopic(topicArn);

            if (attributes == null || attributes.size() == 0) {
                log.error("Could not retrieve attributes from SNS Topic");
                throw new RuntimeException("Could not initialize SNS Client");
            }

            log.debug(String.format("Topic has %s subscribers", attributes.get("SubscriptionsConfirmed")));
        }
    }
}
