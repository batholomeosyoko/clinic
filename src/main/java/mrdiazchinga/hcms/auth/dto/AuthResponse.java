package mrdiazchinga.hcms.auth.dto;

import mrdiazchinga.hcms.user.dto.UserResponseDto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponseDto user
) {
}
