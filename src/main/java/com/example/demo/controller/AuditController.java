package com.example.demo.controller;

import com.example.demo.security.PermissionConstants;
import com.example.demo.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/revisions/{entityClass}/{entityId}")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<List<Number>> getEntityRevisions(
            @PathVariable String entityClass,
            @PathVariable Long entityId) {
        try {
            Class<?> clazz = Class.forName("com.example.demo.entity." + entityClass);
            List<Number> revisions = auditService.getRevisions(clazz, entityId);
            return ResponseEntity.ok(revisions);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/revisions/{entityClass}/{entityId}/{revisionNumber}")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<?> getEntityAtRevision(
            @PathVariable String entityClass,
            @PathVariable Long entityId,
            @PathVariable Long revisionNumber) {
        try {
            Class<?> clazz = Class.forName("com.example.demo.entity." + entityClass);
            Object entity = auditService.getEntityAtRevision(clazz, entityId, revisionNumber);
            return ResponseEntity.ok(entity);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
