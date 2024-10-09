package com.chuk_ca.sprint_initializer.service.util;

import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class CSRUtils {
    private CSRUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated.");
    }

    public static Map<String, String> parseCSR(String csr) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Map<String, String> csrDetails = new HashMap<>();

        // Create a StringReader from the CSR content
        try (StringReader reader = new StringReader(csr);
             PEMParser pemParser = new PEMParser(reader)) {

            Object parsedObject = pemParser.readObject();
            if (!(parsedObject instanceof PKCS10CertificationRequest pkcs10Request)) {
                throw new IllegalArgumentException("Provided CSR is not a valid PKCS10CertificationRequest.");
            }

            // Extract the subject information
            X500Name x500Name = pkcs10Request.getSubject();
            csrDetails.put("Common Name (CN)", getRDNValue(x500Name, BCStyle.CN));
            csrDetails.put("Organization (O)", getRDNValue(x500Name, BCStyle.O));
            csrDetails.put("Organizational Unit (OU)", getRDNValue(x500Name, BCStyle.OU));
            csrDetails.put("Country (C)", getRDNValue(x500Name, BCStyle.C));
            csrDetails.put("State/Province (ST)", getRDNValue(x500Name, BCStyle.ST));
            csrDetails.put("Locality (L)", getRDNValue(x500Name, BCStyle.L));

            // Extract the public key from the SubjectPublicKeyInfo
            SubjectPublicKeyInfo publicKeyInfo = pkcs10Request.getSubjectPublicKeyInfo();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyInfo.getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            csrDetails.put("Public Key", X509CertificateUtils.convertPublicKeyToPEM(publicKey));
        }

        return csrDetails;
    }

    public static String readCSRFromFile(String filePath) throws IOException {
        StringBuilder csrContent = new StringBuilder();
        File file = new File(filePath);

        try (FileReader fileReader = new FileReader(file)) {
            int ch;
            while ((ch = fileReader.read()) != -1) {
                csrContent.append((char) ch);
            }
        }

        return csrContent.toString();
    }

    // Helper method to extract specific RDN values
    private static String getRDNValue(X500Name x500Name, ASN1ObjectIdentifier identifier) {
        RDN[] rdns = x500Name.getRDNs(identifier);
        return rdns.length > 0 ? rdns[0].getFirst().getValue().toString() : null;
    }
}
