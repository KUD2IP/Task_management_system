package org.example.authenticationservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserResponseDto", description = "User response dto")
public class UserResponse {

    @Schema(description = "Email", example = "q7vQs@example.com")
    private String email;

    @Schema(description = "Name", example = "John Doe")
    private String name;

    @Schema(description = "Roles", example = "ROLE_USER")
    private String role;
}
