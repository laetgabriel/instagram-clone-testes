package br.edu.ifpb.instagram.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.LoginRequest;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.model.response.LoginResponse;
import br.edu.ifpb.instagram.model.response.UserDetailsResponse;
import br.edu.ifpb.instagram.service.UserService;
import br.edu.ifpb.instagram.service.impl.AuthServiceImpl;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthServiceImpl authService;
    private final UserService userService;

    public AuthController(AuthServiceImpl authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signIn(@RequestBody LoginRequest loginRequest) {
        String token = authService.authenticate(loginRequest);
        LoginResponse loginResponseModel = new LoginResponse(loginRequest.username(), token);

        return ResponseEntity.ok(loginResponseModel);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDetailsResponse> signUp(@RequestBody UserDetailsRequest userDetailsRequest){

        // object to move across several layers
        UserDto userDto = new UserDto(
            null,
            userDetailsRequest.fullName(),
            userDetailsRequest.username(),
            userDetailsRequest.email(),
            userDetailsRequest.password(),
            null
        );
        UserDto createdUserDto = userService.createUser(userDto);
        UserDetailsResponse response = new UserDetailsResponse(
            createdUserDto.id(),
            createdUserDto.fullName(),
            createdUserDto.username(),
            createdUserDto.email()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
