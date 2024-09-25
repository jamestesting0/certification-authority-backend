package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;

import java.util.List;

public interface X509CertificateEntryManagerInterface {
    List<X509CertificateEntry> getAll();

    X509CertificateEntry getByID(Long id);

    X509CertificateEntry deleteByID(Long id);

    X509CertificateEntry save(X509CertificateEntry certificateEntry);
}
