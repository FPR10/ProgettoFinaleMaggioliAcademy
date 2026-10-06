package it.maggioli.academy.classe;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTests {
    @Value("${local.server.port}") int port;
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();

    private HttpResponse<String> call(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (!method.equals("GET")) {
            JsonNode csrf = json.readTree(call("GET", "/api/auth/csrf", null).body());
            request.header(csrf.get("headerName").asText(), csrf.get("token").asText());
        }
        request.header("Content-Type", "application/json");
        return client.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void sessionCsrfAndCrudWorkTogether() throws Exception {
        assertEquals(401, call("GET", "/api/anagrafiche", null).statusCode());
        assertEquals(200, call("POST", "/api/auth/login", "{\"user\":\"admin\",\"password\":\"admin\"}").statusCode());
        assertEquals(200, call("GET", "/api/auth/me", null).statusCode());
        String body = "{\"nome\":\"Mario\",\"cognome\":\"Rossi\",\"codiceFiscale\":\"RSSMRA80A01H501U\","
                + "\"email\":\"mario.rossi@example.com\",\"telefono\":null,\"dataNascita\":\"1980-01-01\"}";
        var created = call("POST", "/api/anagrafiche", body);
        assertEquals(201, created.statusCode(), created.body());
        String path = "/api/anagrafiche/" + json.readTree(created.body()).get("id").asLong();
        assertTrue(json.readTree(created.body()).get("telefono").isNull());
        assertEquals(409, call("POST", "/api/anagrafiche", body).statusCode());
        assertEquals(200, call("GET", "/api/anagrafiche", null).statusCode());
        assertEquals(200, call("GET", path, null).statusCode());
        var invalid = call("PUT", path, body.replace("Mario", ""));
        assertEquals(400, invalid.statusCode());
        assertTrue(json.readTree(invalid.body()).get("errors").has("nome"));
        var updated = call("PUT", path, body.replace("null", "\"0612345678\""));
        assertEquals(200, updated.statusCode(), updated.body());
        assertEquals("0612345678", json.readTree(updated.body()).get("telefono").asText());
        assertEquals(204, call("DELETE", path, null).statusCode());
        assertEquals(404, call("GET", path, null).statusCode());
        assertEquals(204, call("POST", "/api/auth/logout", "{}").statusCode());
        assertEquals(401, call("GET", "/api/auth/me", null).statusCode());
    }
}
