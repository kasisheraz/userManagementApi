package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void toUserDTO_shouldMapAllFields() {
        // Given
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");
        role.setDescription("Administrator role");

        User user = new User();
        user.setId(1L);
        user.setPhoneNumber("+44-7700-900123");
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setMiddleName("Michael");
        user.setLastName("Doe");
        user.setDateOfBirth(LocalDate.of(1990, 1, 15));
        user.setResidentialAddressIdentifier(100);
        user.setPostalAddressIdentifier(200);
        user.setRole(role);
        user.setStatusDescription("ACTIVE");
        user.setCreatedDatetime(LocalDateTime.now());

        // When
        UserDTO dto = mapper.toUserDTO(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPhoneNumber()).isEqualTo("+44-7700-900123");
        assertThat(dto.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getMiddleName()).isEqualTo("Michael");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(dto.getResidentialAddressIdentifier()).isEqualTo(100);
        assertThat(dto.getPostalAddressIdentifier()).isEqualTo(200);
        assertThat(dto.getRole()).isEqualTo("ADMIN");
        assertThat(dto.getStatusDescription()).isEqualTo("ACTIVE");
    }

    @Test
    void toUserDTO_withNullRole_shouldMapWithNullRoleName() {
        // Given
        User user = new User();
        user.setId(2L);
        user.setPhoneNumber("+44-7700-900456");
        user.setEmail("jane.smith@example.com");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setRole(null);

        // When
        UserDTO dto = mapper.toUserDTO(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getRole()).isNull();
        assertThat(dto.getPhoneNumber()).isEqualTo("+44-7700-900456");
    }

    @Test
    void toUserDTO_withRoleButNullName_shouldMapWithNullRoleName() {
        // Given
        Role role = new Role();
        role.setId(3L);
        role.setName(null);
        role.setDescription("Role with null name");

        User user = new User();
        user.setId(3L);
        user.setPhoneNumber("+44-7700-900789");
        user.setEmail("test.user@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);

        // When
        UserDTO dto = mapper.toUserDTO(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getRole()).isNull();
    }

    @Test
    void toUserDTO_withMinimalFields_shouldMapSuccessfully() {
        // Given
        User user = new User();
        user.setId(4L);
        user.setPhoneNumber("+44-7700-900111");
        user.setEmail("minimal@example.com");

        // When
        UserDTO dto = mapper.toUserDTO(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(4L);
        assertThat(dto.getPhoneNumber()).isEqualTo("+44-7700-900111");
        assertThat(dto.getEmail()).isEqualTo("minimal@example.com");
        assertThat(dto.getFirstName()).isNull();
        assertThat(dto.getLastName()).isNull();
        assertThat(dto.getRole()).isNull();
    }

    @Test
    void toUser_shouldMapCreateDTO() {
        // Given
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("+44-7700-900222");
        dto.setEmail("new.user@example.com");
        dto.setFirstName("New");
        dto.setMiddleName("Test");
        dto.setLastName("User");
        dto.setDateOfBirth(LocalDate.of(1995, 5, 20));
        dto.setResidentialAddressIdentifier(300);
        dto.setPostalAddressIdentifier(400);
        dto.setRole("USER");
        dto.setStatusDescription("PENDING");

        // When
        User user = mapper.toUser(dto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getPhoneNumber()).isEqualTo("+44-7700-900222");
        assertThat(user.getEmail()).isEqualTo("new.user@example.com");
        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getMiddleName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 20));
        assertThat(user.getResidentialAddressIdentifier()).isEqualTo(300);
        assertThat(user.getPostalAddressIdentifier()).isEqualTo(400);
        assertThat(user.getRole()).isNull(); // Role is ignored in mapper
    }

    @Test
    void toUser_withMinimalCreateDTO_shouldMapSuccessfully() {
        // Given
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("+44-7700-900333");
        dto.setEmail("simple@example.com");
        dto.setFirstName("Simple");
        dto.setLastName("User");

        // When
        User user = mapper.toUser(dto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getPhoneNumber()).isEqualTo("+44-7700-900333");
        assertThat(user.getEmail()).isEqualTo("simple@example.com");
        assertThat(user.getFirstName()).isEqualTo("Simple");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getMiddleName()).isNull();
        assertThat(user.getDateOfBirth()).isNull();
    }

    @Test
    void updateUserFromDto_shouldUpdateNonNullFields() {
        // Given
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        User existing = new User();
        existing.setId(1L);
        existing.setPhoneNumber("+44-1111-111111");
        existing.setEmail("old@example.com");
        existing.setFirstName("Old");
        existing.setMiddleName("Middle");
        existing.setLastName("Name");
        existing.setDateOfBirth(LocalDate.of(1980, 1, 1));
        existing.setResidentialAddressIdentifier(500);
        existing.setPostalAddressIdentifier(600);
        existing.setRole(role);
        existing.setStatusDescription("ACTIVE");

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setPhoneNumber("+44-2222-222222");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Username");
        updateDTO.setDateOfBirth(LocalDate.of(1985, 6, 15));

        // When
        mapper.updateUserFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getPhoneNumber()).isEqualTo("+44-2222-222222");
        assertThat(existing.getEmail()).isEqualTo("updated@example.com");
        assertThat(existing.getFirstName()).isEqualTo("Updated");
        assertThat(existing.getLastName()).isEqualTo("Username");
        assertThat(existing.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 6, 15));
        assertThat(existing.getStatusDescription()).isEqualTo("ACTIVE"); // Not updated (ignored)
        assertThat(existing.getRole()).isEqualTo(role); // Not updated (ignored)
    }

    @Test
    void updateUserFromDto_withNullValues_shouldNotUpdateExisting() {
        // Given
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        User existing = new User();
        existing.setId(1L);
        existing.setPhoneNumber("+44-3333-333333");
        existing.setEmail("preserved@example.com");
        existing.setFirstName("Preserved");
        existing.setMiddleName("Middle");
        existing.setLastName("Name");
        existing.setDateOfBirth(LocalDate.of(1990, 3, 10));
        existing.setResidentialAddressIdentifier(700);
        existing.setPostalAddressIdentifier(800);
        existing.setRole(role);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setPhoneNumber("+44-4444-444444");
        updateDTO.setFirstName("Updated");
        updateDTO.setEmail("updated@example.com");
        // lastName, middleName, dateOfBirth, addresses are null

        // When
        mapper.updateUserFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getPhoneNumber()).isEqualTo("+44-4444-444444"); // Updated
        assertThat(existing.getFirstName()).isEqualTo("Updated"); // Updated
        assertThat(existing.getEmail()).isEqualTo("updated@example.com"); // Updated
        assertThat(existing.getLastName()).isEqualTo("Name"); // Not updated (null in DTO)
        assertThat(existing.getMiddleName()).isEqualTo("Middle"); // Not updated (null in DTO)
        assertThat(existing.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 3, 10)); // Not updated (null in DTO)
        assertThat(existing.getResidentialAddressIdentifier()).isEqualTo(700); // Not updated
        assertThat(existing.getPostalAddressIdentifier()).isEqualTo(800); // Not updated
    }

    @Test
    void updateUserFromDto_withPartialUpdate_shouldPreserveOtherFields() {
        // Given
        User existing = new User();
        existing.setId(1L);
        existing.setPhoneNumber("+44-5555-555555");
        existing.setEmail("user@example.com");
        existing.setFirstName("First");
        existing.setMiddleName("Middle");
        existing.setLastName("Last");
        existing.setDateOfBirth(LocalDate.of(1992, 7, 20));

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("NewFirst");
        updateDTO.setLastName("NewLast");

        // When
        mapper.updateUserFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getFirstName()).isEqualTo("NewFirst"); // Updated
        assertThat(existing.getLastName()).isEqualTo("NewLast"); // Updated
        assertThat(existing.getPhoneNumber()).isEqualTo("+44-5555-555555"); // Preserved
        assertThat(existing.getEmail()).isEqualTo("user@example.com"); // Preserved
        assertThat(existing.getMiddleName()).isEqualTo("Middle"); // Preserved
        assertThat(existing.getDateOfBirth()).isEqualTo(LocalDate.of(1992, 7, 20)); // Preserved
    }

    @Test
    void toUserDTO_withDifferentRoles_shouldMapRoleNameCorrectly() {
        // Test USER role
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");
        User user1 = new User();
        user1.setId(1L);
        user1.setPhoneNumber("+44-6666-666666");
        user1.setEmail("user1@example.com");
        user1.setRole(userRole);
        assertThat(mapper.toUserDTO(user1).getRole()).isEqualTo("USER");

        // Test ADMIN role
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
        User user2 = new User();
        user2.setId(2L);
        user2.setPhoneNumber("+44-7777-777777");
        user2.setEmail("user2@example.com");
        user2.setRole(adminRole);
        assertThat(mapper.toUserDTO(user2).getRole()).isEqualTo("ADMIN");

        // Test SUPER_ADMIN role
        Role superAdminRole = new Role();
        superAdminRole.setId(3L);
        superAdminRole.setName("SUPER_ADMIN");
        User user3 = new User();
        user3.setId(3L);
        user3.setPhoneNumber("+44-8888-888888");
        user3.setEmail("user3@example.com");
        user3.setRole(superAdminRole);
        assertThat(mapper.toUserDTO(user3).getRole()).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void toUserDTO_withLongNames_shouldMapSuccessfully() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setPhoneNumber("+44-7700-900123-ext-9999");
        user.setEmail("very.long.email.address@very-long-domain-name.example.com");
        user.setFirstName("VeryLongFirstNameThatExceedsNormalLengthExpectations");
        user.setMiddleName("VeryLongMiddleNameThatExceedsNormalLengthExpectations");
        user.setLastName("VeryLongLastNameThatExceedsNormalLengthExpectations");

        // When
        UserDTO dto = mapper.toUserDTO(user);

        // Then
        assertThat(dto.getPhoneNumber()).isEqualTo("+44-7700-900123-ext-9999");
        assertThat(dto.getFirstName()).isEqualTo("VeryLongFirstNameThatExceedsNormalLengthExpectations");
        assertThat(dto.getMiddleName()).isEqualTo("VeryLongMiddleNameThatExceedsNormalLengthExpectations");
        assertThat(dto.getLastName()).isEqualTo("VeryLongLastNameThatExceedsNormalLengthExpectations");
        assertThat(dto.getEmail()).isEqualTo("very.long.email.address@very-long-domain-name.example.com");
    }

    @Test
    void updateUserFromDto_withEmptyStrings_shouldUpdateToEmptyStrings() {
        // Given
        User existing = new User();
        existing.setId(1L);
        existing.setPhoneNumber("+44-9999-999999");
        existing.setFirstName("First");
        existing.setLastName("Last");
        existing.setEmail("user@example.com");

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("");
        updateDTO.setLastName("");

        // When
        mapper.updateUserFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getFirstName()).isEmpty();
        assertThat(existing.getLastName()).isEmpty();
    }
}
