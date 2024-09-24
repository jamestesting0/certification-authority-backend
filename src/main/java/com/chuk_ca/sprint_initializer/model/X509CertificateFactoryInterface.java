package com.chuk_ca.sprint_initializer.model;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;

public interface X509CertificateFactoryInterface {
    X509Certificate generateCertificate(KeyPair keyPair, String dn, Date notBefore, Date notAfter);

    boolean validateCertificate(java.security.cert.X509Certificate certificate);
}
