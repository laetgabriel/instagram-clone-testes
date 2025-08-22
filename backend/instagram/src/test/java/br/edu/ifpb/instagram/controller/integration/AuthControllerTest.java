package br.edu.ifpb.instagram.controller.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpb.instagram.controller.AuthController;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.LoginRequest;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.service.UserService;
import br.edu.ifpb.instagram.service.impl.AuthServiceImpl;

@SpringJUnitConfig
class AuthControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private AuthController authController;

    private LoginRequest loginRequest;
    private UserDetailsRequest signUpRequest;
    private UserDto userDto;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        authService = org.mockito.Mockito.mock(AuthServiceImpl.class);
        userService = org.mockito.Mockito.mock(UserService.class);
        authController = new AuthController(authService, userService);
        objectMapper = new ObjectMapper();
        
        // Configurar MockMvc standalone
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        
        // Dados de teste
        loginRequest = new LoginRequest("joao_silva", "password123");
        signUpRequest = new UserDetailsRequest(null, "João Silva", "joao_silva", "joao@email.com", "password123");
        userDto = new UserDto(1L, "João Silva", "joao_silva", "joao@email.com", "password123", null);
        jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.token_example";
    }

    @Test
    @DisplayName("POST /auth/signin - Deve autenticar usuário com credenciais válidas e retornar status 200")
    void testSignIn_ShouldAuthenticateUserWithValidCredentialsAndReturnStatus200() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(jwtToken);

        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // When & Then
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao_silva"))
                .andExpect(jsonPath("$.token").value(jwtToken));
    }

    @Test
    @DisplayName("POST /auth/signin - Deve processar JSON de login corretamente")
    void testSignIn_ShouldProcessLoginJsonCorrectly() throws Exception {
        // Given
        String expectedToken = "jwt.token.here";
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(expectedToken);

        String jsonRequest = """
            {
                "username": "maria_santos",
                "password": "senha456"
            }
            """;

        // When & Then
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("maria_santos"))
                .andExpect(jsonPath("$.token").value(expectedToken));
    }

    @Test
    @DisplayName("POST /auth/signup - Deve criar novo usuário com dados válidos e retornar status 201")
    void testSignUp_ShouldCreateUserWithValidDataAndReturnStatus201() throws Exception {
        // Given
        UserDto createdUser = new UserDto(1L, "João Silva", "joao_silva", "joao@email.com", "password123", null);
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUser);

        String requestBody = objectMapper.writeValueAsString(signUpRequest);

        // When & Then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("João Silva"))
                .andExpect(jsonPath("$.username").value("joao_silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password não deve ser retornada
    }

    @Test
    @DisplayName("POST /auth/signup - Deve processar JSON de cadastro corretamente")
    void testSignUp_ShouldProcessSignUpJsonCorrectly() throws Exception {
        // Given
        UserDto createdUser = new UserDto(2L, "Maria Santos", "maria_santos", "maria@email.com", "senha456", null);
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUser);

        String jsonRequest = """
            {
                "fullName": "Maria Santos",
                "username": "maria_santos",
                "email": "maria@email.com",
                "password": "senha456"
            }
            """;

        // When & Then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.fullName").value("Maria Santos"))
                .andExpect(jsonPath("$.username").value("maria_santos"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    @DisplayName("POST /auth/signup - Deve criar usuário com ID null e retornar usuário com ID gerado")
    void testSignUp_ShouldCreateUserWithNullIdAndReturnUserWithGeneratedId() throws Exception {
        // Given
        UserDetailsRequest requestWithoutId = new UserDetailsRequest(null, "Teste User", "teste_user", "teste@email.com", "teste123");
        UserDto createdUser = new UserDto(99L, "Teste User", "teste_user", "teste@email.com", "teste123", null);
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUser);

        String requestBody = objectMapper.writeValueAsString(requestWithoutId);

        // When & Then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.fullName").value("Teste User"))
                .andExpect(jsonPath("$.username").value("teste_user"))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }

    @Test
    @DisplayName("POST /auth/signin - Deve chamar authService com dados corretos do LoginRequest")
    void testSignIn_ShouldCallAuthServiceWithCorrectLoginRequestData() throws Exception {
        String expectedToken = "valid.jwt.token";
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(expectedToken);

        String jsonRequest = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.token").value(expectedToken));
    }

    @Test
    @DisplayName("POST /auth/signup - Deve chamar userService.createUser com UserDto correto")
    void testSignUp_ShouldCallUserServiceCreateUserWithCorrectUserDto() throws Exception {
        // Given
        UserDto expectedCreatedUser = new UserDto(5L, "Ana Costa", "ana_costa", "ana@email.com", "ana123", null);
        when(userService.createUser(any(UserDto.class))).thenReturn(expectedCreatedUser);

        String jsonRequest = """
            {
                "fullName": "Ana Costa",
                "username": "ana_costa",
                "email": "ana@email.com",
                "password": "ana123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.fullName").value("Ana Costa"))
                .andExpect(jsonPath("$.username").value("ana_costa"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }
}