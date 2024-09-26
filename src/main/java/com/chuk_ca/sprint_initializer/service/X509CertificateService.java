package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.X509CertificateCreateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.model.X509CertificateRequest;

import java.util.List;

public interface X509CertificateService {
    List<X509CertificateEntry> getAllCertificateEntries();
    X509CertificateEntry getCertificateEntryById(Long certificateId);
    void deleteCertificateEntryById(Long certificateId);
    X509CertificateEntry saveCertificateEntry(X509CertificateEntry certificateEntry);
    String createCertificateAsPEM(X509CertificateCreateRequest certificateCreateRequest);
    void validateCertificate(X509CertificateRequest certificateRequest);
}
