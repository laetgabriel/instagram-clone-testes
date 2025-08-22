package br.edu.ifpb.instagram.controller.integration;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.service.UserService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração com o banco de dados usando MockMvc
 */

// Configuração para usar uma instância da classe de teste por todos os métodos
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// Carrega o contexto completo do Spring para teste de integração
@SpringBootTest
// Configura MockMvc automaticamente e desabilita filtros de segurança
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes de Integração do UserController")
public class UserControllerTest {

    // MockMvc simula requisições HTTP sem subir servidor real
    // É a principal ferramenta para testar controllers no Spring
    @Autowired
    private MockMvc mockMvc;

    // Mock do service - substitui a implementação real durante os testes
    // Permite controlar exatamente o que o service retorna
    @MockitoBean
    private UserService userService;

    // Para converter objetos Java em JSON e vice-versa nas requisições
    @Autowired
    private ObjectMapper objectMapper;

    // Objetos de teste criados antes de cada método de teste
    private UserDto userDto1;
    private UserDto userDto2;
    private UserDetailsRequest userDetailsRequest;

    @BeforeEach
    public void setUp() {
        // Criação dos dados de teste que serão reutilizados
        userDto1 = new UserDto(1L, "João Silva", "joao123", "joao@email.com", "password123", null);
        userDto2 = new UserDto(2L, "Maria Santos", "maria456", "maria@email.com", "password456", null);
        
        userDetailsRequest = new UserDetailsRequest(1L, "João Silva Atualizado", "joao_updated", "joao.updated@email.com", "newPassword123");
    }

    @Test
    @DisplayName("Deve retornar lista de usuários quando existem usuários cadastrados")
    public void getUsers_ShouldReturnListOfUsers() throws Exception {
        // Given - Configuração do cenário de teste
        List<UserDto> userDtos = Arrays.asList(userDto1, userDto2);
        
        // WHEN: Configuração do mock - quando o método findAll() for chamado no userService
        // THEN: retorna nossa lista de teste predefinida
        // Isso substitui a implementação real do service
        when(userService.findAll()).thenReturn(userDtos);

        // When & Then - Execução da requisição e verificação dos resultados
        mockMvc.perform(get("/users") // MockMvc simula uma requisição GET para /users
                .contentType(MediaType.APPLICATION_JSON)) // Define o Content-Type
                .andDo(print()) // Imprime detalhes da requisição e resposta no console
                .andExpect(status().isOk()) // Verifica se o status HTTP é 200
                .andExpect(jsonPath("$", hasSize(2))) // JsonPath: verifica se o array JSON tem 2 elementos
                // JsonPath para verificar campos específicos do primeiro usuário (índice 0)
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fullName", is("João Silva")))
                .andExpect(jsonPath("$[0].username", is("joao123")))
                .andExpect(jsonPath("$[0].email", is("joao@email.com")))
                // JsonPath para verificar campos do segundo usuário (índice 1)
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fullName", is("Maria Santos")))
                .andExpect(jsonPath("$[1].username", is("maria456")))
                .andExpect(jsonPath("$[1].email", is("maria@email.com")));

        // VERIFY: Confirma que o método findAll() foi chamado exatamente uma vez
        // Isso garante que o controller realmente chamou o service
        verify(userService).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem usuários cadastrados")
    void getUsers_WhenNoUsers_ShouldReturnEmptyList() throws Exception {
        // Given - Cenário: banco de dados vazio (sem usuários)
        // WHEN: configura o mock para retornar lista vazia
        when(userService.findAll()).thenReturn(Collections.emptyList());

        // When & Then - Testa o comportamento com lista vazia
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Verifica que o array JSON está vazio

        // VERIFY: Confirma que o service foi chamado mesmo com resultado vazio
        verify(userService).findAll();
    }

    @Test
    @DisplayName("Deve retornar usuário específico quando ID é válido")
    void getUser_WithValidId_ShouldReturnUser() throws Exception {
        // Given - Configura o mock para retornar usuário específico
        // WHEN: findById(1L) é chamado, THEN: retorna userDto1
        when(userService.findById(1L)).thenReturn(userDto1);

        // When & Then - Simula GET /users/1
        mockMvc.perform(get("/users/1") // Path variable {id} = 1
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // JsonPath com $ refere-se ao objeto raiz (não é um array neste caso)
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("João Silva")))
                .andExpect(jsonPath("$.username", is("joao123")))
                .andExpect(jsonPath("$.email", is("joao@email.com")));

        // VERIFY: Confirma que findById foi chamado com o parâmetro correto (1L)
        verify(userService).findById(1L);
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso quando dados são válidos")
    void updateUser_WithValidRequest_ShouldReturnUpdatedUser() throws Exception {
        // Given - Preparação do objeto que simula o resultado da atualização
        UserDto updatedUserDto = new UserDto(1L, "João Silva Atualizado", "joao_updated", 
                                           "joao.updated@email.com", "newPassword123", null);
        
        // WHEN: updateUser é chamado com qualquer UserDto, THEN: retorna updatedUserDto
        // any(UserDto.class) significa "qualquer objeto do tipo UserDto"
        when(userService.updateUser(any(UserDto.class))).thenReturn(updatedUserDto);

        // When & Then - Simula requisição PUT com JSON no corpo
        mockMvc.perform(put("/users") // Requisição PUT
                .contentType(MediaType.APPLICATION_JSON) // Importante: define que enviamos JSON
                // Converte o objeto Java para JSON string usando ObjectMapper
                .content(objectMapper.writeValueAsString(userDetailsRequest)))
                .andExpect(status().isOk())
                // Verifica se os dados retornados correspondem ao objeto atualizado
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("João Silva Atualizado")))
                .andExpect(jsonPath("$.username", is("joao_updated")))
                .andExpect(jsonPath("$.email", is("joao.updated@email.com")));

