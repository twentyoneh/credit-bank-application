package ru.kalinin.deal.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class AppliedOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "applied_offer_id", nullable = false)
    private UUID id;
}
