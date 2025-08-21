package br.edu.ifpb.instagram.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.ifpb.instagram.model.dto.UserDto;

@Service
public interface UserService {

    UserDto createUser(UserDto user);
    UserDto updateUser(UserDto user);
    void deleteUser(Long id);
    List<UserDto> findAll();
    UserDto findById(Long id);
}
