package ru.kalinin.deal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kalinin.deal.models.Client;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
