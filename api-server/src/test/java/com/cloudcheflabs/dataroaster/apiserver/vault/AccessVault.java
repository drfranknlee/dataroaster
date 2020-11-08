package com.cloudcheflabs.dataroaster.apiserver.vault;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.util.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

public class AccessVault {

    private static Logger LOG = LoggerFactory.getLogger(AccessVault.class);

    private VaultTemplate vaultTemplate;

    @Before
    public void init() throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "8200");
        String trustStore = System.getProperty("trustStore");

        System.setProperty("javax.net.ssl.trustStore", trustStore);

        vaultTemplate = new VaultTemplate(VaultEndpoint.create(host, Integer.parseInt(port)),
                new TokenAuthentication("s.652G9bFIwOC2XQczhZ633UW0"));
    }

    @Test
    public void accessVault() throws Exception {
        vaultTemplate.write("secret/myapp-1", new Secrets("mykidong", "some-password"));
        vaultTemplate.write("secret/myapp-2", new Secrets("mykidong2", "some-password2"));

        VaultResponseSupport<Secrets> response = vaultTemplate.read("secret/myapp-1", Secrets.class);
        LOG.info("secret: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), response.getData())));

        VaultResponseSupport<Secrets> response2 = vaultTemplate.read("secret/myapp-2", Secrets.class);
        LOG.info("secret2: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), response2.getData())));
    }

    @Test
    public void access() throws Exception {
        Secrets secrets = new Secrets("mykidong", "some-password");

        vaultTemplate.write("secret/myapp", secrets);

        VaultResponseSupport<Secrets> response = vaultTemplate.read("secret/myapp", Secrets.class);
        Secrets retValue = response.getData();

        LOG.info("user: {}, password: {}", retValue.getUsername(), retValue.getPassword());

        vaultTemplate.delete("secret/myapp");
    }

    private static class Secrets {

        private String username;
        private String password;

        public Secrets() {}

        public Secrets(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }
    }
}
