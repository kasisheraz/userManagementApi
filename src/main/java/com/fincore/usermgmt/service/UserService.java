package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.mapper.UserMapper;
import com.fincore.usermgmt.repository.AddressRepository;
import com.fincore.usermgmt.repository.RoleRepository;
import com.fincore.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final AddressService addressService;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDTOWithAddresses)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserDTOWithAddresses);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Creating user with phone: {}", userCreateDTO.getPhoneNumber());
        
        User user = userMapper.toUser(userCreateDTO);
        
        // Handle role
        if (userCreateDTO.getRole() != null) {
            Role role = roleRepository.findByName(userCreateDTO.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + userCreateDTO.getRole()));
            user.setRole(role);
        }
        
        // Create residential address if provided
        if (userCreateDTO.getResidentialAddress() != null) {
            log.info("Creating residential address for user");
            AddressDTO residentialAddress = addressService.createAddress(userCreateDTO.getResidentialAddress());
            user.setResidentialAddressIdentifier(residentialAddress.getId());
        }
        
        // Create postal address if provided
        if (userCreateDTO.getPostalAddress() != null) {
            log.info("Creating postal address for user");
            AddressDTO postalAddress = addressService.createAddress(userCreateDTO.getPostalAddress());
            user.setPostalAddressIdentifier(postalAddress.getId());
        }
        
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        
        return toUserDTOWithAddresses(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        log.info("Updating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        userMapper.updateUserFromDto(userUpdateDTO, user);
        
        // Handle role update
        if (userUpdateDTO.getRole() != null) {
            Role role = roleRepository.findByName(userUpdateDTO.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + userUpdateDTO.getRole()));
            user.setRole(role);
        }
        
        // Update or create residential address
        if (userUpdateDTO.getResidentialAddress() != null) {
            if (user.getResidentialAddressIdentifier() != null) {
                log.info("Updating existing residential address: {}", user.getResidentialAddressIdentifier());
                addressService.updateAddress(user.getResidentialAddressIdentifier(), 
                    userUpdateDTO.getResidentialAddress());
            } else {
                log.info("Creating new residential address for user");
                AddressDTO residentialAddress = addressService.createAddress(userUpdateDTO.getResidentialAddress());
                user.setResidentialAddressIdentifier(residentialAddress.getId());
            }
        }
        
        // Update or create postal address
        if (userUpdateDTO.getPostalAddress() != null) {
            if (user.getPostalAddressIdentifier() != null) {
                log.info("Updating existing postal address: {}", user.getPostalAddressIdentifier());
                addressService.updateAddress(user.getPostalAddressIdentifier(), 
                    userUpdateDTO.getPostalAddress());
            } else {
                log.info("Creating new postal address for user");
                AddressDTO postalAddress = addressService.createAddress(userUpdateDTO.getPostalAddress());
                user.setPostalAddressIdentifier(postalAddress.getId());
            }
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User {} updated successfully", id);
        
        return toUserDTOWithAddresses(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }
    
    /**
     * Convert User entity to UserDTO with populated address objects.
     */
    private UserDTO toUserDTOWithAddresses(User user) {
        UserDTO userDTO = userMapper.toUserDTO(user);
        
        // Populate residential address if exists
        if (user.getResidentialAddressIdentifier() != null) {
            addressRepository.findById(user.getResidentialAddressIdentifier())
                    .ifPresent(address -> userDTO.setResidentialAddress(addressMapper.toAddressDTO(address)));
        }
        
        // Populate postal address if exists
        if (user.getPostalAddressIdentifier() != null) {
            addressRepository.findById(user.getPostalAddressIdentifier())
                    .ifPresent(address -> userDTO.setPostalAddress(addressMapper.toAddressDTO(address)));
        }
        
        return userDTO;
    }
}
