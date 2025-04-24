package org.example.authenticationservice.service.contract;

import org.example.authenticationservice.model.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends UserDetailsService {
    UserResponse getUserById(Long id);
}
