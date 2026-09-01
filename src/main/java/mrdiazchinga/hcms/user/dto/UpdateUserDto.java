package mrdiazchinga.hcms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import mrdiazchinga.hcms.user.entity.Role;

public record UpdateUserDto(
        @Email @Size(min = 3, max = 254) String email,
        @Size(min = 8, max = 72) String password,
        Role role,
        Boolean enabled
) {
}
