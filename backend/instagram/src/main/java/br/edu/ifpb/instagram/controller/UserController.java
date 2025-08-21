package br.edu.ifpb.instagram.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.model.response.UserDetailsResponse;
import br.edu.ifpb.instagram.service.UserService;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDetailsResponse> getUsers(){

        List<UserDto> userDtos = userService.findAll();
        List<UserDetailsResponse> userDetailsResponses = new ArrayList<>();

        for (UserDto userDto : userDtos) {
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(
                userDto.id(),
                userDto.fullName(),
                userDto.username(),
                userDto.email()
            );
            userDetailsResponses.add(userDetailsResponse);
        }

        return userDetailsResponses;
    }

    @GetMapping("/{id}")
    public UserDetailsResponse getUser(@PathVariable Long id){

        UserDto userDto = userService.findById(id);
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse(
            userDto.id(),
            userDto.fullName(),
            userDto.username(),
            userDto.email()
        );
        BeanUtils.copyProperties(userDto, userDetailsResponse);

        return userDetailsResponse;
    }

    @PutMapping
    public UserDetailsResponse updateUser(@RequestBody UserDetailsRequest userDetailsRequest){

        UserDto userDto = new UserDto(
            userDetailsRequest.id(),
            userDetailsRequest.fullName(),
            userDetailsRequest.username(),
            userDetailsRequest.email(),
            userDetailsRequest.password(),
            null
        );

        UserDto updatedUserDto = userService.updateUser(userDto);

        return new UserDetailsResponse(
            updatedUserDto.id(),
            updatedUserDto.fullName(),
            updatedUserDto.username(),
            updatedUserDto.email()
        );
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id){

        userService.deleteUser(id);

        return "user was deleted!";
    }

}
