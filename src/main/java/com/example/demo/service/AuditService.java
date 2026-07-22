package com.example.demo.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final EntityManager entityManager;

    public List<Number> getRevisions(Class<?> entityClass, Long entityId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.getRevisions(entityClass, entityId);
    }

    public <T> T getEntityAtRevision(Class<T> entityClass, Long entityId, Number revision) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.find(entityClass, entityId, revision);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAllRevisions(Class<T> entityClass, Long entityId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(entityId))
                .getResultList();
    }
}
