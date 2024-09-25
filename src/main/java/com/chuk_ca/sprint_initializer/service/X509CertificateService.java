package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.CertificateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.service.dao.DAO;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateFactory;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Service
public class X509CertificateService {
    private final X509CertificateFactory certificateFactory;

    private final DAO<X509CertificateEntry> certificateEntryDAO;

    @Autowired
    public X509CertificateService(X509CertificateFactory certificateFactory, DAO<X509CertificateEntry> certificateEntryDAO) {
        this.certificateFactory = certificateFactory;
        this.certificateEntryDAO = certificateEntryDAO;
    }

    public List<X509CertificateEntry> getAllCertificateEntries() {
        return certificateEntryDAO.getAll();
    }

    public X509CertificateEntry getCertificateEntryById(Long certificateId) {
        return certificateEntryDAO.getByID(certificateId);
    }

    public void deleteCertificateEntryById(Long certificateId) {
        certificateEntryDAO.deleteByID(certificateId);
    }

    public X509CertificateEntry saveCertificateEntry(X509CertificateEntry certificateEntry) {
        return certificateEntryDAO.save(certificateEntry);
    }

    public String createCertificateAsPEM(CertificateRequest certificateRequest) throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, OperatorCreationException, IOException {
        // Process the public key as base64 string
        byte[] decodedKey = Base64.getDecoder().decode(certificateRequest.getBase64Key().getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // Create the subject name
        X500Name subject = X509CertificateUtils.createX500Name(
                certificateRequest.getCommonName(),
                certificateRequest.getOrganization(),
                certificateRequest.getOrganizationUnit(),
                certificateRequest.getLocality(),
                certificateRequest.getState(),
                certificateRequest.getCountry()
        );

        // Create the X509 certificate
        X509Certificate certificate = certificateFactory.generateCertificate(
                publicKey,
                subject,
                certificateRequest.getNotBefore(),
                certificateRequest.getNotAfter()
        );

        return X509CertificateUtils.convertCertificateToPEM(certificate);
    }
}
