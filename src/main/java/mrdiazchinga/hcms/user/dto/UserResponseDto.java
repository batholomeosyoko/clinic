package mrdiazchinga.hcms.user.dto;

import java.time.Instant;

import mrdiazchinga.hcms.user.entity.Role;
import mrdiazchinga.hcms.user.entity.User;

public record UserResponseDto(
        Long id,
        String email,
        Role role,
        boolean enabled,
        Instant createdAt
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
