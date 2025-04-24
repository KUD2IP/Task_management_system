package org.example.authenticationservice.repository;

import org.example.authenticationservice.model.entity.User;
import org.example.authenticationservice.model.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    @Query("""
        SELECT c
        FROM VerificationCode c
            inner join User u
            on c.user.id = u.id
            where c.user.id = :userId and c.valid = true
        """)
    List<VerificationCode> findAllValid(Long userId);

    Optional<VerificationCode> findByUserAndCode(User user, String code);
}