package com.lcb.admin.controller;

import com.lcb.admin.LcbApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
class UserControllerTest extends LcbApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void testUserPageUnauthenticated() throws Exception {
        mvc.perform(get("/api/system/user/page"))
            .andExpect(status().isOk());
    }
}
