package by.rublevskaya.authservice.repository;

import by.rublevskaya.authservice.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByLogin(String login);
    Optional<UserCredential> findByUserId(Long userId);
    boolean existsByLogin(String login);
    boolean existsByUserId(Long userId);
}