package com.warmhouse.devicecontrol.repository;

import com.warmhouse.devicecontrol.model.CommandResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommandResultRepository extends JpaRepository<CommandResult, UUID> {

    Optional<CommandResult> findByCommandId(UUID commandId);
}
