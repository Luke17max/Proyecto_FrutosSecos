-- ============================================================================
-- SCRIPT DE INICIALIZACION: USUARIOS Y PEDIDOS OYL FRUTOS SECOS
-- Carga automatica al arrancar el microservicio en Spring Boot
-- ============================================================================

INSERT INTO usuarios (nombre, alias, correo, telefono, direccion, contrasena, rol, avatar_uri) VALUES
('Administrador OYL', 'admin', 'admin@admin.cl', '+56 9 1234 1234', 'Av. Providencia 1234, Santiago', 'admin', 'ADMINISTRADOR', NULL),
('Ana Torres', 'ana', 'ana@correo.cl', '+56 9 1234 5678', 'Los Leones 456, Depto 302, Providencia', '1234', 'CLIENTE', NULL),
('Carlos Mendez', 'carlos', 'carlos@correo.cl', '+56 9 8765 4321', 'Av. Apoquindo 4500, Las Condes', '1234', 'CLIENTE', NULL);

INSERT INTO pedidos (numero_pedido, cliente_id, cliente_nombre, fecha_hora, tipo_entrega, direccion_entrega, subtotal, costo_envio, total, estado) VALUES
('PED-1001', 2, 'Ana Torres', '17/11/2026 14:30', 'RETIRO_TIENDA', 'Tienda Central OYL - Av. Providencia 1234', 9700, 0, 9700, 'CONFIRMADO'),
('PED-1002', 2, 'Ana Torres', '18/11/2026 10:15', 'DESPACHO_DOMICILIO', 'Los Leones 456, Depto 302, Providencia', 11300, 2500, 13800, 'PENDIENTE_VALIDACION'),
('PED-1003', 3, 'Carlos Mendez', '18/11/2026 12:45', 'RETIRO_TIENDA', 'Punto de Retiro Metro Manuel Montt', 6800, 0, 6800, 'PREPARADO');

INSERT INTO detalles_pedido (pedido_id, producto_id, nombre_producto, precio_unitario, cantidad, subtotal) VALUES
(1, 1, 'Almendras Non Pareil', 4500, 1, 4500),
(1, 2, 'Nueces Mariposa Extra Light', 5200, 1, 5200),
(2, 3, 'Castanas de Caju Tostadas', 6800, 1, 6800),
(2, 1, 'Almendras Non Pareil', 4500, 1, 4500),
(3, 3, 'Castanas de Caju Tostadas', 6800, 1, 6800);
