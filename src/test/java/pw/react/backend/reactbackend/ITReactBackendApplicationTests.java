package pw.react.backend.reactbackend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pw.react.backend.reactbackend.models.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("it")
@RunWith(SpringRunner.class)
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

    @After
    public void clean() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/users")).andReturn();
        String content = result.getResponse().getContentAsString();
        List<User> users = objectMapper.readValue(content, new TypeReference<List<User>>() {
        });

        for (int i = 0; i < users.size(); ++i) {
            this.mockMvc.perform(delete("/users/" + users.get(i).getId()));
        }
    }

    @Test
    public void givenUser_whenPostIsRequested_thenReturnCorrectResponse() throws Exception {
        this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0)))).andExpect(status().is2xxSuccessful());
    }

    @Test
    public void givenUser_whenGetIsRequested_thenReturnUserAndCorrectStatus() throws Exception {
        // given
        MvcResult result = this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(1)))).andExpect(status().is2xxSuccessful()).andReturn();
        String content = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);


        // when ... then
        this.mockMvc.perform(get("/users/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString(users.get(1).getLastName())));

        this.mockMvc.perform(get("/users/" + user.getId())).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString(users.get(1).getLastName())));

        this.mockMvc.perform(get("/users?login=" + users.get(1).getLogin())).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString(users.get(1).getLastName())));
    }

    @Test
    public void givenUser_whenPutIsRequested_thenReturnCorrectStatus() throws Exception {
        // given
        MvcResult result = this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0)))).andExpect(status().is2xxSuccessful()).andReturn();

        String content = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);
        user.setLogin("stefan123");
        user.setFirstName("abracadabra");
        this.mockMvc.perform(put("/users/" + user.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))).andExpect(status().is2xxSuccessful());
    }

    @Test
    public void givenUser_whenDeleteIsRequested_thenReturnCorrectStatus() throws Exception {
        // given
        MvcResult result = this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0)))).andExpect(status().is2xxSuccessful()).andReturn();

        String content = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);
        // when then
        this.mockMvc.perform(delete("/users/" + user.getId())).andExpect(status().is2xxSuccessful())
                .andExpect(content().string((containsString("deleted"))));
    }

    @Test
    public void givenNothing_whenGetForUserIsRequested_thenReturnErrorStatus() throws Exception {
        this.mockMvc.perform(get("/users/1")).andExpect(status().isNotFound());
    }

    @Test
    public void givenUser_whenPutForExistentUserIsRequested_thenReturnErrorStatus() throws Exception {
        this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0)))).andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0)))).andExpect(status().isBadRequest());
    }

    @Test
    public void givenNothing_whenUpdateIsRequested_thenReturnErrorStatus() throws Exception {
        this.mockMvc.perform(put("/users/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(1)))).andExpect(status().isNotFound());
    }

    @Test
    public void givenNothing_whenDeleteIsRequested_thenReturnErrorStatus() throws Exception {
        // when then
        this.mockMvc.perform(delete("/users/1")).andExpect(status().isNotFound());
    }
}
