package it.maggioli.academy.classe.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.maggioli.academy.classe.controllers.AuthController;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(SecurityConfigTests.TestConfiguration.class)
class SecurityConfigTests {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymousRequestsRequireAuthentication() throws Exception {
        mvc.perform(get("/test").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedRequestsAreAllowed() throws Exception {
        mvc.perform(get("/test").with(user("student")))
                .andExpect(status().isOk());
    }

    @Test
    void writesRequireCsrfToken() throws Exception {
        mvc.perform(post("/test").with(user("student")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/test").with(user("student")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void allowedPreflightDoesNotRequireAuthentication() throws Exception {
        mvc.perform(options("/test")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "X-CSRF-TOKEN"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void untrustedOriginsAreRejected() throws Exception {
        mvc.perform(options("/test")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void adminLoginCreatesSessionAndLogoutRevokesAccess() throws Exception {
        var result = mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("admin"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("admin"));
        mvc.perform(get("/test").session(session)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongCredentialsAreRejected() throws Exception {
        for (String credentials : new String[] {
                "{\"user\":\"admin\",\"password\":\"wrong\"}",
                "{\"user\":\"wrong\",\"password\":\"admin\"}"}) {
            mvc.perform(post("/api/auth/login").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(credentials))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void csrfTokenIsPublicAndLoginRequiresIt() throws Exception {
        var result = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty()).andReturn();
        var token = (org.springframework.security.web.csrf.CsrfToken)
                result.getRequest().getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        mvc.perform(post("/api/auth/login")
                        .session((MockHttpSession) result.getRequest().getSession(false))
                        .header(token.getHeaderName(), token.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isForbidden());
    }

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, TestController.class, AuthController.class})
    static class TestConfiguration {
    }

    @RestController
    static class TestController {
        @GetMapping("/test")
        String read() {
            return "ok";
        }

        @PostMapping("/test")
        String write() {
            return "ok";
        }
    }
}
