package com.lcb.admin.controller;

import com.lcb.admin.LcbApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
class UserControllerTest extends LcbApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void testUserPageUnauthenticated() throws Exception {
        mvc.perform(get("/api/system/user/page"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.msg").value("未登录，请先登录"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        String body = """
            {"username": "admin", "password": "admin123"}
            """;
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
