package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.entity.Address;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.mapper.UserMapper;
import com.fincore.usermgmt.repository.AddressRepository;
import com.fincore.usermgmt.repository.RoleRepository;
import com.fincore.usermgmt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

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
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.createUser(createDTO);

        assertNotNull(result);
        assertEquals("+1234567890", result.getPhoneNumber());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserWithAddresses() {
        // Setup DTOs with addresses
        AddressCreateDTO residentialAddress = new AddressCreateDTO();
        residentialAddress.setTypeCode(1);
        residentialAddress.setAddressLine1("123 Main Street");
        residentialAddress.setCity("London");
        residentialAddress.setCountry("United Kingdom");
        residentialAddress.setPostalCode("SW1A 1AA");

        AddressCreateDTO postalAddress = new AddressCreateDTO();
        postalAddress.setTypeCode(5);
        postalAddress.setAddressLine1("456 Oak Avenue");
        postalAddress.setCity("Manchester");
        postalAddress.setCountry("United Kingdom");
        postalAddress.setPostalCode("M1 1AA");

        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setPhoneNumber("+1234567890");
        createDTO.setEmail("test@example.com");
        createDTO.setFirstName("Test");
        createDTO.setLastName("User");
        createDTO.setRole("USER");
        createDTO.setResidentialAddress(residentialAddress);
        createDTO.setPostalAddress(postalAddress);

        // Setup entities
        Address savedResidential = new Address();
        savedResidential.setId(1L);
        savedResidential.setTypeCode(1);
        savedResidential.setAddressLine1("123 Main Street");
        savedResidential.setCity("London");

        Address savedPostal = new Address();
        savedPostal.setId(2L);
        savedPostal.setTypeCode(5);
        savedPostal.setAddressLine1("456 Oak Avenue");
        savedPostal.setCity("Manchester");

        User user = new User();
        user.setId(1L);
        user.setPhoneNumber("+1234567890");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setResidentialAddressIdentifier(1L);
        user.setPostalAddressIdentifier(2L);

        Role role = new Role();
        role.setName("USER");

        AddressDTO residentialDTO = new AddressDTO();
        residentialDTO.setId(1L);
        residentialDTO.setAddressLine1("123 Main Street");
        
        AddressDTO postalDTO = new AddressDTO();
        postalDTO.setId(2L);
        postalDTO.setAddressLine1("456 Oak Avenue");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setPhoneNumber("+1234567890");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setResidentialAddress(residentialDTO);
        userDTO.setPostalAddress(postalDTO);

        // Mock behaviors
        when(addressService.createAddress(any(AddressCreateDTO.class)))
            .thenReturn(savedResidential)
            .thenReturn(savedPostal);
        when(userMapper.toUser(any(UserCreateDTO.class))).thenReturn(user);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(savedResidential));
        when(addressRepository.findById(2L)).thenReturn(Optional.of(savedPostal));
        when(addressMapper.toAddressDTO(savedResidential)).thenReturn(residentialDTO);
        when(addressMapper.toAddressDTO(savedPostal)).thenReturn(postalDTO);

        // Execute
        UserDTO result = userService.createUser(createDTO);

        // Verify
        assertNotNull(result);
        assertEquals("Test", result.getFirstName());
        assertNotNull(result.getResidentialAddress());
        assertNotNull(result.getPostalAddress());
        assertEquals("123 Main Street", result.getResidentialAddress().getAddressLine1());
        assertEquals("456 Oak Avenue", result.getPostalAddress().getAddressLine1());
        
        verify(addressService, times(2)).createAddress(any(AddressCreateDTO.class));
        verify(userRepository).save(any(User.class));
    }
}
