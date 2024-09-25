package com.chuk_ca.sprint_initializer.model;

import java.util.List;

public interface X509CertificateManagerInterface {
    List<X509CertificateEntry> getAll();

    X509CertificateEntry getByID(Long id);
}
