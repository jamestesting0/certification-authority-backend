package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.X509Certificate;
import com.chuk_ca.sprint_initializer.model.X509CertificateFactoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.cert.CertificateException;
import java.util.Date;

@Service
public class X509CertificateFactory implements X509CertificateFactoryInterface {
    private static final String
            SIGNATURE_ALGORITHM = "SHA256withRSA",
            CA_COMMON_NAME = "ca.common_name",
            CA_ORGANIZATION = "ca.organization",
            CA_ORGANIZATION_UNIT = "ca.organizational_unit",
            CA_LOCALITY = "ca.locality",
            CA_STATE = "ca.state",
            CA_COUNTRY = "ca.country",
            CA_CERT_PATH = "ca.cert.path",
            CA_KEY_PATH = "ca.key.path";

    private ConfigFileLoader configLoader;
    private java.security.cert.X509Certificate caCertificate;
    private PrivateKey caPrivateKey;

    @Autowired
    public X509CertificateFactory(ConfigFileLoader configLoader) {
        this.configLoader = configLoader;
        this.caCertificate = readCACert();
        this.caPrivateKey = readCAPrivateKey();
    }

    private PrivateKey readCAPrivateKey() {
        return null;
    }

    private java.security.cert.X509Certificate readCACert() {
        return null;
    }

    @Override
    public X509Certificate generateCertificate(KeyPair keyPair, String dn, Date notBefore, Date notAfter) {
        return null;
    }

    @Override
    public boolean validateCertificate(java.security.cert.X509Certificate certificate) {
        try {
            PublicKey caPublicKey = caCertificate.getPublicKey();
            certificate.verify(caPublicKey);
            return true;
        } catch (CertificateException | NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
            return false;
        }
    }

    public void setConfigLoader(ConfigFileLoader configLoader) {
        this.configLoader = configLoader;
    }

    public void setCACertificate(java.security.cert.X509Certificate caCertificate) {
        this.caCertificate = caCertificate;
    }

    public void setCaPrivateKey(PrivateKey caPrivateKey) {
        this.caPrivateKey = caPrivateKey;
    }
}