        // VERIFY: Confirma que updateUser foi chamado com algum UserDto
        // Não verifica o conteúdo exato, apenas que foi chamado
        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso quando ID é válido")
    void deleteUser_WithValidId_ShouldReturnSuccessMessage() throws Exception {
        // Given
        Long userId = 1L;
        // doNothing(): configura mock para método void - não retorna nada, apenas executa
        doNothing().when(userService).deleteUser(userId);

        // When & Then - Simula DELETE /users/1
        mockMvc.perform(delete("/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // content().string(): verifica o texto retornado (não JSON, apenas string)
                .andExpect(content().string("user was deleted!"));

        // VERIFY: Confirma que deleteUser foi chamado com o ID correto
        verify(userService).deleteUser(userId);
    }

    @Test
    @DisplayName("Deve retornar Bad Request quando JSON está malformado")
    void updateUser_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // When & Then - Teste de cenário de erro
        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}")) // JSON propositalmente inválido
                .andExpect(status().isBadRequest()); // Espera HTTP 400

        // verifyNoInteractions(): Garante que NENHUM método do service foi chamado
        // Isso acontece porque o erro ocorre antes de chegar no controller
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Deve retornar Unsupported Media Type quando Content-Type está ausente")
    void updateUser_WithMissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then - Testa requisição sem Content-Type
        mockMvc.perform(put("/users")
                // Note que não há .contentType() aqui
                .content(objectMapper.writeValueAsString(userDetailsRequest)))
                .andExpect(status().isUnsupportedMediaType()); // Espera HTTP 415

        // Service não é chamado porque a requisição é rejeitada antes
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Deve retornar todos os usuários quando dataset é grande")
    void getUsers_WithLargeDataset_ShouldReturnAllUsers() throws Exception {
        // Given - Simula um cenário com mais dados
        List<UserDto> largeUserList = Arrays.asList(
            new UserDto(1L, "User 1", "user1", "user1@email.com", "pass1", null),
            new UserDto(2L, "User 2", "user2", "user2@email.com", "pass2", null),
            new UserDto(3L, "User 3", "user3", "user3@email.com", "pass3", null),
            new UserDto(4L, "User 4", "user4", "user4@email.com", "pass4", null),
            new UserDto(5L, "User 5", "user5", "user5@email.com", "pass5", null)
        );
        
        // WHEN: findAll() retorna lista com 5 usuários
        when(userService.findAll()).thenReturn(largeUserList);

        // When & Then - Verifica que todos os dados são retornados
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5))) // Verifica tamanho da lista
                // Testa apenas alguns elementos específicos (primeiro e último)
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[4].username", is("user5")));

        // VERIFY: Confirma a chamada do service
        verify(userService).findAll();
    }
}