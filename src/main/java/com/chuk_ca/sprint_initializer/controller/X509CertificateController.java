package com.chuk_ca.sprint_initializer.controller;

import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.service.X509CertificateEntryManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
public class X509CertificateController {
    private final X509CertificateEntryManager x509CertificateEntryManager;

    public X509CertificateController(X509CertificateEntryManager x509CertificateEntryManager) {
        this.x509CertificateEntryManager = x509CertificateEntryManager;
    }

    @GetMapping("certificate")
    public ResponseEntity<List<X509CertificateEntry>> getAllCertificates() {
        return ResponseEntity.ok(x509CertificateEntryManager.getAll());
    }

    @GetMapping("certificate/{certificateId}")
    public ResponseEntity<X509CertificateEntry> getCertificate(@PathVariable("certificateId") Long certificateId) {
        X509CertificateEntry certificate = x509CertificateEntryManager.getByID(certificateId);
        if (certificate == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return ResponseEntity.ok(certificate);
    }
}
