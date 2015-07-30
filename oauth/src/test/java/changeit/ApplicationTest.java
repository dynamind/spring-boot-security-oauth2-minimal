package changeit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public class ApplicationTest {

    @Value("${local.server.port}")
    private int port;

    @Test
    public void tokenKeyEndpoint() {

        String tokenKeyUri = "http://localhost:" + port + "/oauth/token_key";

        // Public client is rejected

        RestTemplate template = new TestRestTemplate("public", "");
        ResponseEntity<String> response = template.getForEntity(tokenKeyUri, null, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Trusted client is allowed

        template = new TestRestTemplate("trusted", "secret");
        response = template.getForEntity(tokenKeyUri, null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Anonymous is allowed

        template = new TestRestTemplate();
        response = template.getForEntity(tokenKeyUri, null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void checkTokenEndpoint() {

        String tokenKeyUri = "http://localhost:" + port + "/oauth/check_token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set("token", "invalid-token");

        // Trusted client invalid token

        RestTemplate template = new TestRestTemplate("trusted", "secret");
        ResponseEntity<String> response = template.postForEntity(tokenKeyUri, body, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(response.getBody(), "{\"error\":\"invalid_token\",\"error_description\":\"Cannot convert access token to JSON\"}");

        // Now fix the key in the body

        body.set("token", "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0Mzc4MTYyMjcsInVzZXJfbmFtZSI6InJveSIsInNjb3BlIjpbInJlYWQiXSwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjE2N2Y3NTg1LWI1MmYtNDIyMy04NmJjLWJhZWJlMzYwYThkMSIsImNsaWVudF9pZCI6InRydXN0ZWQifQ.ZX66_W2wueVaGzhqp3VMkKARp58HbVi6gIEryKaIUZA8UmD5pjaG4fQJW63liABi5k8g6F2vuE364P1znaysg02EY0uzDEEaKgaMbT5sB-qpwzTnCUjT7JaGFkO1_nXegWsWxefukv-kHxOIXTFvx5RlwJE2o8wQmSSLSVDW-havvBEDD0CKteXH1DgVdOGIgo18n0JPpf89O5_Ukhwkac-OEKYf_3NroXU2pG5fAKwIquBBWa04C2-FvPnPAhWoB9dQfaR2ea0fxRblYvjZahFqH7Qv3O4KhssMqIIw_qJ1vRpGbKdaUMgFBaszISC7mVAIYJXEiEZ4Dp2koGmcaA");

        // Public client is rejected

        template = new TestRestTemplate("public", "");
        response = template.postForEntity(tokenKeyUri, body, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Anonymous is rejected

        template = new TestRestTemplate();
        response = template.postForEntity(tokenKeyUri, body, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(response.getBody(), "{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}");

        // Trusted client is allowed

        template = new TestRestTemplate("trusted", "secret");
        response = template.postForEntity(tokenKeyUri, body, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void protectedEndpoints() {

        String tokenUri = "http://localhost:" + port + "/oauth/token";

        RestTemplate template = new TestRestTemplate();
        ResponseEntity<String> response = template.postForEntity(tokenUri, null, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(response.getBody(), "{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}");
    }

    @Test
    public void passwordGrants() {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", "roy");
        body.add("password", "42");
        body.add("scope", "read");

        String tokenUri = "http://localhost:" + port + "/oauth/token";

        // Trusted client is allowed

        RestTemplate template = new TestRestTemplate("trusted", "secret");
        ResponseEntity<String> response = template.postForEntity(tokenUri, body, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"access_token\""));

        // Confidential client is not allowed

        template = new TestRestTemplate("confidential", "secret");
        response = template.postForEntity(tokenUri, body, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(response.getBody(), "{\"error\":\"invalid_client\",\"error_description\":\"Unauthorized grant type: password\"}");
    }

    public void implicitGrants() {
        // Implicit grants are more difficult to test, because the 'Unauthorized grant type' error is given
        // after successful authentication at the login screen, which requires cookie handling. Geb is much better suited for that
    }

    public void authorizationCodeGrants() {
        // Authorization code grants are more difficult to test, because the 'Unauthorized grant type' error is given
        // after successful authentication at the login screen, which requires cookie handling. Geb is much better suited for that
    }

    private void dumpHttpHeaders(HttpHeaders headers) {
        for (String key : headers.keySet()) {
            List<String> values = headers.get(key);
            for (String value : values) {
                System.out.println(key + ": " + value);
            }
        }
    }


}