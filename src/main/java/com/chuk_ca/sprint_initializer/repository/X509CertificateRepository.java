package com.chuk_ca.sprint_initializer.repository;

import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface X509CertificateRepository extends JpaRepository<X509CertificateEntry, Long> {
    @NotNull Optional<X509CertificateEntry> findById(@NotNull Long id);
}
