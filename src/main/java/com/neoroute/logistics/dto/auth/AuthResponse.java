package com.neoroute.logistics.dto.auth;

import com.neoroute.logistics.domain.user.Role;
import com.neoroute.logistics.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response with JWT token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;

    @Builder.Default
    private String tokenType = Constants.TOKEN_TYPE;

    private String username;
    private Role role;
    private Long userId;
}
