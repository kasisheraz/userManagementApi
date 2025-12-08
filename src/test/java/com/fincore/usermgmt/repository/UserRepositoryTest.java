package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        Optional<User> user = userRepository.findByUsername("admin");
        
        assertTrue(user.isPresent());
        assertEquals("admin", user.get().getUsername());
        assertEquals("System Administrator", user.get().getFullName());
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> user = userRepository.findByUsername("nonexistent");
        
        assertFalse(user.isPresent());
    }

    @Test
    void existsByUsername_WhenUserExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByUsername("admin");
        
        assertTrue(exists);
    }

    @Test
    void existsByUsername_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        
        assertFalse(exists);
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByEmail("admin@fincore.com");
        
        assertTrue(exists);
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@test.com");
        
        assertFalse(exists);
    }

    @Test
    void save_ShouldPersistUser() {
        Role role = entityManager.find(Role.class, 1L);
        
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        newUser.setFullName("New User");
        newUser.setEmail("newuser@test.com");
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setRole(role);
        newUser.setFailedLoginAttempts(0);

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }
}
