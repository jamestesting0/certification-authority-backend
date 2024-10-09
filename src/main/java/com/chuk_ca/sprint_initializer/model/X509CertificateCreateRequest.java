package com.chuk_ca.sprint_initializer.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class X509CertificateCreateRequest {
    private String csr;
    private Date notBefore;
    private Date notAfter;
}
