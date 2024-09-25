package com.chuk_ca.sprint_initializer.service.x509;

import com.chuk_ca.sprint_initializer.service.util.ConfigFileLoader;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

@Component
public class X509CertificateFactoryImpl implements X509CertificateFactory {
    private static final Logger logger = LoggerFactory.getLogger(X509CertificateFactoryImpl.class);
    private static final String CA_COMMON_NAME = "ca.common_name",
            CA_ORGANIZATION = "ca.organization",
            CA_ORGANIZATIONAL_UNIT = "ca.organizational_unit",
            CA_LOCALITY = "ca.locality",
            CA_STATE = "ca.state",
            CA_COUNTRY = "ca.country",
            CA_CERT_PATH = "ca.cert.path",
            CA_PRIVATE_KEY_PATH = "ca.key.path",
            CA_SIGNATURE_ALGORITHM = "ca.signature_algorithm";

    private final X500Name issuer;
    private final String signatureAlgorithm;
    private final PrivateKey caPrivateKey;
    private final X509Certificate caCertificate;

    public X509CertificateFactoryImpl(ConfigFileLoader configLoader) throws IOException, GeneralSecurityException, OperatorCreationException {
        Properties properties = configLoader.loadPropertiesFromFile("x509.properties");
        this.issuer = X509CertificateUtils.createX500Name(
                properties.getProperty(CA_COMMON_NAME),
                properties.getProperty(CA_ORGANIZATION),
                properties.getProperty(CA_ORGANIZATIONAL_UNIT),
                properties.getProperty(CA_LOCALITY),
                properties.getProperty(CA_STATE),
                properties.getProperty(CA_COUNTRY)
        );

        this.signatureAlgorithm = properties.getProperty(CA_SIGNATURE_ALGORITHM);

        // Get/create the CA's private key and certificate
        String caPrivateKeyPath = properties.getProperty(CA_PRIVATE_KEY_PATH);
        String caCertificatePath = properties.getProperty(CA_CERT_PATH);
        if (Files.notExists(Paths.get(caPrivateKeyPath)) || Files.notExists(Paths.get(caCertificatePath))) {
            KeyPair caKeyPair = X509CertificateUtils.generateKeyPair();
            this.caPrivateKey = caKeyPair.getPrivate();
            PublicKey caPublicKey = caKeyPair.getPublic();
            X509CertificateUtils.savePrivateKeyToPEM(caPrivateKeyPath, this.caPrivateKey);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 5);
            this.caCertificate = generateCertificate(
                    caPublicKey,
                    this.issuer,
                    new Date(),
                    calendar.getTime()
            );
            X509CertificateUtils.saveCertificateToPEM(caCertificatePath, this.caCertificate);
        } else {
            this.caPrivateKey = readCAPrivateKey(caPrivateKeyPath);
            this.caCertificate = readCACert(caCertificatePath);
        }

        logger.info("Certificate is loaded.\n{}", X509CertificateUtils.convertCertificateToString(this.caCertificate));
    }

    private PrivateKey readCAPrivateKey(String fileName) throws GeneralSecurityException, IOException {
        return X509CertificateUtils.readPrivateKeyFromPEM(fileName);
    }

    private X509Certificate readCACert(String fileName) throws IOException, CertificateException {
        return X509CertificateUtils.readCertificateFromPEM(fileName);
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
