package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
    void testFindById_WhenUserExists_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        UserDto found = userService.findById(1L);

        assertNotNull(found);
        assertEquals(userEntity.getId(), found.id());
        assertEquals(userEntity.getFullName(), found.fullName());
        assertEquals(userEntity.getEmail(), found.email());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findById(999L));
        assertEquals("User not found with id: 999", exception.getMessage());

        verify(userRepository, times(1)).findById(999L);
    }

    // ------------------ CREATE USER ------------------
    @Test
    void testCreateUser_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);
        UserDto dto = new UserDto(null, "Name", "username", "existing@email.com", "123", "123");

        FieldAlreadyExistsException exception = assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(dto));
        assertEquals("E-email already in use.", exception.getMessage());
    }

    @Test
    void testCreateUser_WhenValidUser_ShouldSaveAndReturnUserDto() {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserDto saved = userService.createUser(userDto);

        assertNotNull(saved);
        assertEquals(2L, saved.id());
        assertEquals(userDto.fullName(), saved.fullName());
        assertEquals(userDto.email(), saved.email());

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_WhenValidUser_ShouldEncodePassword() {
        when(passwordEncoder.encode(userDto.password())).thenReturn("encrypted123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> {
            UserEntity u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserDto saved = userService.createUser(userDto);

        assertNotNull(saved);
        assertEquals(2L, saved.id());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encrypted123", captor.getValue().getEncryptedPassword());
        verify(passwordEncoder, times(1)).encode(userDto.password());
    }

    // ------------------ DELETE USER ------------------
    @Test
    void testDeleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.existsById(2L)).thenReturn(true);

        userService.deleteUser(2L);

        verify(userRepository).existsById(2L);
        verify(userRepository).deleteById(2L);
    }

    @Test
    void testDeleteUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(99L));
        assertEquals("User not found with id: 99", exception.getMessage());

        verify(userRepository).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // ------------------ UPDATE USER ------------------
    @Test
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

        assertNotNull(updated);
        assertEquals("Gabriel Laet Updated", updated.fullName());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encryptedNewPass", captor.getValue().getEncryptedPassword());
    }

    @Test
    void testUpdateUser_WhenDtoIsNullOrIdIsNull_ShouldThrowException() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
        assertEquals("UserDto or UserDto.id must not be null", exception1.getMessage());

        UserDto dtoWithNullId = new UserDto(null, "Name", "username", "email@test.com", "123456", "123456");
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(dtoWithNullId));
        assertEquals("UserDto or UserDto.id must not be null", exception2.getMessage());
    }

    // ------------------ FIND ALL ------------------
    @Test
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

        assertEquals(2, users.size());
        assertEquals("josefina", users.get(0).fullName());
        assertEquals("josegrosso", users.get(1).fullName());
    }
}
