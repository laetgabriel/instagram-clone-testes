package br.edu.ifpb.instagram.model.response;

public record UserDetailsResponse(
    Long id,
    String fullName,
    String username,
    String email) { }
