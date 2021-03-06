package com.beerjournal.breweriana.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @PostMapping
    ResponseEntity<UserDto> createUser(@RequestBody @Validated UserDto user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @GetMapping
    ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "firstname", defaultValue = "", required = false) String firstName,
            @RequestParam(value = "lastname", defaultValue = "", required = false) String lastName,
            @RequestParam(value = "sortBy", defaultValue = "jointime") String sortBy,
            @RequestParam(value = "sortType", defaultValue = "desc") String sortType) {
        return new ResponseEntity<>(userService.findUsers(firstName, lastName, page, count, sortBy, sortType), HttpStatus.OK);
    }

    @GetMapping("{userId}")
    ResponseEntity<UserDto> getUserWithId(@PathVariable(value = "userId") String userId) {
        return new ResponseEntity<>(userService.getUserWithId(userId), HttpStatus.OK);
    }

}
