package pw.react.backend.reactbackend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pw.react.backend.reactbackend.models.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("it")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITReactBackendApplicationTests {
    @Autowired
    private WebApplicationContext context;

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

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }
    
    @After
    public void clean() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/users").with(httpBasic("john123", "pass"))).andReturn();
        String content = result.getResponse().getContentAsString();
        List<User> users = objectMapper.readValue(content, new TypeReference<List<User>>() {
        });

        for (User user : users) {
            this.mockMvc.perform(delete("/users/" + user.getId()).with(httpBasic("john123", "pass")));
        }
    }

    @Test
    public void givenNoCredentials_whenRequestIsIssued_then_ReturnErrorResponse() throws Exception {
        this.mockMvc.perform(get("/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenValidCredentials_whenRequestIsIssued_then_ReturnErrorResponse() throws Exception {
        this.mockMvc.perform(get("/users")
                .with(httpBasic("john123", "pass")))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    public void givenUser_whenPostIsRequested_thenReturnCorrectResponse() throws Exception {
        this.mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0))))
                .andExpect(status().is2xxSuccessful());
    }

    @WithMockUser
    @Test
    public void givenUser_whenGetIsRequested_thenReturnUserAndCorrectStatus() throws Exception {
        // given
        MvcResult result = this.mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(1))))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);


        // when ... then
        this.mockMvc.perform(get("/users/"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString(users.get(1).getLastName())));

        this.mockMvc.perform(get("/users/" + user.getId()))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString(users.get(1).getLastName())));

        this.mockMvc.perform(get("/users?login=" + users.get(1).getLogin()))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString(users.get(1).getLastName())));
    }

    @WithMockUser
    @Test
    public void givenUser_whenPutIsRequested_thenReturnCorrectStatus() throws Exception {
        // given
        MvcResult result = this.mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String content = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);
        user.setLogin("stefan123");
        user.setFirstName("abracadabra");
        this.mockMvc.perform(put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))).andExpect(status().is2xxSuccessful());
    }

    @WithMockUser
    @Test
    public void givenUser_whenDeleteIsRequested_thenReturnCorrectStatus() throws Exception {
        // given
        MvcResult result = this.mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String content = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);
        // when then
        this.mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string((containsString("deleted"))));
    }

    @WithMockUser
    @Test
    public void givenNothing_whenGetForUserIsRequested_thenReturnErrorStatus() throws Exception {
        this.mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    public void givenUser_whenPutForExistentUserIsRequested_thenReturnErrorStatus() throws Exception {
        this.mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0))))
                .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(0))))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    public void givenNothing_whenUpdateIsRequested_thenReturnErrorStatus() throws Exception {
        this.mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users.get(1))))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    public void givenNothing_whenDeleteIsRequested_thenReturnErrorStatus() throws Exception {
        // when then
        this.mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNotFound());
    }
}
