package org.example.authenticationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.authenticationservice.model.dto.response.UserResponse;
import org.example.authenticationservice.model.entity.Role;
import org.example.authenticationservice.model.entity.User;
import org.example.authenticationservice.repository.UserRepository;
import org.example.authenticationservice.service.contract.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Метод для получения пользователя по его email.
     * @param email - email пользователя
     * @return пользователь
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
    }


    /**
     * Метод для получения пользователя по его id.
     * @param id - id пользователя
     * @return пользователь
     */
    @Override
    public UserResponse getUserById(Long id) {
        //Получение пользователя по id
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Формирование ответа
        UserResponse userResponse = new UserResponse();

        //Заполнение полей ответа
        userResponse.setEmail(user.getEmail());
        userResponse.setName(user.getName());
        userResponse.setRole(user.getRoles().stream().map(Role::getName).toList().toString());
        log.info(userResponse.toString());
        return userResponse;
    }
}
