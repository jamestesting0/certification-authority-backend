package com.chuk_ca.sprint_initializer.controller;

import com.chuk_ca.sprint_initializer.model.CertificateCreateRequest;
import com.chuk_ca.sprint_initializer.model.CertificateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.service.X509CertificateService;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class X509CertificateController {
    private final X509CertificateService x509CertificateService;

    @Autowired
    public X509CertificateController(X509CertificateService x509CertificateService) {
        this.x509CertificateService = x509CertificateService;
    }

    @GetMapping("certificates")
    public ResponseEntity<List<X509CertificateEntry>> getAllCertificateEntries() {
        List<X509CertificateEntry> certificateEntries = x509CertificateService.getAllCertificateEntries();
        if (certificateEntries.isEmpty()) {
            throw new ResponseStatusException(NO_CONTENT, "There are no certificate entries yet.");
        }
        return ResponseEntity.ok(certificateEntries);
    }

    @GetMapping("certificates/{certificateId}")
    public ResponseEntity<X509CertificateEntry> getCertificateEntry(@PathVariable("certificateId") Long certificateId) {
        X509CertificateEntry certificateEntry = x509CertificateService.getCertificateEntryById(certificateId);
        if (certificateEntry == null) {
            throw new ResponseStatusException(NOT_FOUND, "This certificate entry does not exist.");
        }
        return ResponseEntity.ok(certificateEntry);
    }

    @DeleteMapping("certificates/{certificateId}")
    public ResponseEntity<Void> deleteCertificateEntry(@PathVariable("certificateId") Long certificateId) {
        x509CertificateService.deleteCertificateEntryById(certificateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("certificates")
    public ResponseEntity<X509CertificateEntry> createCertificate(@RequestBody X509CertificateEntry certificateEntry) {
        return ResponseEntity.ok(x509CertificateService.saveCertificateEntry(certificateEntry));
    }

    @PostMapping("certificates/create")
    public ResponseEntity<String> createCertificate(@RequestBody CertificateCreateRequest certificateCreateRequest) {
        try {
            return ResponseEntity.ok(x509CertificateService.createCertificateAsPEM(certificateCreateRequest));
        } catch (CertificateException | OperatorCreationException e) {
            throw new ResponseStatusException(BAD_REQUEST);
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(BAD_REQUEST, "An invalid algorithm is used for the certificate generation.");
        } catch (InvalidKeySpecException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The public key from the request body is invalid.");
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Failed to write the certificate as a PEM file.");
        }
    }

    @PostMapping("certificates/validate")
    public ResponseEntity<String> validateCertificate(@RequestBody CertificateRequest certificateRequest) {
        return null;
    }

    @PostMapping("certificates/view")
    public ResponseEntity<String> viewCertificate(@RequestBody CertificateRequest certificateRequest) {
        return null;
    }
}
