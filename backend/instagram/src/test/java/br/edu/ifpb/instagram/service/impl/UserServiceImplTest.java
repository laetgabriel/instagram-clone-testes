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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
    UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    @Autowired
    UserServiceImpl userService;

    // ------------------ FIND BY ID ------------------
    @Test
    void testFindById_WhenUserExists_ShouldReturnUserDto() {
        Long userId = 1L;

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setId(userId);
        mockUserEntity.setFullName("Paulo Pereira");
        mockUserEntity.setEmail("paulo@ppereira.dev");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUserEntity));

        UserDto userDto = userService.findById(userId);

        assertNotNull(userDto);
        assertEquals(mockUserEntity.getId(), userDto.id());
        assertEquals(mockUserEntity.getFullName(), userDto.fullName());
        assertEquals(mockUserEntity.getEmail(), userDto.email());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testFindById_ThrowsExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findById(userId));
        assertEquals("User not found with id: 999", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
    }

    // ------------------ CREATE USER ------------------
    @Test
    void testCreateUser_ThrowsExceptionWhenEmailExists() {
        UserDto userDto = new UserDto(null, "Name", "username", "existing@email.com", "123", "123");
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        FieldAlreadyExistsException exception = assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(userDto));
        assertEquals("E-email already in use.", exception.getMessage());
    }

    @Test
    void testCreateUser_SuccessWhenValidUser() {
        UserDto userDto = new UserDto(null, "Gabriel Laet", "laet", "gabriellaetfm12@gmail.com", "123456", "123456");

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserDto savedUser = userService.createUser(userDto);

        assertNotNull(savedUser);
        assertEquals(2L, savedUser.id());
        assertEquals(userDto.fullName(), savedUser.fullName());
        assertEquals(userDto.email(), savedUser.email());

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_SuccessWithPasswordEncoding() {
        UserDto userDto = new UserDto(null, "Gabriel Laet", "laet", "gabriellaetfm12@gmail.com", "123456", "123456");

        when(passwordEncoder.encode("123456")).thenReturn("bananinha123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> {
            UserEntity user = i.getArgument(0);
            user.setId(2L);
            return user;
        });

        UserDto savedUser = userService.createUser(userDto);

        assertNotNull(savedUser);
        assertEquals(2L, savedUser.id());
        assertEquals(userDto.fullName(), savedUser.fullName());
        assertEquals(userDto.email(), savedUser.email());

        verify(passwordEncoder, times(1)).encode("123456");
        verify(userRepository, times(1)).save(any(UserEntity.class));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("bananinha123", captor.getValue().getEncryptedPassword());
    }

    // ------------------ DELETE USER ------------------
    @Test
    void testDeleteUser_SuccessWhenUserExists() {
        when(userRepository.existsById(2L)).thenReturn(true);

        userService.deleteUser(2L);

        verify(userRepository).existsById(2L);
        verify(userRepository).deleteById(2L);
    }

    @Test
    void testDeleteUser_ThrowsExceptionWhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(99L));
        assertEquals("User not found with id: 99", exception.getMessage());

        verify(userRepository).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // ------------------ UPDATE USER ------------------
    @Test
    void testUpdateUser_SuccessWithPasswordEncoding() {
        UserDto userDto = new UserDto(2L, "Gabriel Laet Updated", "laet", "gabriellaetfm12@gmail.com", "newpass", "newpass");

        UserEntity existingUser = new UserEntity();
        existingUser.setId(2L);
        existingUser.setFullName("Gabriel Laet");
        existingUser.setEmail("gabriellaetfm12@gmail.com");
        existingUser.setUsername("laet");
        existingUser.setEncryptedPassword("oldpass");

        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newpass")).thenReturn("encryptedNewPass");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updatedUser = userService.updateUser(userDto);

        assertNotNull(updatedUser);
        assertEquals("Gabriel Laet Updated", updatedUser.fullName());

        verify(userRepository).findById(2L);
        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(any(UserEntity.class));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encryptedNewPass", captor.getValue().getEncryptedPassword());
    }

    @Test
    void testUpdateUser_ThrowsExceptionWhenDtoIsNullOrIdIsNull() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
        assertEquals("UserDto or UserDto.id must not be null", exception1.getMessage());

        UserDto userDtoWithNullId = new UserDto(null, "Name", "username", "email@test.com", "123456", "123456");
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userDtoWithNullId));
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

        verify(userRepository).findAll();
    }
}
