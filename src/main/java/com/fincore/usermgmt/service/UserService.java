package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.mapper.UserMapper;
import com.fincore.usermgmt.repository.RoleRepository;
import com.fincore.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toUserDTO);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        User user = userMapper.toUser(userCreateDTO);
        if (userCreateDTO.getRole() != null) {
            Role role = roleRepository.findByName(userCreateDTO.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }
        return userMapper.toUserDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.updateUserFromDto(userUpdateDTO, user);
        if (userUpdateDTO.getRole() != null) {
            Role role = roleRepository.findByName(userUpdateDTO.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }
        return userMapper.toUserDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
