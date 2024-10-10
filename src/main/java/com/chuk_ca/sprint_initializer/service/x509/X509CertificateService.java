package com.chuk_ca.sprint_initializer.service.x509;

import com.chuk_ca.sprint_initializer.model.EmailRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateData;
import com.chuk_ca.sprint_initializer.model.X509CertificateViewRequest;
import jakarta.mail.MessagingException;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public interface X509CertificateService {
    String createCertificateAsPEM(X509CertificateData certificateData) throws InvalidKeySpecException, CertificateException, OperatorCreationException, IOException;

    void validateCertificate(X509CertificateViewRequest certificateRequest) throws CertificateException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException;

    KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException;

    void sendPrivateKey(EmailRequest emailRequest, PrivateKey privateKey) throws IOException, MessagingException;

    String getCAPublicKey() throws IOException;
}
