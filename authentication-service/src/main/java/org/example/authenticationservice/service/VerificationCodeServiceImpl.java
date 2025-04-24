package org.example.authenticationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.authenticationservice.exception.CodeExpiredException;
import org.example.authenticationservice.exception.InvalidCodeException;
import org.example.authenticationservice.exception.UserNotFoundException;
import org.example.authenticationservice.kafka.KafkaProducer;
import org.example.authenticationservice.model.entity.User;
import org.example.authenticationservice.model.entity.VerificationCode;
import org.example.authenticationservice.repository.UserRepository;
import org.example.authenticationservice.repository.VerificationCodeRepository;
import org.example.authenticationservice.service.contract.VerificationCodeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    public VerificationCodeServiceImpl(VerificationCodeRepository verificationCodeRepository, UserRepository userRepository, KafkaProducer kafkaProducer) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }

    private void revokeAllValidCodes(Long userId) {
        List<VerificationCode> validCodes = verificationCodeRepository.findAllValid(userId);

        log.info("Valid tokens: {}", validCodes.toString());

        if(!validCodes.isEmpty()){
            validCodes.forEach(t ->{
                t.setValid(false);
            });
        }

        verificationCodeRepository.saveAll(validCodes);
    }

    @Override
    public void sendCode(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));

        revokeAllValidCodes(user.getId());

        String code = generateCode();

        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .valid(true)
                .expiryAt(LocalDateTime.now().plusMinutes(5))
                .build();

        verificationCodeRepository.save(verificationCode);

        kafkaProducer.send(user.getEmail(), code);

        log.info("Code sent {} on email {}", code, email);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @Override
    public void verify(String email, String code) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));

        VerificationCode verificationCode = verificationCodeRepository.findByUserAndCode(user, code)
                .orElseThrow(() -> new InvalidCodeException("Code is invalid"));

        if(verificationCode.getExpiryAt().isBefore(LocalDateTime.now())){
            verificationCode.setValid(false);
            throw new CodeExpiredException("Code expired");
        }

        user.setVerified(true);
        userRepository.save(user);

        verificationCode.setValid(false);
        verificationCodeRepository.save(verificationCode);

        log.info("Account with email {} is verified", user.getEmail());
    }

}