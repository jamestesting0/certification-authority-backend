package com.chuk_ca.sprint_initializer.service;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.StringWriter;

public class X509CertificateFactoryImpl implements X509CertificateFactory {
    public static String toPEM(java.security.cert.X509Certificate certificate) throws IOException {
        try (
                StringWriter stringWriter = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)
        ) {
            pemWriter.writeObject(certificate);
            pemWriter.flush();
            return stringWriter.toString();
        }
    }
}
