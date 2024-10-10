package com.chuk_ca.sprint_initializer.dao;

import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.repository.X509CertificateEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class X509CertificateEntryDAO implements DAO<X509CertificateEntry> {
    private final X509CertificateEntryRepository x509CertificateEntryRepository;

    @Autowired
    public X509CertificateEntryDAO(X509CertificateEntryRepository x509CertificateEntryRepository) {
        this.x509CertificateEntryRepository = x509CertificateEntryRepository;
    }

    @Override
    public List<X509CertificateEntry> getAll() {
        return x509CertificateEntryRepository.findAll();
    }

    @Override
    @Cacheable(value = "x509CertificateCache", key = "#id")
    public X509CertificateEntry getByID(Long id) {
        Optional<X509CertificateEntry> certificateEntry = x509CertificateEntryRepository.findById(id);
        return certificateEntry.orElse(null);
    }

    @Override
    @CacheEvict(value = "x509CertificateCache", key = "#id")
    public void deleteByID(Long id) {
        x509CertificateEntryRepository.deleteById(id);
    }

    @Override
    @CachePut(value = "x509CertificateCache", key = "#certificateEntry.id")
    public X509CertificateEntry save(X509CertificateEntry certificateEntry) {
        return x509CertificateEntryRepository.save(certificateEntry);
    }
}
