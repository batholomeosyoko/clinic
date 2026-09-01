package mrdiazchinga.hcms.auth;

import mrdiazchinga.hcms.auth.dto.AuthResponse;
import mrdiazchinga.hcms.auth.dto.LoginRequest;
import mrdiazchinga.hcms.auth.dto.RegisterRequest;
import mrdiazchinga.hcms.user.UserRepository;
import mrdiazchinga.hcms.user.UserService;
import mrdiazchinga.hcms.user.dto.CreateUserDto;
import mrdiazchinga.hcms.user.dto.UserResponseDto;
import mrdiazchinga.hcms.user.entity.Role;
import mrdiazchinga.hcms.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        User user = userService.createUser(new CreateUserDto(
                registerRequest.email(),
                registerRequest.password(),
                Role.PATIENT));

        return createAuthResponse(user);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .filter(User::isEnabled)
                .filter(existingUser -> passwordEncoder.matches(loginRequest.password(), existingUser.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                BEARER_TOKEN_TYPE,
                jwtService.getExpirationSeconds(),
                UserResponseDto.from(user));
    }
}
