package ru.kalinin.deal.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class PaymentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_schedule_id", nullable = false)
    private UUID id;

}
