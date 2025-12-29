package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
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
    void findByPhoneNumber_WhenUserExists_ShouldReturnUser() {
        Optional<User> user = userRepository.findByPhoneNumber("+1234567890");
        
        assertTrue(user.isPresent());
        assertEquals("+1234567890", user.get().getPhoneNumber());
        assertEquals("System", user.get().getFirstName());
        assertEquals("Administrator", user.get().getLastName());
    }

    @Test
    void findByPhoneNumber_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> user = userRepository.findByPhoneNumber("+9999999999");
        
        assertFalse(user.isPresent());
    }

    @Test
    void existsByPhoneNumber_WhenUserExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByPhoneNumber("+1234567890");
        
        assertTrue(exists);
    }

    @Test
    void existsByPhoneNumber_WhenUserDoesNotExist_ShouldReturnFalse() {
        boolean exists = userRepository.existsByPhoneNumber("+9999999999");
        
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
        newUser.setPhoneNumber("+1111111111");
        newUser.setEmail("newuser@test.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        newUser.setStatusDescription("ACTIVE");
        newUser.setRole(role);

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("+1111111111", savedUser.getPhoneNumber());
        assertEquals("New", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertNotNull(savedUser.getCreatedDatetime());
        assertNotNull(savedUser.getLastModifiedDatetime());
    }
}
