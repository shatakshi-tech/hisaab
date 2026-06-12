package com.hisaab.hisaab.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "balances")
@Data
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "owed_by", nullable = false)
    private User owedBy;  // this user owes money

    @ManyToOne
    @JoinColumn(name = "owed_to", nullable = false)
    private User owedTo;  // to this user

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Version
    private Long version;
}