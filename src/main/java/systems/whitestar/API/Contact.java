package systems.whitestar.API;

import com.google.common.hash.Hashing;
import lombok.extern.log4j.Log4j;
import systems.whitestar.API.Models.SimpleMessage;
import systems.whitestar.SNS;
import systems.whitestar.Secret;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

/**
 * @author Tom Paulus
 * Created on 11/21/17.
 */
@Log4j
@Path("contact")
public class Contact {
    private static final String CONACT_MESSAGE_TEMPLATE = "From: %s <%s>\n Subject: %s\n\n %s";
    private static final String TOPIC_SUBJECT = "Contact Form Response from whitestar.systems";

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response contactForm(@FormParam("check-nonce") final String checkNonce,
                                @FormParam("name") final String name,
                                @FormParam("email") final String email,
                                @FormParam("message") final String message,
                                @FormParam("check") final String check) {
        if (check == null || check.isEmpty() ||
                checkNonce == null || checkNonce.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_ACCEPTABLE)
                    .entity(new SimpleMessage("Error", "Human Verification Missing")
                            .asJson())
                    .build();
        }

        if (!Hashing.sha256()
                .hashString(check, StandardCharsets.UTF_8)
                .toString()
                .equals(checkNonce)) {
            return Response
                    .status(Response.Status.NOT_ACCEPTABLE)
                    .entity(new SimpleMessage("Error", "Human Verification Failed")
                            .asJson())
                    .build();
        }

        if (name == null || name.isEmpty() ||
                email == null || email.isEmpty() ||
                message == null || message.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new SimpleMessage("Error", "Not all form fields were completed")
                            .asJson())
                    .build();
        }

        Runnable publishMessage = () -> {
            String publishMsg = String.format(CONACT_MESSAGE_TEMPLATE,
                    name,
                    email,
                    TOPIC_SUBJECT,
                    message);

            log.info(String.format("Posting message from %s <%s> to Contact Form Topic", name, email));
            final String messageId = SNS.getInstance().publishToTopic(
                    Secret.getInstance().getSecret("sns.contact_topic.arn"),
                    publishMsg);

            log.debug("Message ID - " + messageId);
        };

        return Response.accepted().build();
    }
}
