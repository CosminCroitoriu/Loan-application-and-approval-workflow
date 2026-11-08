package com.loanworkflow.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.loanworkflow.approval.Role;
import com.loanworkflow.persistence.entity.UserEntity;
import com.loanworkflow.persistence.repository.UserRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void persistsUsersWithDifferentRoles() {
        UserEntity applicant = new UserEntity("carol", "hash123", Role.APPLICANT);
        UserEntity officer = new UserEntity("bob", "hash456", Role.OFFICER);
        UserEntity approver = new UserEntity("anna", "hash789", Role.APPROVER);

        userRepository.save(applicant);
        userRepository.save(officer);
        userRepository.save(approver);
        entityManager.flush();
        entityManager.clear();

        Optional<UserEntity> retrieved = userRepository.findByUsername("carol");
        assertTrue(retrieved.isPresent());
        assertEquals(Role.APPLICANT, retrieved.get().getRole());
        assertNotNull(retrieved.get().getCreatedAt());

        assertTrue(userRepository.existsByUsername("bob"));
        assertEquals(Role.APPROVER, userRepository.findByUsername("anna").orElseThrow().getRole());
    }

    @Test
    void rejectsDuplicateUsername() {
        UserEntity user1 = new UserEntity("unique_user", "hash1", Role.APPLICANT);
        userRepository.save(user1);
        entityManager.flush();

        UserEntity user2 = new UserEntity("unique_user", "hash2", Role.OFFICER);
        userRepository.save(user2);

        assertThrows(ConstraintViolationException.class, () -> entityManager.flush());
    }
}
