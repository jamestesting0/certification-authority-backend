package com.chuk_ca.sprint_initializer.controller;

import com.chuk_ca.sprint_initializer.model.X509Certificate;
import com.chuk_ca.sprint_initializer.service.X509CertificateService;
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
    private final X509CertificateService x509CertificateService;

    public X509CertificateController(X509CertificateService x509CertificateService) {
        this.x509CertificateService = x509CertificateService;
    }

    @GetMapping("certificate")
    public ResponseEntity<List<X509Certificate>> getAllCertificates() {
        return ResponseEntity.ok(x509CertificateService.getAll());
    }

    @GetMapping("certificate/{certificateId}")
    public ResponseEntity<X509Certificate> getCertificate(@PathVariable("certificateId") Long certificateId) {
        X509Certificate certificate = x509CertificateService.getByID(certificateId);
        if (certificate == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return ResponseEntity.ok(certificate);
    }
}
