package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.EmailRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateCreateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.model.X509CertificateRequest;
import jakarta.mail.MessagingException;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface X509CertificateService {
    List<X509CertificateEntry> getAllCertificateEntries();

    X509CertificateEntry getCertificateEntryById(Long certificateId);

    void deleteCertificateEntryById(Long certificateId);

    X509CertificateEntry saveCertificateEntry(X509CertificateEntry certificateEntry);

    String createCertificateAsPEM(X509CertificateCreateRequest certificateCreateRequest) throws InvalidKeySpecException, CertificateException, OperatorCreationException, IOException;

    void validateCertificate(X509CertificateRequest certificateRequest) throws CertificateException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException;

    KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException;

    void sendPrivateKey(EmailRequest emailRequest, PrivateKey privateKey) throws IOException, MessagingException;
}
