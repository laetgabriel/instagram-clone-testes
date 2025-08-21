package br.edu.ifpb.instagram.model.request;

public record UserDetailsRequest(
    Long id,
    String email,
    String password,
    String fullName,
    String username) { }
