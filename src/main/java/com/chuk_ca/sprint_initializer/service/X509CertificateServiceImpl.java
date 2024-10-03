package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.EmailRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateCreateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.model.X509CertificateRequest;
import com.chuk_ca.sprint_initializer.service.dao.DAO;
import com.chuk_ca.sprint_initializer.service.util.EmailSender;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateFactory;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import jakarta.mail.MessagingException;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@Service
public class X509CertificateServiceImpl implements X509CertificateService {
    private final X509CertificateFactory certificateFactory;
    private final DAO<X509CertificateEntry> certificateEntryDAO;
    private final EmailSender emailSender;

    @Autowired
    public X509CertificateServiceImpl(X509CertificateFactory certificateFactory, DAO<X509CertificateEntry> certificateEntryDAO, EmailSender emailSender) {
        this.certificateFactory = certificateFactory;
        this.certificateEntryDAO = certificateEntryDAO;
        this.emailSender = emailSender;
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

    public String createCertificateAsPEM(X509CertificateCreateRequest certificateCreateRequest) throws InvalidKeySpecException, CertificateException, OperatorCreationException, IOException {
        // Process the public key as base64 string
        PublicKey publicKey;
        try (PEMParser pemParser = new PEMParser(new StringReader(certificateCreateRequest.getPublicKey()))) {
            Object object = pemParser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (object instanceof SubjectPublicKeyInfo subjectPublicKeyInfo) {
                publicKey = converter.getPublicKey(subjectPublicKeyInfo);
            } else {
                throw new InvalidKeySpecException("The PEM content does not contain a valid public key");
            }
        }

        // Create the subject name
        X500Name subject = X509CertificateUtils.createX500Name(
                certificateCreateRequest.getCommonName(),
                certificateCreateRequest.getOrganization(),
                certificateCreateRequest.getOrganizationUnit(),
                certificateCreateRequest.getLocality(),
                certificateCreateRequest.getState(),
                certificateCreateRequest.getCountry()
        );

        // Create the X509 certificate
        X509Certificate certificate = certificateFactory.generateCertificate(
                publicKey,
                subject,
                certificateCreateRequest.getNotBefore(),
                certificateCreateRequest.getNotAfter()
        );

        return X509CertificateUtils.convertCertificateToPEM(certificate);
    }

    public void validateCertificate(X509CertificateRequest certificateRequest) throws CertificateException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        X509Certificate certificate = X509CertificateUtils.readCertificateFromPEM(certificateRequest.getPemContent());
        certificateFactory.validateCertificate(certificate);
    }

    @Override
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        return certificateFactory.generateKeyPair();
    }

    @Override
    public void sendPrivateKey(EmailRequest emailRequest, PrivateKey privateKey) throws IOException, MessagingException {
        String pemContent = X509CertificateUtils.convertPrivateKeyToPEM(privateKey);

        emailSender.sendEmail(
                emailRequest.getAddress(),
                "Your Private Key",
                "This is your private key for your certificate issued by the CUHK Certificate Authority, " +
                        "please keep it safe and not share with anyone else.",
                X509CertificateUtils.stringToInputStreamSource(pemContent)
        );
    }

    @Override
    public String getCAPublicKey() throws IOException {
        return X509CertificateUtils.convertPublicKeyToPEM(certificateFactory.getCACertificate().getPublicKey());
    }
}
