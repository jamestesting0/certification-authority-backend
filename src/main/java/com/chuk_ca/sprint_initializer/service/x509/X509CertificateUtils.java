package com.chuk_ca.sprint_initializer.service.x509;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.hazelcast.internal.util.Sha256Util.bytesToHex;

public class X509CertificateUtils {
    private X509CertificateUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated.");
    }

    public static Date createDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static X500Name createX500Name(String commonName, String organization, String organizationalUnit,
                                          String locality, String state, String country) {
        return new X500Name(
                new RDN[] {
                        createRDN(BCStyle.CN, commonName),
                        createRDN(BCStyle.O, organization),
                        createRDN(BCStyle.OU, organizationalUnit),
                        createRDN(BCStyle.L, locality),
                        createRDN(BCStyle.ST, state),
                        createRDN(BCStyle.C, country)
                }
        );
    }

    private static RDN createRDN(ASN1ObjectIdentifier oid, String value) {
        return new RDN(oid, new DERPrintableString(value));
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public static void saveCertificateToPEM(String fileName, X509Certificate certificate) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(fileName))) {
            pemWriter.writeObject(certificate);
        }
    }

    public static void savePrivateKeyToPEM(String fileName, PrivateKey privateKey) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(fileName))) {
            pemWriter.writeObject(privateKey);
        }
    }

    public static X509Certificate readCertificateFromPEM(String filePath) throws IOException, CertificateException {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            Object object = pemParser.readObject();
            if (object instanceof X509CertificateHolder certificateHolder) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                byte[] certBytes = certificateHolder.getEncoded();
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(certBytes)) {
                    return (X509Certificate) certFactory.generateCertificate(inputStream);
                }
            } else {
                throw new CertificateException("Invalid certificate format");
            }
        }
    }

    public static PrivateKey readPrivateKeyFromPEM(String filePath) throws IOException, GeneralSecurityException {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (object instanceof PEMKeyPair keyPair) {
                return converter.getPrivateKey(keyPair.getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo privateKeyInfo) {
                return converter.getPrivateKey(privateKeyInfo);
            } else {
                throw new GeneralSecurityException("Invalid private key format");
            }
        }
    }

    public static String convertCertificateToPEM(X509Certificate certificate) throws IOException {
        try (
                StringWriter stringWriter = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)
        ) {
            pemWriter.writeObject(certificate);
            pemWriter.flush();
            return stringWriter.toString();
        }
    }

    public static String convertPrivateKeyToPEM(PrivateKey privateKey) throws IOException {
        try (
                StringWriter stringWriter = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)
        ) {
            pemWriter.writeObject(privateKey);
            pemWriter.flush();
            return stringWriter.toString();
        }
    }

    public static String convertCertificateToString(X509Certificate certificate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return "Certificate Details:\n" +
                "=====================\n" +
                "Version: " + certificate.getVersion() + "\n" +
                "Serial Number: " + certificate.getSerialNumber().toString(16) + "\n" +
                "Subject: " + certificate.getSubjectX500Principal().getName() + "\n" +
                "Issuer: " + certificate.getIssuerX500Principal().getName() + "\n" +
                "Not Before: " + dateFormat.format(certificate.getNotBefore()) + "\n" +
                "Not After: " + dateFormat.format(certificate.getNotAfter()) + "\n" +
                "Public Key: " + certificate.getPublicKey().toString() + "\n" +
                "Signature Algorithm: " + certificate.getSigAlgName() + "\n" +
                "Signature: " + bytesToHex(certificate.getSignature()) + "\n";
    }
}
