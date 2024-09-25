package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.repository.X509CertificateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
public class X509CertificateEntryManager implements X509CertificateEntryManagerInterface {
    private final X509CertificateRepository x509CertificateRepository;

    public X509CertificateEntryManager(X509CertificateRepository x509CertificateRepository) {
        this.x509CertificateRepository = x509CertificateRepository;
    }

    @Override
    public List<X509CertificateEntry> getAll() {
        return x509CertificateRepository.findAll();
    }

    @Override
    @Cacheable(value = "x509CertificateCache", key = "#id")
    public X509CertificateEntry getByID(Long id) {
        try {
            return x509CertificateRepository.getReferenceById(id);
        } catch (EntityNotFoundException entityNotFoundException) {
            return null;
        }
    }

    @Override
    @CacheEvict(value = "x509CertificateCache", key = "#id")
    public void deleteByID(Long id) {
        x509CertificateRepository.deleteById(id);
    }

    @Override
    @CachePut(value = "x509CertificateCache", key = "#certificateEntry.id")
    public X509CertificateEntry save(X509CertificateEntry certificateEntry) {
        return x509CertificateRepository.save(certificateEntry);
    }
}
