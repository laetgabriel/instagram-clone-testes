package br.edu.ifpb.instagram.repository.integration;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração com o banco de dados usando DataJpaTest
 * - Carrega apenas os componentes JPA
 */

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Testes de Integração do UserRepository")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    public void setUp() {
        // Criação de usuários de teste usando setters
        user1 = new UserEntity();
        user1.setFullName("João Silva");
        user1.setUsername("joao123");
        user1.setEmail("joao@email.com");
        user1.setEncryptedPassword("123");
    

        user2 = new UserEntity();
        user2.setFullName("Maria Santos");
        user2.setUsername("maria456");
        user2.setEmail("maria@email.com");
        user2.setEncryptedPassword("123");

        // Salva os dados no banco em memória
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    @DisplayName("Deve salvar usuário corretamente")
    public void saveUser_ShouldPersistInDatabase() {
        UserEntity newUser = new UserEntity();
        newUser.setFullName("Pedro Oliveira");
        newUser.setUsername("pedro789");
        newUser.setEmail("pedro@email.com");
        newUser.setEncryptedPassword("123456");

        UserEntity saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("pedro789");
    }

    @Test
    @DisplayName("Deve encontrar usuário por ID existente")
    public void findById_WithValidId_ShouldReturnUser() {
        Optional<UserEntity> found = userRepository.findById(user1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("joao123");
    }

    @Test
    @DisplayName("Não deve encontrar usuário por ID inexistente")
    public void findById_WithInvalidId_ShouldReturnEmpty() {
        Optional<UserEntity> found = userRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar todos os usuários cadastrados")
    public void findAll_ShouldReturnAllUsers() {
        List<UserEntity> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting("username").containsExactlyInAnyOrder("joao123", "maria456");
    }

    @Test
    @DisplayName("Deve verificar existência de usuário pelo email")
    public void existsByEmail_ShouldReturnTrueWhenEmailExists() {
        boolean exists = userRepository.existsByEmail("joao@email.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve verificar inexistência de usuário pelo email")
    public void existsByEmail_ShouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("naoexiste@email.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve encontrar usuário por username")
    public void findByUsername_ShouldReturnUserWhenExists() {
        Optional<UserEntity> found = userRepository.findByUsername("maria456");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("maria@email.com");
    }


    @Test
    @DisplayName("Deve deletar usuário por ID")
    public void deleteUser_ShouldRemoveFromDatabase() {
        userRepository.deleteById(user2.getId());

        Optional<UserEntity> found = userRepository.findById(user2.getId());
        assertThat(found).isEmpty();
    }
}
