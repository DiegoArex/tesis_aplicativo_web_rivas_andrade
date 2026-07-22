-- Active: 1772201258173@@127.0.0.1@5432@pruebas
-- aqui esto lo ejecutamos en el sql. de SUPABASE

-- 1. LABORATORIOS 
-- ============================================
INSERT INTO laboratorios (id, nombre, ubicacion, capacidad, descripcion, activo, created_at, updated_at) VALUES
(1, 'Laboratorio 1', 'Piso 2', 30, 'Laboratorio de Programación 1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Laboratorio 2', 'Piso 3', 25, 'Laboratorio de Programación 2', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Laboratorio 3', 'Piso 1', 35, 'Laboratorio de Programación 3', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Laboratorio 4', 'Piso 2', 20, 'Laboratorio de Programación 4', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Laboratorio 5', 'Piso 1', 28, 'Laboratorio de Programación 5', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Laboratorio 6', 'Piso 1', 28, 'Laboratorio de Programación 6', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Laboratorio 7', 'Piso 1', 28, 'Laboratorio de Programación 7', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Laboratorio A', 'SUFICIENCIA', 28, 'Laboratorio de Programación A', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Laboratorio B', 'SUFICIENCIA', 28, 'Laboratorio de Programación B', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. EQUIPOS (Reducido a 5 por cada Laboratorio)
-- ============================================

-- EQUIPOS LABORATORIO 1
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LAB1-PC01', 'PC', 'Dell', 'OptiPlex 7090', 'SN-L1-01', 'OPERATIVO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB1-PC02', 'PC', 'Dell', 'OptiPlex 7090', 'SN-L1-02', 'OPERATIVO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB1-PC03', 'PC', 'Dell', 'OptiPlex 7090', 'SN-L1-03', 'OPERATIVO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB1-PC04', 'PC', 'Dell', 'OptiPlex 7090', 'SN-L1-04', 'OPERATIVO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB1-PC05', 'PC', 'Dell', 'OptiPlex 7090', 'SN-L1-05', 'DANADO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO 2
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LAB2-PC01', 'PC', 'HP', 'EliteDesk 800', 'SN-L2-01', 'OPERATIVO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB2-PC02', 'PC', 'HP', 'EliteDesk 800', 'SN-L2-02', 'OPERATIVO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB2-PC03', 'PC', 'HP', 'EliteDesk 800', 'SN-L2-03', 'OPERATIVO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB2-PC04', 'PC', 'HP', 'EliteDesk 800', 'SN-L2-04', 'OPERATIVO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB2-PC05', 'PC', 'HP', 'EliteDesk 800', 'SN-L2-05', 'OPERATIVO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO 3
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LAB3-PC01', 'PC', 'Lenovo', 'ThinkCentre M720', 'SN-L3-01', 'OPERATIVO', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB3-PC02', 'PC', 'Lenovo', 'ThinkCentre M720', 'SN-L3-02', 'OPERATIVO', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB3-PC03', 'PC', 'Lenovo', 'ThinkCentre M720', 'SN-L3-03', 'OPERATIVO', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB3-PC04', 'PC', 'Lenovo', 'ThinkCentre M720', 'SN-L3-04', 'OPERATIVO', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB3-PC05', 'PC', 'Lenovo', 'ThinkCentre M720', 'SN-L3-05', 'OPERATIVO', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO 4
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LAB4-PC01', 'PC', 'Dell', 'Precision', 'SN-L4-01', 'OPERATIVO', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB4-PC02', 'PC', 'Dell', 'Precision', 'SN-L4-02', 'OPERATIVO', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB4-PC03', 'PC', 'Dell', 'Precision', 'SN-L4-03', 'OPERATIVO', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB4-PC04', 'PC', 'Dell', 'Precision', 'SN-L4-04', 'OPERATIVO', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB4-PC05', 'PC', 'Dell', 'Precision', 'SN-L4-05', 'OPERATIVO', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO 5
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LAB5-PC01', 'PC', 'HP', 'EliteDesk', 'SN-L5-01', 'OPERATIVO', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB5-PC02', 'PC', 'HP', 'EliteDesk', 'SN-L5-02', 'OPERATIVO', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB5-PC03', 'PC', 'HP', 'EliteDesk', 'SN-L5-03', 'OPERATIVO', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB5-PC04', 'PC', 'HP', 'EliteDesk', 'SN-L5-04', 'OPERATIVO', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB5-PC05', 'PC', 'HP', 'EliteDesk', 'SN-L5-05', 'OPERATIVO', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO 6
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LAB6-PC01', 'PC', 'Lenovo', 'M720', 'SN-L6-01', 'OPERATIVO', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB6-PC02', 'PC', 'Lenovo', 'M720', 'SN-L6-02', 'OPERATIVO', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB6-PC03', 'PC', 'Lenovo', 'M720', 'SN-L6-03', 'OPERATIVO', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB6-PC04', 'PC', 'Lenovo', 'M720', 'SN-L6-04', 'OPERATIVO', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB6-PC05', 'PC', 'Lenovo', 'M720', 'SN-L6-05', 'OPERATIVO', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO 7
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LAB7-PC01', 'PC', 'Dell', 'Vostro', 'SN-L7-01', 'OPERATIVO', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB7-PC02', 'PC', 'Dell', 'Vostro', 'SN-L7-02', 'OPERATIVO', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB7-PC03', 'PC', 'Dell', 'Vostro', 'SN-L7-03', 'OPERATIVO', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB7-PC04', 'PC', 'Dell', 'Vostro', 'SN-L7-04', 'OPERATIVO', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LAB7-PC05', 'PC', 'Dell', 'Vostro', 'SN-L7-05', 'OPERATIVO', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO A
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LABA-PC01', 'PC', 'Apple', 'iMac', 'SN-LA-01', 'OPERATIVO', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABA-PC02', 'PC', 'Apple', 'iMac', 'SN-LA-02', 'OPERATIVO', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABA-PC03', 'PC', 'Apple', 'iMac', 'SN-LA-03', 'OPERATIVO', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABA-PC04', 'PC', 'Apple', 'iMac', 'SN-LA-04', 'OPERATIVO', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABA-PC05', 'PC', 'Apple', 'iMac', 'SN-LA-05', 'OPERATIVO', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EQUIPOS LABORATORIO B
INSERT INTO equipos (codigo, tipo, marca, modelo, numero_serie, estado, laboratorio_id, created_at, updated_at) VALUES
('LABB-PC01', 'PC', 'Dell', 'OptiPlex', 'SN-LB-01', 'OPERATIVO', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABB-PC02', 'PC', 'Dell', 'OptiPlex', 'SN-LB-02', 'OPERATIVO', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABB-PC03', 'PC', 'Dell', 'OptiPlex', 'SN-LB-03', 'OPERATIVO', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABB-PC04', 'PC', 'Dell', 'OptiPlex', 'SN-LB-04', 'OPERATIVO', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LABB-PC05', 'PC', 'Dell', 'OptiPlex', 'SN-LB-05', 'OPERATIVO', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Resetear secuencias DESPUES de todos los INSERTs
SELECT setval('laboratorios_id_seq', (SELECT MAX(id) FROM laboratorios) + 1);
SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios) + 1);
SELECT setval('equipos_id_seq', (SELECT MAX(id) FROM equipos) + 1);
SELECT setval('novedades_id_seq', (SELECT MAX(id) FROM novedades) + 1);
SELECT setval('registros_uso_id_seq', (SELECT MAX(id) FROM registros_uso) + 1);
SELECT setval('imagenes_novedad_id_seq', (SELECT MAX(id) FROM imagenes_novedad) + 1);