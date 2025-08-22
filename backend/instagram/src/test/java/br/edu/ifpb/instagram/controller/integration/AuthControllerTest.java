package br.edu.ifpb.instagram.controller.integration;

import br.edu.ifpb.instagram.controller.AuthController;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.LoginRequest;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.model.response.LoginResponse;
import br.edu.ifpb.instagram.model.response.UserDetailsResponse;
import br.edu.ifpb.instagram.service.UserService;
import br.edu.ifpb.instagram.service.impl.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Configuração para usar uma instância da classe de teste por todos os métodos
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// Carrega o contexto completo do Spring para teste de integração
@SpringBootTest
// Configura MockMvc automaticamente e desabilita filtros de segurança
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes de Integração do AuthController")
public class AuthControllerTest {

    // MockMvc simula requisições HTTP sem subir servidor real
    // Essencial para testar endpoints de autenticação
    @Autowired
    private MockMvc mockMvc;

    // Mock do AuthService - controla o comportamento da autenticação
    @MockitoBean
    private AuthServiceImpl authService;

    // Mock do UserService - controla o comportamento do cadastro
    @MockitoBean
    private UserService userService;

    // Para converter objetos Java em JSON nas requisições POST
    @Autowired
    private ObjectMapper objectMapper;

    // Objetos de teste criados antes de cada método
    private LoginRequest loginRequest;
    private UserDetailsRequest userDetailsRequest;
    private UserDto createdUserDto;

    @BeforeEach
    void setUp() {
        // Dados para teste de login
        loginRequest = new LoginRequest("joao123", "password123");
        
        // Dados para teste de cadastro
        userDetailsRequest = new UserDetailsRequest(
            null, 
            "João Silva", 
            "joao123", 
            "joao@email.com", 
            "password123"
        );
        
        // Simula o usuário criado que seria retornado pelo service
        createdUserDto = new UserDto(
            1L, 
            "João Silva", 
            "joao123", 
            "joao@email.com", 
            "password123", 
            null
        );
    }

    @Test
    @DisplayName("Deve fazer login com sucesso quando credenciais são válidas")
    void signIn_WithValidCredentials_ShouldReturnTokenAndUser() throws Exception {
        // Given - Configuração do cenário de sucesso no login
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        
        // WHEN: authService.authenticate() é chamado com loginRequest
        // THEN: retorna um token JWT mockado
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(mockToken);

        // When & Then - Simula requisição POST para /auth/signin
        mockMvc.perform(post("/auth/signin") // Endpoint de login
                .contentType(MediaType.APPLICATION_JSON) // Importante: define Content-Type JSON
                // Converte loginRequest para JSON string
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // Mostra detalhes da requisição no console
                .andExpect(status().isOk()) // Verifica status HTTP 200
                // JsonPath: verifica se o username retornado está correto
                .andExpect(jsonPath("$.username", is("joao123")))
                // JsonPath: verifica se o token foi retornado
                .andExpect(jsonPath("$.token", is(mockToken)))
                // Verifica que a resposta tem exatamente 2 campos
                .andExpect(jsonPath("$.*", hasSize(2)));

        // VERIFY: Confirma que o método authenticate foi chamado uma vez
        // any(LoginRequest.class) = qualquer objeto do tipo LoginRequest
        verify(authService).authenticate(any(LoginRequest.class));
        
        // Verifica que o UserService NÃO foi chamado (só AuthService)
        verifyNoInteractions(userService);
    }



    @Test
    @DisplayName("Deve criar usuário com sucesso quando dados são válidos")
    void signUp_WithValidUserData_ShouldCreateUserAndReturnDetails() throws Exception {
        // Given - Configura o mock para simular criação bem-sucedida
        // WHEN: createUser() é chamado com qualquer UserDto
        // THEN: retorna o createdUserDto (simulando usuário salvo no banco)
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);

        // When & Then - Simula requisição POST para /auth/signup
        mockMvc.perform(post("/auth/signup") // Endpoint de cadastro
                .contentType(MediaType.APPLICATION_JSON)
                // Converte userDetailsRequest para JSON
                .content(objectMapper.writeValueAsString(userDetailsRequest)))
                .andDo(print())
                .andExpect(status().isCreated()) // Verifica status HTTP 201 (Created)
                // JsonPath: verifica dados do usuário criado na resposta
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("João Silva")))
                .andExpect(jsonPath("$.username", is("joao123")))
                .andExpect(jsonPath("$.email", is("joao@email.com")))
                // Verifica que password NÃO está na resposta (segurança)
                .andExpect(jsonPath("$.password").doesNotExist())
                // Confirma que resposta tem exatamente 4 campos
                .andExpect(jsonPath("$.*", hasSize(4)));

        // VERIFY: Confirma que createUser foi chamado com algum UserDto
        verify(userService).createUser(any(UserDto.class));
        
        // Verifica que AuthService NÃO foi chamado (só UserService)
        verifyNoInteractions(authService);
    }



    @Test
    @DisplayName("Deve retornar Bad Request quando JSON de login está malformado")
    void signIn_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // When & Then - Testa requisição com JSON inválido
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}")) // JSON propositalmente quebrado
                .andExpect(status().isBadRequest()); // Espera HTTP 400

        // VERIFY: Nenhum service é chamado porque erro acontece antes
        verifyNoInteractions(authService);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Deve retornar Unsupported Media Type quando Content-Type está ausente no login")
    void signIn_WithoutContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then - Testa requisição sem Content-Type
        mockMvc.perform(post("/auth/signin")
                // Note: sem .contentType()
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType()); // Espera HTTP 415

        // VERIFY: Services não são chamados
        verifyNoInteractions(authService);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Deve processar corretamente requisição de cadastro com todos os campos")
    void signUp_WithAllRequiredFields_ShouldProcessSuccessfully() throws Exception {
        // Given - Usuário com todos os campos preenchidos
        UserDetailsRequest completeRequest = new UserDetailsRequest(
            null,
            "Maria Santos Silva", 
            "maria_santos", 
            "maria.santos@email.com", 
            "strongPassword123"
        );
        
        UserDto completeUserDto = new UserDto(
            2L, 
            "Maria Santos Silva", 
            "maria_santos", 
            "maria.santos@email.com", 
            "strongPassword123", 
            null
        );
        
        // WHEN: createUser() retorna usuário completo
        when(userService.createUser(any(UserDto.class))).thenReturn(completeUserDto);

        // When & Then - Verifica processamento completo
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isCreated())
                // Verifica cada campo individualmente
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.fullName", is("Maria Santos Silva")))
                .andExpect(jsonPath("$.username", is("maria_santos")))
                .andExpect(jsonPath("$.email", is("maria.santos@email.com")));

        // VERIFY: Método foi chamado exatamente uma vez
        verify(userService, times(1)).createUser(any(UserDto.class));
    }
}