package ru.kalinin.deal.repositories;

import ru.kalinin.deal.models.Client;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
