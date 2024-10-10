package com.chuk_ca.sprint_initializer.dao;

import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;

import java.util.List;

public interface DAO<T> {
    List<T> getAll();

    T getByID(Long id);

    void deleteByID(Long id);

    T save(X509CertificateEntry certificateEntry);
}
