package com.warmhouse.devicecontrol.repository;

import com.warmhouse.devicecontrol.model.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommandRepository extends JpaRepository<Command, UUID> {

    List<Command> findByDeviceIdOrderByCreatedAtDesc(Integer deviceId);
}
