-- ============================================================================
-- SCRIPT DE INICIALIZACION: CATALOGO DE PRODUCTOS OYL FRUTOS SECOS
-- Carga automatica al arrancar el microservicio en Spring Boot
-- ============================================================================

INSERT INTO productos (codigo_qr, nombre, categoria, precio, stock, formato, descripcion, imagen_uri, activo) VALUES
('OYL-ALM-001', 'Almendras Non Pareil', 'FRUTOS_SECOS', 4500, 25, '250 gr', 'Almendras seleccionadas de exportacion, tostadas sin sal. Ricas en proteinas, fibra y vitamina E.', NULL, true),
('OYL-NUE-002', 'Nueces Mariposa Extra Light', 'FRUTOS_SECOS', 5200, 18, '250 gr', 'Mitades de nueces chilenas de color claro, textura crujiente y alto contenido de acidos grasos Omega-3.', NULL, true),
('OYL-CAS-003', 'Castanas de Caju Tostadas', 'FRUTOS_SECOS', 6800, 6, '250 gr', 'Castanas de caju de grano entero, ligeramente tostadas. Fuente natural de magnesio, hierro y fosforo.', NULL, true),
('OYL-PIS-004', 'Pistachos con Cascara y Sal Marina', 'FRUTOS_SECOS', 7400, 12, '300 gr', 'Pistachos tostados al horno con un toque de sal marina de Cahuil. Gran sabor y aporte antioxidante.', NULL, true),
('OYL-MAN-005', 'Mani Tostado sin Sal', 'FRUTOS_SECOS', 2200, 45, '500 gr', 'Mani desgrasado al horno, ideal para colaciones saludables, repostería o mantequilla casera.', NULL, true),
('OYL-CHI-006', 'Semillas de Chia Negra', 'SEMILLAS', 2900, 30, '500 gr', 'Semillas de chia organica de alta pureza. Excelente fuente vegetal de fibra soluble y acidos Omega-3.', NULL, true),
('OYL-LIN-007', 'Semillas de Linaza Dorada', 'SEMILLAS', 2400, 5, '500 gr', 'Linaza dorada seleccionada para infusion, batidos o panaderia integral. Favorece la digestion.', NULL, true),
('OYL-ZAP-008', 'Semillas de Zapallo Peladas', 'SEMILLAS', 3800, 14, '250 gr', 'Pepitas de zapallo crudas ricas en zinc, antioxidantes y proteinas vegetales completas.', NULL, true),
('OYL-GIR-009', 'Semillas de Maravilla Peladas', 'SEMILLAS', 1900, 22, '250 gr', 'Semillas de girasol peladas y limpias, listas para consumo directo, ensaladas o granola.', NULL, true),
('OYL-ARA-010', 'Arandanos Rojos Deshidratados', 'DESHIDRATADOS', 4100, 4, '250 gr', 'Cranberries deshidratados endulzados levemente con jugo de manzana. Alto poder antioxidante.', NULL, true),
('OYL-PAS-011', 'Pasas Morenas Thompson', 'DESHIDRATADOS', 1800, 35, '500 gr', 'Pasas de uva natural deshidratadas al sol, dulces y carnosas. Aporte inmediato de energia sana.', NULL, true),
('OYL-DAT-012', 'Datiles Deglet Nour sin Carozo', 'DESHIDRATADOS', 5900, 8, '400 gr', 'Datiles naturales originarios de Tunez, de textura suave y dulzor acaramelado sin azucares anadidos.', NULL, true);
