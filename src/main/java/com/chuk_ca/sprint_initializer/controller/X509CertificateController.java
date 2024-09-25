package com.chuk_ca.sprint_initializer.controller;

import com.chuk_ca.sprint_initializer.model.CertificateRequest;
import com.chuk_ca.sprint_initializer.model.CertificateValidateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.service.X509CertificateEntryManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.cert.X509Certificate;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class X509CertificateController {
    private final X509CertificateEntryManager x509CertificateEntryManager;

    public X509CertificateController(X509CertificateEntryManager x509CertificateEntryManager) {
        this.x509CertificateEntryManager = x509CertificateEntryManager;
    }

    @GetMapping("certificates")
    public ResponseEntity<List<X509CertificateEntry>> getAllCertificateEntries() {
        List<X509CertificateEntry> certificateEntries = x509CertificateEntryManager.getAll();
        if (certificateEntries.isEmpty()) {
            throw new ResponseStatusException(NO_CONTENT);
        }
        return ResponseEntity.ok(certificateEntries);
    }

    @GetMapping("certificates/{certificateId}")
    public ResponseEntity<X509CertificateEntry> getCertificateEntry(@PathVariable("certificateId") Long certificateId) {
        X509CertificateEntry certificateEntry = x509CertificateEntryManager.getByID(certificateId);
        if (certificateEntry == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return ResponseEntity.ok(certificateEntry);
    }

    @DeleteMapping("certificates/{certificateId}")
    public ResponseEntity<X509CertificateEntry> deleteCertificateEntry(@PathVariable("certificateId") Long certificateId) {
        return null;
    }

    @PostMapping("certificates")
    public ResponseEntity<X509Certificate> createCertificate(@RequestBody CertificateRequest certificateRequest) {
        return null;
    }

    @PostMapping("certificates/validate")
    public ResponseEntity<String> validateCertificate(@RequestBody CertificateValidateRequest certificateRequest) {
        return null;
    }
}
