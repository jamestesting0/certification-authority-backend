package com.chuk_ca.sprint_initializer.service.x509;

import com.chuk_ca.sprint_initializer.model.X509CertificateResponse;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public static X509Certificate readCertificateFromFile(String filePath) throws IOException, CertificateException {
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

    public static PrivateKey readPrivateKeyFromFile(String filePath) throws IOException, GeneralSecurityException {
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

    public static X509Certificate readCertificateFromPEM(String pemString) throws IOException, CertificateException {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemString))) {
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

    public static String convertPublicKeyToPEM(PublicKey publicKey) throws IOException {
        try (
                StringWriter stringWriter = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)
        ) {
            pemWriter.writeObject(publicKey);
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
        StringBuilder sb = new StringBuilder();

        sb.append("Certificate Details:\n");
        sb.append("=====================\n");
        sb.append("Version: ").append(certificate.getVersion()).append("\n");
        sb.append("Serial Number: ").append(certificate.getSerialNumber().toString(16)).append("\n");
        sb.append("Subject: ").append(certificate.getSubjectX500Principal().getName()).append("\n");
        sb.append("Issuer: ").append(certificate.getIssuerX500Principal().getName()).append("\n");
        sb.append("Not Before: ").append(dateFormat.format(certificate.getNotBefore())).append("\n");
        sb.append("Not After: ").append(dateFormat.format(certificate.getNotAfter())).append("\n");
        sb.append("Public Key: ").append(certificate.getPublicKey().toString()).append("\n");
        sb.append("Signature Algorithm: ").append(certificate.getSigAlgName()).append("\n");
        sb.append("Signature (hex): ").append(bytesToHex(certificate.getSignature())).append("\n");

        sb.append("Extensions:\n");
        try {
            Set<String> criticalExtensions = certificate.getCriticalExtensionOIDs();
            Set<String> nonCriticalExtensions = certificate.getNonCriticalExtensionOIDs();

            if (criticalExtensions != null && !criticalExtensions.isEmpty()) {
                sb.append("  Critical Extensions:\n");
                for (String oid : criticalExtensions) {
                    sb.append("    OID: ").append(oid).append(" - ").append(getExtensionName(oid)).append("\n");
                }
            }

            if (nonCriticalExtensions != null && !nonCriticalExtensions.isEmpty()) {
                sb.append("  Non-Critical Extensions:\n");
                for (String oid : nonCriticalExtensions) {
                    sb.append("    OID: ").append(oid).append(" - ").append(getExtensionName(oid)).append("\n");
                }
            }

            if ((criticalExtensions == null || criticalExtensions.isEmpty()) &&
                    (nonCriticalExtensions == null || nonCriticalExtensions.isEmpty())) {
                sb.append("  No extensions present.\n");
            }

        } catch (Exception e) {
            sb.append("  (Failed to retrieve extensions)\n");
        }

        return sb.toString();
    }

    public static X509CertificateResponse convertCertificateToJSON(X509Certificate certificate) {
        X509CertificateResponse response = new X509CertificateResponse();
        response.setVersion(certificate.getVersion());
        response.setSerialNumber(certificate.getSerialNumber().toString(16).toUpperCase());
        response.setSubject(certificate.getSubjectX500Principal().getName());
        response.setIssuer(certificate.getIssuerX500Principal().getName());
        response.setNotBefore(certificate.getNotBefore());
        response.setNotAfter(certificate.getNotAfter());
        response.setPublicKey(certificate.getPublicKey().toString());
        response.setSignatureAlgorithm(certificate.getSigAlgName());
        response.setSignature(bytesToHex(certificate.getSignature()));

        Map<String, String> extensionsMap = new HashMap<>();
        try {
            Set<String> criticalExtensions = certificate.getCriticalExtensionOIDs();
            Set<String> nonCriticalExtensions = certificate.getNonCriticalExtensionOIDs();

            if (criticalExtensions != null) {
                for (String oid : criticalExtensions) {
                    extensionsMap.put(oid, getExtensionName(oid) + " (Critical)");
                }
            }

            if (nonCriticalExtensions != null) {
                for (String oid : nonCriticalExtensions) {
                    extensionsMap.put(oid, getExtensionName(oid) + " (Non-Critical)");
                }
            }
        } catch (Exception e) {
            extensionsMap.put("Error", "Failed to retrieve extensions");
        }

        response.setExtensions(extensionsMap);

        return response;
    }

    private static String getExtensionName(String oid) {
        return switch (oid) {
            case "2.5.29.14" -> "Subject Key Identifier";
            case "2.5.29.35" -> "Authority Key Identifier";
            case "2.5.29.19" -> "Basic Constraints";
            case "2.5.29.15" -> "Key Usage";
            case "2.5.29.37" -> "Extended Key Usage";
            default -> "Unknown Extension";
        };
    }

    public static InputStreamSource stringToInputStreamSource(String content) {
        byte[] byteArray = content.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(byteArray);
    }
}
