package systems.whitestar;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.response.LogicalResponse;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j;

import javax.inject.Singleton;
import java.util.Map;

/**
 * @author Tom Paulus
 * Created on 9/30/17.
 */
@Singleton
@Log4j
public class Secret {
    private static Secret secret = null;
    private static Vault vault = null;
    private String mVaultAddr;
    private String mRoleId;
    private String mSecretId;
    private String mAppName;

    private Secret() throws VaultException {
        mVaultAddr = System.getenv("VAULT_ADDR");
        if (mVaultAddr.endsWith("/")) mVaultAddr = mVaultAddr.substring(0, mVaultAddr.length() - 2);
        mRoleId = System.getenv("VAULT_ROLE");
        mSecretId = System.getenv("VAULT_SECRET");
        mAppName = System.getenv("WWW_APP");

        initializeVaultConnector();
    }

    public static Secret getInstance() {
        try {
            if (secret == null) secret = new Secret();
            return secret;
        } catch (VaultException e) {
            log.fatal("Could not connect to Vault", e);
            throw new RuntimeException(e);
        }
    }

    public String getSecret(String secretName) {
        return getSecret(mAppName, secretName, String.class);
    }

    public <T> T getSecret(String secretName, Class<T> type) {
        return getSecret(mAppName, secretName, type);
    }

    public String getSecret(String appName, String secretName) {
        return getSecret(appName, secretName, String.class);
    }

    public <T> T getSecret(String appName, String secretName, Class<T> type) {
        try {
            String response = vault.logical()
                    .read("secret/" + appName)
                    .getData().get(secretName);

            return new Gson().fromJson(response, type);

        } catch (VaultException e) {
            log.error(String.format("Secret with name \"%s\" in app \"%s\" is not defined", secretName, appName));
            throw new RuntimeException(e);
        }

    }

    public LogicalResponse setSecret(String appName, Map<String, Object> secret) {
        final Gson gson = new Gson();

        log.debug("App Name: " + appName);
        log.debug("Secret Payload: " + gson.toJson(secret));

        for (final Map.Entry<String, Object> pair : secret.entrySet()) {
            final Object value = pair.getValue();
            if (value != null && !(value instanceof String)) {
                pair.setValue(gson.toJson(value));
            }
        }

        try {
            return vault.logical()
                    .write("secret/" + appName, secret);
        } catch (VaultException e) {
            log.error(String.format("Problem writing secret %s", appName));
            throw new RuntimeException(e);
        }
    }

    public LogicalResponse deleteSecret(String appName) {
        log.warn(String.format("Deleting Secret %s", appName));
        try {
            return vault.logical().delete("secret/" + appName);
        } catch (VaultException e) {
            log.error(String.format("Problem deleting secret %s", appName));
            throw new RuntimeException(e);
        }
    }

    private void initializeVaultConnector() throws VaultException {
        final VaultConfig vaultConfig = new VaultConfig()
                .address(mVaultAddr)
                .build();

        vault = new Vault(vaultConfig);

        AuthResponse response = vault.auth().loginByAppRole("approle", mRoleId, mSecretId);
        vaultConfig.token(response.getAuthClientToken());

        vault = new Vault(vaultConfig);
    }
}
