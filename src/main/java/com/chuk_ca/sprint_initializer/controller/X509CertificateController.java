package com.chuk_ca.sprint_initializer.controller;

import com.chuk_ca.sprint_initializer.model.*;
import com.chuk_ca.sprint_initializer.service.util.CSRUtils;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateService;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import jakarta.mail.MessagingException;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

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
        X509CertificateData certificateData = new X509CertificateData();
        try {
            Map<String, String> certificateAttrs = CSRUtils.parseCSR(certificateCreateRequest.getCsr());
            certificateData.setPublicKey(certificateAttrs.get("Public Key"));
            certificateData.setCommonName(certificateAttrs.get("Common Name (CN)"));
            certificateData.setOrganization(certificateAttrs.get("Organization (O)"));
            certificateData.setOrganizationUnit(certificateAttrs.get("Organizational Unit (OU)"));
            certificateData.setCountry(certificateAttrs.get("Country (C)"));
            certificateData.setState(certificateAttrs.get("State/Province (ST)"));
            certificateData.setLocality(certificateAttrs.get("Locality (L)"));
            certificateData.setNotBefore(certificateCreateRequest.getNotBefore());
            certificateData.setNotAfter(certificateCreateRequest.getNotAfter());
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Something went wrong when parsing the CSR.");
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The key algorithm should be RSA.");
        } catch (InvalidKeySpecException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The public key provided in the CSR is invalid.");
        }

        try {
            return ResponseEntity.ok(certificateService.createCertificateAsPEM(certificateData));
        } catch (CertificateException | OperatorCreationException e) {
            throw new ResponseStatusException(BAD_REQUEST);
        } catch (InvalidKeySpecException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The public key from the request body is invalid.");
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Failed to write the certificate as a PEM file.");
        }
    }

    @PostMapping("certificates/validate")
    public ResponseEntity<String> validateCertificate(@RequestBody X509CertificateViewRequest certificateViewRequest) {
        try {
            certificateService.validateCertificate(certificateViewRequest);
            return ResponseEntity.ok("The certificate is valid.");
        } catch (CertificateExpiredException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate has expired.");
        } catch (CertificateNotYetValidException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is not yet valid.");
        } catch (CertificateException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is encoded incorrectly.");
        } catch (NoSuchProviderException | InvalidKeyException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Internal server error.");
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate's signature algorithm is invalid.");
        } catch (SignatureException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate's content does not match its signature.");
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is invalid.");
        }
    }

    @PostMapping("certificates/view-json")
    public ResponseEntity<X509CertificateViewResponse> viewCertificate(@RequestBody X509CertificateViewRequest certificateViewRequest) {
        try {
            X509Certificate certificate = X509CertificateUtils.readCertificateFromPEM(certificateViewRequest.getPemContent());
            X509CertificateViewResponse response = X509CertificateUtils.convertCertificateToJSON(certificate);
            return ResponseEntity.ok(response);
        } catch (IOException | CertificateException e) {
            throw new ResponseStatusException(BAD_REQUEST, "The certificate is invalid.");
        }
    }

    @PostMapping("certificates/generate-keys")
    public ResponseEntity<PublicKeyResponse> generateKeys(@RequestBody EmailRequest emailRequest) {
        try {
            KeyPair keyPair = certificateService.generateKeyPair();
            PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
            publicKeyResponse.setPemContent(X509CertificateUtils.convertPublicKeyToPEM(keyPair.getPublic()));
            certificateService.sendPrivateKey(emailRequest, keyPair.getPrivate());
            return ResponseEntity.ok(publicKeyResponse);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Internal server error.");
        } catch (MessagingException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to send the private key via Email.");
        }
    }

    @GetMapping("certificates/ca-public-key")
    public ResponseEntity<String> getCAPublicKey() {
        try {
            return ResponseEntity.ok(certificateService.getCAPublicKey());
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Failed to get the CA's certificate.");
        }
    }
}
