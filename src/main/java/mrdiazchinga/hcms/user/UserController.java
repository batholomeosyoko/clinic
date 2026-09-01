package mrdiazchinga.hcms.user;

import jakarta.validation.Valid;
import mrdiazchinga.hcms.user.dto.CreateUserDto;
import mrdiazchinga.hcms.user.dto.UpdateUserDto;
import mrdiazchinga.hcms.user.dto.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final String DEFAULT_PAGE_NUMBER = "0";
    private static final String DEFAULT_PAGE_SIZE = "20";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        return UserResponseDto.from(userService.createUser(createUserDto));
    }

    @GetMapping
    public Page<UserResponseDto> getUsers(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return userService.findUsers(page, size).map(UserResponseDto::from);
    }

    @GetMapping("/{id}")
    public UserResponseDto getUser(@PathVariable Long id) {
        return UserResponseDto.from(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        return UserResponseDto.from(userService.updateUser(id, updateUserDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
