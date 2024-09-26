package com.chuk_ca.sprint_initializer.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class X509CertificateResponse {
    private int version;
    private String serialNumber;
    private String subject;
    private String issuer;
    private Date notBefore;
    private Date notAfter;
    private String publicKey;
    private String signatureAlgorithm;
    private String signature;
    private Map<String, String> extensions;
}
