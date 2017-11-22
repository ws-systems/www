package systems.whitestar;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.bettercloud.vault.VaultException;
import lombok.extern.log4j.Log4j;

import java.util.Map;

/**
 * Extends Secret Class to deal with AWS Access Keys and Secrets.
 * Docs - https://www.vaultproject.io/docs/secrets/aws/index.html
 *
 * @author Tom Paulus
 * Created on 11/21/17.
 */
@Log4j
public class AWSSecrets extends Secret {
    private static AWSSecrets secret = null;


    private AWSSecrets() throws VaultException {
        super();
    }

    public static AWSSecrets getInstance() {
        try {
            if (secret == null) secret = new AWSSecrets();
            return secret;
        } catch (VaultException e) {
            log.fatal("Could not connect to Vault", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a set of Credentials for the given role in Vault
     *
     * @param role {@link String} Name of Role bound to AWS Policy in Vault
     * @return {@link AWSCredentialsProvider} Credentials
     * @throws VaultException if the AWS Secret Backend has not been configured correctly
     *                        or the specified role does not exist
     */
    public AWSCredentialsProvider getCredentials(final String role) throws VaultException {
        Map<String, String> response = vault.logical()
                .read("aws/creds/" + role)
                .getData();

        return new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials(response.get("access_key"), response.get("secret_key"));
            }

            @Override
            public void refresh() {
                throw new UnsupportedOperationException("Refresh is not supported by the Vault Credential Provider");
            }
        };
    }
}
