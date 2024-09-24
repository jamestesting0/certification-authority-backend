package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.X509Certificate;
import com.chuk_ca.sprint_initializer.repository.X509CertificateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
public class X509CertificateService {
    private final X509CertificateRepository x509CertificateRepository;

    public X509CertificateService(X509CertificateRepository x509CertificateRepository) {
        this.x509CertificateRepository = x509CertificateRepository;
    }

    public List<X509Certificate> getAll() {
        return x509CertificateRepository.findAll();
    }

    @Cacheable(value = "x509CertificateCache", key = "#id")
    public X509Certificate getByID(Long id) {
        try {
            return x509CertificateRepository.getReferenceById(id);
        } catch (EntityNotFoundException entityNotFoundException) {
            return null;
        }
    }
}
