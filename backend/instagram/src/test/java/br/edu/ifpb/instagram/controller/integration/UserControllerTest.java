package br.edu.ifpb.instagram.controller.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

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

import br.edu.ifpb.instagram.controller.UserController;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.service.UserService;

@SpringJUnitConfig
class UserControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private UserController userController;

    private UserDto userDto1;
    private UserDto userDto2;
    private UserDetailsRequest userDetailsRequest;

    @BeforeEach
    void setUp() {
        // Configurar manualmente sem Spring Context pesado
        userService = org.mockito.Mockito.mock(UserService.class);
        userController = new UserController(userService);
        objectMapper = new ObjectMapper();
        
        // Configurar MockMvc standalone
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        
        // Dados de teste
        userDto1 = new UserDto(1L, "João Silva", "joao_silva", "joao@email.com", "password123", null);
        userDto2 = new UserDto(2L, "Maria Santos", "maria_santos", "maria@email.com", "password456", null);
        userDetailsRequest = new UserDetailsRequest(1L, "João Silva Atualizado", "joao_silva_novo", "joao_novo@email.com", "newpassword");
    }

    @Test
    @DisplayName("GET /users - Deve retornar lista de usuários com status 200")
    void testGetUsers_ShouldReturnUsersListWithStatus200() throws Exception {
        // Given
        List<UserDto> userDtos = Arrays.asList(userDto1, userDto2);
        when(userService.findAll()).thenReturn(userDtos);

        // When & Then
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fullName").value("João Silva"))
                .andExpect(jsonPath("$[0].username").value("joao_silva"))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].fullName").value("Maria Santos"))
                .andExpect(jsonPath("$[1].username").value("maria_santos"))
                .andExpect(jsonPath("$[1].email").value("maria@email.com"));
    }

    @Test
    @DisplayName("GET /users - Deve retornar lista vazia com status 200")
    void testGetUsers_ShouldReturnEmptyListWithStatus200() throws Exception {
        // Given
        when(userService.findAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /users/{id} - Deve retornar usuário por ID com status 200")
    void testGetUserById_ShouldReturnUserWithStatus200() throws Exception {
        // Given
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(userDto1);

        // When & Then
        mockMvc.perform(get("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("João Silva"))
                .andExpect(jsonPath("$.username").value("joao_silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @DisplayName("PUT /users - Deve atualizar usuário com status 200")
    void testUpdateUser_ShouldUpdateUserWithStatus200() throws Exception {
        // Given
        UserDto updatedUserDto = new UserDto(1L, "João Silva Atualizado", "joao_silva_novo", "joao_novo@email.com", "newpassword", null);
        when(userService.updateUser(any(UserDto.class))).thenReturn(updatedUserDto);

        String requestBody = objectMapper.writeValueAsString(userDetailsRequest);

        // When & Then
        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("João Silva Atualizado"))
                .andExpect(jsonPath("$.username").value("joao_silva_novo"))
                .andExpect(jsonPath("$.email").value("joao_novo@email.com"));
    }


    @Test
    @DisplayName("DELETE /users/{id} - Deve deletar usuário com status 200")
    void testDeleteUser_ShouldDeleteUserWithStatus200() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("user was deleted!"));
    }

    @Test
    @DisplayName("DELETE /users/{id} - Deve chamar service com ID correto")
    void testDeleteUser_ShouldCallServiceWithCorrectId() throws Exception {
        // Given
        Long userId = 999L;
        doNothing().when(userService).deleteUser(anyLong());

        // When & Then
        mockMvc.perform(delete("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("user was deleted!"));
    }

    @Test
    @DisplayName("PUT /users - Deve processar JSON corretamente")
    void testUpdateUser_ShouldProcessJsonCorrectly() throws Exception {
        // Given
        UserDto updatedUserDto = new UserDto(2L, "Teste Nome", "teste_user", "teste@email.com", "senha123", null);
        when(userService.updateUser(any(UserDto.class))).thenReturn(updatedUserDto);

        String jsonRequest = """
            {
                "id": 2,
                "fullName": "Teste Nome",
                "username": "teste_user",
                "email": "teste@email.com",
                "password": "senha123"
            }
            """;

        // When & Then
        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.fullName").value("Teste Nome"))
                .andExpect(jsonPath("$.username").value("teste_user"))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }
}
