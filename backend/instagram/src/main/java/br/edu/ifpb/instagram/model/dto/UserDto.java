package br.edu.ifpb.instagram.model.dto;

public record UserDto(
    Long id,
    String fullName,
    String username,
    String email,
    String password,
    String encryptedPassword) { }
