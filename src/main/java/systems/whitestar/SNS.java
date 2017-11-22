package systems.whitestar;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.*;
import com.bettercloud.vault.VaultException;
import lombok.extern.log4j.Log4j;

/**
 * Wrapper for AWS SNS Subscription Service
 *
 * @author Tom Paulus
 * Created on 11/21/17.
 */
@SuppressWarnings("WeakerAccess")
@Log4j
public class SNS {
    private static final String VAULT_ROLE_NAME = "sns";

    private static SNS instance = new SNS();
    private static AmazonSNS snsClient;

    private SNS() {
        try {
            AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard();
            builder.setCredentials(AWSSecrets.getInstance().getCredentials(VAULT_ROLE_NAME));
            builder.setRegion(Region.getRegion(Regions.US_WEST_2).toString());

            snsClient = builder.build();
            log.debug("SNS Client Ready!");
        } catch (VaultException e) {
            snsClient = null;
            log.error("Unable to obtain AWS Credentials for SNS", e);
        }
    }

    public static SNS getInstance() {
        return instance;
    }

    /**
     * Create a new AWS SNS Topic.
     * <p>
     * The name of the topic will be included in the ARN, so it should not contain spaces or any special characters.
     *
     * @param topicName {@link String} Topic Name
     * @return {@link String} Topic ARN
     */
    public String createTopic(final String topicName) {
        log.debug("Creating SNS Topic - " + topicName);
        CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
        CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);

        log.debug("Topic ARN:" + createTopicResult.getTopicArn());
        log.debug("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest));

        return createTopicResult.getTopicArn();
    }

    /**
     * Subscribe a user
     *
     * @param topicArn {@link String} The ARN of the Topic being subscribed to
     * @param method   {@link String} How the message will be delivered. {@code "email"} is recommended
     * @param endpoint {@link String} The endpoint that you want to receive notifications
     */
    public void subscribeToTopic(final String topicArn, final String method, final String endpoint) {
        log.debug(String.format("Subscribing \"%s\" to topic with arn \"%s\"", endpoint, topicArn));

        SubscribeRequest subRequest = new SubscribeRequest(topicArn, method, endpoint);
        snsClient.subscribe(subRequest);

        log.debug("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
    }

    /**
     * Publish a message to a topic
     *
     * @param topicArn {@link String} The ARN of the Topic being published to
     * @param message  {@link String} Message to publish
     * @return {@link String} Published Message ID
     */
    public String publishToTopic(final String topicArn, final String message) {
        log.debug(String.format("Publishing message to topic arn \"%s\" - \"%s\"", topicArn, message));

        PublishRequest publishRequest = new PublishRequest(topicArn, message);
        PublishResult publishResult = snsClient.publish(publishRequest);

        log.debug("MessageId - " + publishResult.getMessageId());
        return publishResult.getMessageId();
    }
}
