package com.chuk_ca.sprint_initializer.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class X509CertificateData {
    private String publicKey;
    private String commonName;
    private String organization;
    private String organizationUnit;
    private String locality;
    private String state;
    private String country;
    private Date notBefore;
    private Date notAfter;
}
