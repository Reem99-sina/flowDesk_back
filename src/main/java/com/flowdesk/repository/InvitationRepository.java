
package com.flowdesk.repository;

import com.flowdesk.entity.Invitation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invitation i WHERE i.tokenHash = :tokenHash")
    Optional<Invitation> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    Optional<Invitation> findByTokenHash(String tokenHash);

    List<Invitation> findByUserId(Long userId);
}
