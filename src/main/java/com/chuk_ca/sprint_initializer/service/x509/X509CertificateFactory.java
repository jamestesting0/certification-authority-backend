package com.chuk_ca.sprint_initializer.service.x509;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.OperatorCreationException;

import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public interface X509CertificateFactory {
    X509Certificate generateCertificate(PublicKey publicKey, X500Name subject, Date notBefore, Date notAfter) throws CertificateException, OperatorCreationException;

    boolean validateCertificate(java.security.cert.X509Certificate certificate);
}
