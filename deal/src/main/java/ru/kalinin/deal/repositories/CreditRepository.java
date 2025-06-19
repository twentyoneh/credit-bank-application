package ru.kalinin.deal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Credit;

import java.util.UUID;

@Repository
public interface CreditRepository  extends JpaRepository<Credit, UUID> {
}
