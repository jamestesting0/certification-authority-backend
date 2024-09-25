package com.chuk_ca.sprint_initializer.model;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

public interface X509CertificateFactoryInterface {
    X509Certificate generateCertificate(KeyPair keyPair, String dn, Date notBefore, Date notAfter);

    boolean validateCertificate(java.security.cert.X509Certificate certificate);
}
