package com.chuk_ca.sprint_initializer.service.util;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

public class X509CertificateUtils {
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

    public static void saveKeyToPEM(String fileName, Key key) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(fileName))) {
            pemWriter.writeObject(key);
        }
    }

    public static X509Certificate fromPEM(String certificatePEM) throws CertificateException {
        String normalizedPEM = certificatePEM
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = java.util.Base64.getDecoder().decode(normalizedPEM);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded)) {
            return (X509Certificate) certFactory.generateCertificate(inputStream);
        } catch (IOException e) {
            throw new CertificateException(e);
        }
    }

    public static String toPEM(X509Certificate certificate) throws IOException {
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
