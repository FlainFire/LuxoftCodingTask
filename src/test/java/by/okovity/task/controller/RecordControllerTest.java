package by.okovity.task.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RecordControllerTest {

    private static final String RECORD_JSON_RESPONSE = "{\"id\":1,\"name\":\"First\",\"description\":\"Lorem ipsum dolor sit amet.\",\"timestamp\":\"2020-08-19T09:50:00.000+00:00\"}";
    private static final String CORRUPTED_RECORD_JSON_RESPONSE = "{\"id\":7,\"name\":\"\",\"description\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent.\",\"timestamp\":\"2020-08-19T12:00:00.000+00:00\"}";

    @Value("${project.accept.corrupted.records}")
    private boolean acceptCorrupted;

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
    public void successTest() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("CodingTaskTest.csv");
        final MockMultipartFile testFile = new MockMultipartFile("file", "test.csv", "text/csv", is);

        mvc.perform(MockMvcRequestBuilders.post("/records"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File does not exist or empty"));

        mvc.perform(MockMvcRequestBuilders.multipart("/records").file(testFile))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get("/records/101"))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders.get("/records/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(RECORD_JSON_RESPONSE));

        ResultActions getRecord = mvc.perform(MockMvcRequestBuilders.get("/records/7"));
        if (acceptCorrupted) {
            getRecord.andExpect(status().isOk()).andExpect(content().json(CORRUPTED_RECORD_JSON_RESPONSE));
        } else {
            getRecord.andExpect(status().isNotFound());
        }

        mvc.perform(MockMvcRequestBuilders.delete("/records/test"))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders.delete("/records/1"))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.delete("/records/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSecurity() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/records"))
                .andExpect(status().isUnauthorized());

        mvc.perform(MockMvcRequestBuilders.get("/records/2"))
                .andExpect(status().isUnauthorized());

        mvc.perform(MockMvcRequestBuilders.delete("/records/2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void testSecurityForAdmin() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/records/3"))
                .andExpect(status().isForbidden());
    }
}
