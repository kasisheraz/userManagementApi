package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.mapper.UserMapper;
import com.fincore.usermgmt.repository.RoleRepository;
import com.fincore.usermgmt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setPhoneNumber("+1234567890");
        createDTO.setEmail("test@example.com");
        createDTO.setFirstName("Test");
        createDTO.setLastName("User");
        createDTO.setRole("SYSTEM_ADMINISTRATOR");

        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setFirstName("Test");
        user.setLastName("User");

        Role role = new Role();
        role.setName("SYSTEM_ADMINISTRATOR");

        UserDTO userDTO = new UserDTO();
        userDTO.setPhoneNumber("+1234567890");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");

        when(userMapper.toUser(any(UserCreateDTO.class))).thenReturn(user);
        when(roleRepository.findByName("SYSTEM_ADMINISTRATOR")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);

        UserDTO result = userService.createUser(createDTO);

        assertEquals("+1234567890", result.getPhoneNumber());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
    }
}
