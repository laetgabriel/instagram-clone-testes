package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

@SpringBootTest
public class UserServiceImplTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    @Autowired
    private UserServiceImpl userService;

    private UserDto userDto;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(null, "Gabriel Laet", "laet", "gabriellaetfm12@gmail.com", "123456", "123456");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFullName("Gabriel Laet");
        userEntity.setUsername("laet");
        userEntity.setEmail("gabriellaetfm12@gmail.com");
        userEntity.setEncryptedPassword("123456");

        Mockito.reset(userRepository, passwordEncoder);
    }

    // ------------------ FIND BY ID ------------------
    @Test
    @DisplayName("Deve retornar usuário quando o ID existir")
    void testFindById_WhenUserExists_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        UserDto found = userService.findById(1L);

        assertAll("Validação do usuário encontrado",
            () -> assertNotNull(found, "O usuário não deve ser nulo"),
            () -> assertEquals(userEntity.getId(), found.id(), "O ID deve ser igual"),
            () -> assertEquals(userEntity.getFullName(), found.fullName(), "O nome deve ser igual"),
            () -> assertEquals(userEntity.getEmail(), found.email(), "O email deve ser igual")
        );

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for encontrado")
    void testFindById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.findById(999L));
        assertAll("Validação da exceção de usuário não encontrado",
            () -> assertEquals("User not found with id: 999", ex.getMessage(), "Mensagem da exceção incorreta"),
            () -> verify(userRepository, times(1)).findById(999L)
        );
    }

    // ------------------ CREATE USER ------------------
    @Test
    @DisplayName("Deve lançar exceção quando o email já existir")
    void testCreateUser_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);
        UserDto dto = new UserDto(null, "Name", "username", "existing@email.com", "123", "123");

        FieldAlreadyExistsException ex =
            assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(dto));

        assertAll("Validação da exceção de email existente",
            () -> assertEquals("E-email already in use.", ex.getMessage(), "Mensagem da exceção incorreta"),
            () -> verify(userRepository, times(1)).existsByEmail("existing@email.com")
        );
    }

    @Test
    @DisplayName("Deve salvar usuário válido e retornar UserDto")
    void testCreateUser_WhenValidUser_ShouldSaveAndReturnUserDto() {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserDto saved = userService.createUser(userDto);

        assertAll("Validação do usuário salvo",
            () -> assertNotNull(saved, "O usuário salvo não deve ser nulo"),
            () -> assertEquals(2L, saved.id(), "O ID deve ser 2"),
            () -> assertEquals(userDto.fullName(), saved.fullName(), "O nome deve ser igual"),
            () -> assertEquals(userDto.email(), saved.email(), "O email deve ser igual")
        );

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve salvar usuário válido e criptografar senha")
    void testCreateUser_WhenValidUser_ShouldEncodePassword() {
        when(passwordEncoder.encode(userDto.password())).thenReturn("encrypted123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> {
            UserEntity u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserDto saved = userService.createUser(userDto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity captured = captor.getValue();

        assertAll("Validação da criptografia de senha",
            () -> assertNotNull(saved, "O usuário salvo não deve ser nulo"),
            () -> assertEquals(2L, saved.id(), "O ID deve ser 2"),
            () -> assertEquals("encrypted123", captured.getEncryptedPassword(), "A senha deve estar criptografada")
        );

        verify(passwordEncoder, times(1)).encode(userDto.password());
    }

    // ------------------ DELETE USER ------------------
    @Test
    @DisplayName("Deve deletar usuário existente")
    void testDeleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.existsById(2L)).thenReturn(true);

        userService.deleteUser(2L);

        assertAll("Validação da deleção de usuário",
            () -> verify(userRepository).existsById(2L),
            () -> verify(userRepository).deleteById(2L)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar usuário inexistente")
    void testDeleteUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteUser(99L));

        assertAll("Validação da exceção ao deletar",
            () -> assertEquals("User not found with id: 99", ex.getMessage(), "Mensagem da exceção incorreta"),
            () -> verify(userRepository).existsById(99L),
            () -> verify(userRepository, never()).deleteById(anyLong())
        );
    }

    // ------------------ UPDATE USER ------------------
    @Test
    @DisplayName("Deve atualizar usuário válido e criptografar senha")
    void testUpdateUser_WhenValidUser_ShouldUpdateAndEncodePassword() {
        UserDto updateDto = new UserDto(2L, "Gabriel Laet Updated", "laet", "gabriellaetfm12@gmail.com", "newpass", "newpass");

        UserEntity existing = new UserEntity();
        existing.setId(2L);
        existing.setFullName("Gabriel Laet");
        existing.setEmail("gabriellaetfm12@gmail.com");
        existing.setUsername("laet");
        existing.setEncryptedPassword("oldpass");

        when(userRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("encryptedNewPass");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updated = userService.updateUser(updateDto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity captured = captor.getValue();

        assertAll("Validação da atualização de usuário",
            () -> assertNotNull(updated, "Usuário atualizado não deve ser nulo"),
            () -> assertEquals("Gabriel Laet Updated", updated.fullName(), "O nome deve ser atualizado"),
            () -> assertEquals("encryptedNewPass", captured.getEncryptedPassword(), "Senha deve estar criptografada")
        );

        verify(userRepository).findById(2L);
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o username já existir")
    public void testCreateUser_WhenUsernameExists_ShouldThrowException() {
        when(userRepository.existsByUsername("laet")).thenReturn(true);

        UserDto dto = new UserDto(null, "Gabriel Laet", "laet", "gabriellaetfm12@gmail.com", "123456", "123456");

        FieldAlreadyExistsException ex =
            assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(dto));

        assertAll("Validação da exceção de username existente",
            () -> assertEquals("Username already in use.", ex.getMessage()),
            () -> verify(userRepository, times(1)).existsByUsername("laet")
        );
    }


    @Test
    @DisplayName("Deve lançar exceção quando DTO ou ID forem nulos")
    void testUpdateUser_WhenDtoIsNullOrIdIsNull_ShouldThrowException() {
        assertAll("Validação de exceções ao atualizar",
            () -> {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> userService.updateUser(null));
                assertEquals("UserDto or UserDto.id must not be null", ex.getMessage(), "Mensagem incorreta");
            },
            () -> {
                UserDto dtoWithNullId = new UserDto(null, "Name", "username", "email@test.com", "123456", "123456");
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> userService.updateUser(dtoWithNullId));
                assertEquals("UserDto or UserDto.id must not be null", ex.getMessage(), "Mensagem incorreta");
            }
        );
    }

    // ------------------ FIND ALL ------------------
    @Test
    @DisplayName("Deve retornar lista de usuários quando existirem")
    void testFindAllUsers_WhenUsersExist_ShouldReturnListOfUserDto() {
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setFullName("josefina");
        user1.setUsername("user1");
        user1.setEmail("user1@email.com");

        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setFullName("josegrosso");
        user2.setUsername("user2");
        user2.setEmail("user2@email.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> users = userService.findAll();

        assertAll("Validação da lista de usuários",
            () -> assertEquals(2, users.size(), "Deve ter 2 usuários"),
            () -> assertEquals("josefina", users.get(0).fullName(), "Primeiro nome incorreto"),
            () -> assertEquals("josegrosso", users.get(1).fullName(), "Segundo nome incorreto")
        );
    }
}
