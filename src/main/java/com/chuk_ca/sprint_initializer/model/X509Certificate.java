package com.chuk_ca.sprint_initializer.model;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

@Data
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class X509Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String certificate;
}
