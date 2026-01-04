package com.fincore.usermgmt.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for User entity lifecycle methods and JPA behavior.
 * Tests @PrePersist, @PreUpdate callbacks, and entity relationships.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("User Entity Tests")
class UserEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    private Role testRole;

    @BeforeEach
    void setUp() {
        // Create a test role for user relationship testing
        testRole = new Role();
        testRole.setName("TEST_USER");
        testRole.setDescription("Test role");
        entityManager.persist(testRole);
        entityManager.flush();
    }

    @Test
    @DisplayName("@PrePersist should set createdDatetime and lastModifiedDatetime on new user")
    void prePersist_shouldSetTimestamps() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(testRole);

        // Act
        LocalDateTime beforePersist = LocalDateTime.now().minusSeconds(1);
        User savedUser = entityManager.persistAndFlush(user);
        LocalDateTime afterPersist = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(savedUser.getCreatedDatetime()).isNotNull();
        assertThat(savedUser.getLastModifiedDatetime()).isNotNull();
        assertThat(savedUser.getCreatedDatetime()).isBetween(beforePersist, afterPersist);
        assertThat(savedUser.getLastModifiedDatetime()).isBetween(beforePersist, afterPersist);
        assertThat(savedUser.getCreatedDatetime()).isEqualTo(savedUser.getLastModifiedDatetime());
    }

    @Test
    @DisplayName("@PreUpdate should update lastModifiedDatetime on user update")
    void preUpdate_shouldUpdateLastModifiedDatetime() throws InterruptedException {
        // Arrange - Create and persist user
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(testRole);
        User savedUser = entityManager.persistAndFlush(user);
        
        LocalDateTime originalCreatedDatetime = savedUser.getCreatedDatetime();
        LocalDateTime originalLastModified = savedUser.getLastModifiedDatetime();
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(100);

        // Act - Update user
        savedUser.setFirstName("Jane");
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        entityManager.persistAndFlush(savedUser);
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(savedUser.getCreatedDatetime()).isEqualTo(originalCreatedDatetime);
        assertThat(savedUser.getLastModifiedDatetime()).isNotEqualTo(originalLastModified);
        assertThat(savedUser.getLastModifiedDatetime()).isBetween(beforeUpdate, afterUpdate);
        assertThat(savedUser.getLastModifiedDatetime()).isAfter(savedUser.getCreatedDatetime());
    }

    @Test
    @DisplayName("User should persist with all fields populated")
    void persist_withAllFields_shouldSucceed() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setMiddleName("Michael");
        user.setLastName("Doe");
        user.setDateOfBirth(LocalDate.of(1990, 5, 15));
        user.setResidentialAddressIdentifier(1);
        user.setPostalAddressIdentifier(2);
        user.setStatusDescription("ACTIVE");
        user.setRole(testRole);

        // Act
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();
        User retrievedUser = entityManager.find(User.class, savedUser.getId());

        // Assert
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(retrievedUser.getFirstName()).isEqualTo("John");
        assertThat(retrievedUser.getMiddleName()).isEqualTo("Michael");
        assertThat(retrievedUser.getLastName()).isEqualTo("Doe");
        assertThat(retrievedUser.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(retrievedUser.getResidentialAddressIdentifier()).isEqualTo(1);
        assertThat(retrievedUser.getPostalAddressIdentifier()).isEqualTo(2);
        assertThat(retrievedUser.getStatusDescription()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("User should persist with only required fields")
    void persist_withMinimalFields_shouldSucceed() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setRole(testRole);

        // Act
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();
        User retrievedUser = entityManager.find(User.class, savedUser.getId());

        // Assert
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(retrievedUser.getEmail()).isNull();
        assertThat(retrievedUser.getFirstName()).isNull();
        assertThat(retrievedUser.getMiddleName()).isNull();
        assertThat(retrievedUser.getLastName()).isNull();
    }

    @Test
    @DisplayName("User should maintain relationship with Role")
    void persist_shouldMaintainRoleRelationship() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(testRole);

        // Act
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();
        User retrievedUser = entityManager.find(User.class, savedUser.getId());

        // Assert
        assertThat(retrievedUser.getRole()).isNotNull();
        assertThat(retrievedUser.getRole().getId()).isEqualTo(testRole.getId());
        assertThat(retrievedUser.getRole().getName()).isEqualTo("TEST_USER");
    }

    @Test
    @DisplayName("User can be updated with new email")
    void update_shouldChangeEmail() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setEmail("old@example.com");
        user.setRole(testRole);
        User savedUser = entityManager.persistAndFlush(user);

        // Act
        savedUser.setEmail("new@example.com");
        entityManager.persistAndFlush(savedUser);
        entityManager.clear();
        User updatedUser = entityManager.find(User.class, savedUser.getId());

        // Assert
        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("User can be updated with new status")
    void update_shouldChangeStatus() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setStatusDescription("ACTIVE");
        user.setRole(testRole);
        User savedUser = entityManager.persistAndFlush(user);

        // Act
        savedUser.setStatusDescription("INACTIVE");
        entityManager.persistAndFlush(savedUser);
        entityManager.clear();
        User updatedUser = entityManager.find(User.class, savedUser.getId());

        // Assert
        assertThat(updatedUser.getStatusDescription()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("User phoneNumber should be unique")
    void persist_duplicatePhoneNumber_shouldFail() {
        // Arrange
        User user1 = new User();
        user1.setPhoneNumber("+1234567890");
        user1.setRole(testRole);
        entityManager.persistAndFlush(user1);

        User user2 = new User();
        user2.setPhoneNumber("+1234567890");
        user2.setRole(testRole);

        // Act & Assert
        try {
            entityManager.persistAndFlush(user2);
            assertThat(false).as("Should have thrown exception for duplicate phone number").isTrue();
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("User should persist with future date of birth")
    void persist_withFutureDateOfBirth_shouldSucceed() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setDateOfBirth(LocalDate.now().plusYears(1));
        user.setRole(testRole);

        // Act
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();
        User retrievedUser = entityManager.find(User.class, savedUser.getId());

        // Assert
        assertThat(retrievedUser.getDateOfBirth()).isAfter(LocalDate.now());
    }

    @Test
    @DisplayName("User should handle null optional fields")
    void persist_withNullOptionalFields_shouldSucceed() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setEmail(null);
        user.setMiddleName(null);
        user.setDateOfBirth(null);
        user.setRole(testRole);

        // Act
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();
        User retrievedUser = entityManager.find(User.class, savedUser.getId());

        // Assert
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getEmail()).isNull();
        assertThat(retrievedUser.getMiddleName()).isNull();
        assertThat(retrievedUser.getDateOfBirth()).isNull();
    }

    @Test
    @DisplayName("Multiple users can share the same role")
    void persist_multipleUsersWithSameRole_shouldSucceed() {
        // Arrange
        User user1 = new User();
        user1.setPhoneNumber("+1234567890");
        user1.setRole(testRole);

        User user2 = new User();
        user2.setPhoneNumber("+9876543210");
        user2.setRole(testRole);

        // Act
        User savedUser1 = entityManager.persistAndFlush(user1);
        User savedUser2 = entityManager.persistAndFlush(user2);

        // Assert
        assertThat(savedUser1.getRole().getId()).isEqualTo(savedUser2.getRole().getId());
    }

    @Test
    @DisplayName("User timestamps should not be null after persistence")
    void persist_timestampsShouldNeverBeNull() {
        // Arrange
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setRole(testRole);

        // Act
        User savedUser = entityManager.persistAndFlush(user);

        // Assert
        assertThat(savedUser.getCreatedDatetime()).as("Created datetime should not be null").isNotNull();
        assertThat(savedUser.getLastModifiedDatetime()).as("Last modified datetime should not be null").isNotNull();
    }
}
