package com.chuk_ca.sprint_initializer.controller;

import com.chuk_ca.sprint_initializer.model.X509CertificateCreateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.model.X509CertificateResponse;
import com.chuk_ca.sprint_initializer.service.X509CertificateService;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class X509CertificateController {
    private final X509CertificateService certificateService;

    @Autowired
    public X509CertificateController(X509CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("certificates")
    public ResponseEntity<List<X509CertificateEntry>> getAllCertificateEntries() {
        List<X509CertificateEntry> certificateEntries = certificateService.getAllCertificateEntries();
        if (certificateEntries.isEmpty()) {
            throw new ResponseStatusException(NO_CONTENT, "There are no certificate entries yet.");
        }
        return ResponseEntity.ok(certificateEntries);
    }

    @GetMapping("certificates/{certificateId}")
    public ResponseEntity<X509CertificateEntry> getCertificateEntry(@PathVariable("certificateId") Long certificateId) {
        X509CertificateEntry certificateEntry = certificateService.getCertificateEntryById(certificateId);
        if (certificateEntry == null) {
            throw new ResponseStatusException(NOT_FOUND, "This certificate entry does not exist.");
        }
        return ResponseEntity.ok(certificateEntry);
    }

    @DeleteMapping("certificates/{certificateId}")
    public ResponseEntity<Void> deleteCertificateEntry(@PathVariable("certificateId") Long certificateId) {
        certificateService.deleteCertificateEntryById(certificateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("certificates")
    public ResponseEntity<X509CertificateEntry> saveCertificateEntry(@RequestBody X509CertificateEntry certificateEntry) {
        return ResponseEntity.ok(certificateService.saveCertificateEntry(certificateEntry));
    }

    @PostMapping("certificates/create")
    public ResponseEntity<String> createCertificate(@RequestBody X509CertificateCreateRequest certificateCreateRequest) {
        try {
            return ResponseEntity.ok(certificateService.createCertificateAsPEM(certificateCreateRequest));
        } catch (CertificateException | OperatorCreationException e) {
            throw new ResponseStatusException(BAD_REQUEST);
        } catch (InvalidKeySpecException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The public key from the request body is invalid.");
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Failed to write the certificate as a PEM file.");
        }
    }

    @PostMapping("certificates/validate")
    public ResponseEntity<String> validateCertificate(@RequestBody X509CertificateRequest certificateRequest) {
        try {
            certificateService.validateCertificate(certificateRequest);
            return ResponseEntity.ok("The certificate is valid.");
        } catch (CertificateExpiredException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate has expired.");
        } catch (CertificateNotYetValidException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is not yet valid.");
        } catch (CertificateException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is encoded incorrectly.");
        }  catch (NoSuchProviderException | InvalidKeyException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Internal server error.");
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate's signature algorithm is invalid.");
        } catch (SignatureException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate's content does not match its signature.");
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is invalid.");
        }
    }

    @PostMapping("certificates/view-json")
    public ResponseEntity<X509CertificateResponse> viewCertificate(@RequestBody X509CertificateRequest certificateRequest) {
        try {
            X509Certificate certificate = X509CertificateUtils.readCertificateFromPEM(certificateRequest.getPemContent());
            X509CertificateResponse response = X509CertificateUtils.convertCertificateToJSON(certificate);
            return ResponseEntity.ok(response);
        } catch (IOException | CertificateException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is invalid.");
        }
    }
}
