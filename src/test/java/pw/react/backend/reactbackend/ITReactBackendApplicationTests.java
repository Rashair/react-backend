package pw.react.backend.reactbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pw.react.backend.reactbackend.models.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("it")
@SpringBootTest
@AutoConfigureMockMvc
public class ITReactBackendApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static List<User> users = Arrays.asList(
            new User().setAllDetails("wiczolekp", "przemek", "wiczolek", true),
            new User().setAllDetails("kaladin", "kaladin", "",
                    LocalDate.of(100, 10, 1), true),
            new User().setAllDetails("davars", "shallan", "davar", true),
            new User().setAllDetails("eodin", "eodin", "", false),
            new User().setAllDetails("kholind", "dalinar", "kholin",
                    LocalDate.of(80, 3, 5), true)
    );

    @Test
    void givenUsers_whenPostIsRequested_thenReturnCorrectResponse() throws Exception {
        this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0)))).andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenUser_whenRequestForUserIsIssued_thenReturnUser() throws Exception {
        this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(1)))).andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/users?login=" + users.get(1).getLogin())).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString(users.get(1).getLastName())));
    }

}
