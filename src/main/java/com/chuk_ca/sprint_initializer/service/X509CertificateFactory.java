package com.chuk_ca.sprint_initializer.service;

import com.chuk_ca.sprint_initializer.model.X509CertificateFactoryInterface;
import com.chuk_ca.sprint_initializer.service.util.X509CertificateUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

@Service
public class X509CertificateFactory implements X509CertificateFactoryInterface {
    private static final String
            CA_COMMON_NAME = "ca.common_name",
            CA_ORGANIZATION = "ca.organization",
            CA_ORGANIZATIONAL_UNIT = "ca.organizational_unit",
            CA_LOCALITY = "ca.locality",
            CA_STATE = "ca.state",
            CA_COUNTRY = "ca.country",
            CA_CERT_PATH = "ca.cert.path",
            CA_KEY_PATH = "ca.key.path",
            CA_SIGNATURE_ALGORITHM = "ca.signature_algorithm",
            CA_KEY_ALGORITHM = "ca.key_algorithm";

    private final X500Name issuer;
    private final X509Certificate caCertificate;
    private final PrivateKey caPrivateKey;
    private final String signatureAlgorithm;
    private final String keyAlgorithm;

    @Autowired
    public X509CertificateFactory(ConfigFileLoader configLoader) throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException {
        Properties properties = configLoader.loadPropertiesFromFile("x509.config.properties");
        this.issuer = X509CertificateUtils.createX500Name(
                properties.getProperty(CA_COMMON_NAME),
                properties.getProperty(CA_ORGANIZATION),
                properties.getProperty(CA_ORGANIZATIONAL_UNIT),
                properties.getProperty(CA_LOCALITY),
                properties.getProperty(CA_STATE),
                properties.getProperty(CA_COUNTRY)
        );
        this.caCertificate = readCACert(properties.getProperty(CA_CERT_PATH));
        this.caPrivateKey = readCAPrivateKey(properties.getProperty(CA_KEY_PATH));
        this.signatureAlgorithm = properties.getProperty(CA_SIGNATURE_ALGORITHM);
        this.keyAlgorithm = properties.getProperty(CA_KEY_ALGORITHM);
    }

    private PrivateKey readCAPrivateKey(String fileName) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        String pemContent = new String(Files.readAllBytes(Paths.get(fileName)));

        String base64Key = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);

        return keyFactory.generatePrivate(keySpec);
    }

    private X509Certificate readCACert(String fileName) throws IOException, CertificateException {
        String pemContent = new String(Files.readAllBytes(Paths.get(fileName)));
        return X509CertificateUtils.fromPEM(pemContent);
    }

    @Override
    public X509Certificate generateCertificate(PublicKey publicKey, X500Name subject, Date notBefore, Date notAfter) throws CertificateException, OperatorCreationException {
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                publicKey
        );

        // Sign the certificate using the CA's private key
        ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm)
                .build(caPrivateKey);

        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certBuilder.build(signer));
    }

    @Override
    public boolean validateCertificate(X509Certificate certificate) {
        try {
            PublicKey caPublicKey = caCertificate.getPublicKey();
            certificate.verify(caPublicKey);
            return true;
        } catch (CertificateException | NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
            return false;
        }
    }
}
