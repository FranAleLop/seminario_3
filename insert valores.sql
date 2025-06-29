-- Usar la base de datos TaiChi
USE TaiChi;

-- 1. Insertar Usuarios del Sistema (Tabla: UsuariosSistema)
--    Las contraseñas deben ser almacenadas como hashes seguros en un sistema real.
--    Aquí usamos placeholders para el hash.
INSERT INTO UsuariosSistema (nombre_usuario, password_hash, rol) VALUES
('director', 'hash_seguro_director_aqui', 'Director'),
('recepcionista', 'hash_seguro_recepcionista_aqui', 'Recepcionista');

-- 2. Insertar Períodos de Cuota (Tabla: PeriodosCuota)
--    Monto base $23000, límite día 10.
INSERT INTO PeriodosCuota (nombre_periodo, fecha_limite_sin_recargo, monto_base) VALUES
('Marzo 2025', '2025-03-10', 23000.00),
('Abril 2025', '2025-04-10', 23000.00);
-- Nota: Los IDs de estos períodos serán 1 y 2 respectivamente, si no hay datos previos.

-- 3. Insertar Profesores (Tabla: Profesores)
INSERT INTO Profesores (nombre_completo, telefono_contacto) VALUES
('Ana Pérez', '387-1111111'),
('Juan Gómez', '387-2222222'),
('María Rodríguez', '387-3333333');
-- Nota: Los IDs de estos profesores serán 1, 2 y 3 respectivamente.

-- 4. Insertar Clases (Tabla: Clases)
--    Asignamos profesores (IDs 1, 2, 3) y horarios.
INSERT INTO Clases (nombre_clase, horario, dia, id_profesor) VALUES
('children-I', '18:30-20:30', 'Lunes', 1), -- Ana Pérez
('adults-III', '18:30-20:30', 'Martes', 2), -- Juan Gómez
('juniors-II', '18:30-20:30', 'Miércoles', 3); -- María Rodríguez
-- Nota: Los IDs de estas clases serán 1, 2 y 3 respectivamente.

-- 5. Insertar Tipos de Documentos Requeridos (Tabla: DocumentosRequeridos)
INSERT INTO DocumentosRequeridos (tipo_documento) VALUES
('Copia DNI'),
('Formulario Inscripción');
-- Nota: Los IDs de estos documentos serán 1 y 2 respectivamente.

-- 6. Insertar Alumnos (Tabla: Alumnos)
--    Inventamos 10 alumnos. Asegurar DNI único.
INSERT INTO Alumnos (nombre_completo, dni, fecha_nacimiento, direccion, telefono_contacto, email) VALUES
('Carlos López', '40111222', '2000-01-15', 'Calle Falsa 123', '387-4444444', 'carlos.lopez@email.com'),    -- Alumno 1
('Ana García', '41333444', '1999-05-20', 'Av. Siempre Viva 742', '387-5555555', 'ana.garcia@email.com'),      -- Alumno 2
('Pedro Martínez', '39555666', '2001-11-10', 'Ruta 66 Km 10', '387-6666666', 'pedro.m@email.com'),      -- Alumno 3
('Laura Fernández', '42777888', '2002-03-01', 'Boulevard del Sol 45', '387-7777777', 'laura.f@email.com'),   -- Alumno 4
('Diego Pérez', '43999000', '2003-07-07', 'Pasaje Secreto 8', '387-8888888', 'diego.p@email.com'),       -- Alumno 5
('Sofía Gmez', '44121314', '2000-09-30', 'Calle Imaginaria 1', '387-9999999', 'sofia.g@email.com'),      -- Alumno 6
('Martín Ruiz', '45151617', '2001-04-03', 'Av. Inventada 2', '387-1010101', 'martin.r@email.com'),      -- Alumno 7
('Valeria Díaz', '46181920', '2002-08-22', 'Rincón Olvidado 3', '387-1112131', 'valeria.d@email.com'),    -- Alumno 8
('Gabriel Sánchez', '47212223', '2003-12-05', 'Camino Desconocido 4', '387-1415161', 'gabriel.s@email.com'), -- Alumno 9
('Paula Torres', '48242526', '2000-02-18', 'Sendero Oculto 5', '387-1718191', 'paula.t@email.com');     -- Alumno 10
-- Nota: Los IDs de estos alumnos serán 1 al 10 respectivamente.

-- 7. Asignar Alumnos a Clases (Tabla: Alumnos_Clases)
--    IDs de Alumnos (1-10), IDs de Clases (1-3)
INSERT INTO Alumnos_Clases (id_alumno, id_clase) VALUES
(1, 1), -- Carlos en children-I
(2, 1), -- Ana en children-I
(3, 2), -- Pedro en adults-III
(4, 2), -- Laura en adults-III
(5, 3), -- Diego en juniors-II
(6, 3), -- Sofía en juniors-II
(7, 1), -- Martín en children-I (un alumno en 2 clases?)
(7, 2), -- Martin también en adults-III
(8, 2), -- Valeria en adults-III
(9, 3), -- Gabriel en juniors-II
(10, 1); -- Paula en children-I
-- Nota: Ajustar si la asignación es solo 1 alumno a 1 clase. Mantengo la tabla de unión por si la relación M:N es correcta.

-- 8. Establecer Estados de Documentos (Tabla: EstadosDocumentosAlumno)
--    IDs Alumnos (1-10), IDs DocumentosRequeridos (1: Copia DNI, 2: Formulario Inscripción)
INSERT INTO EstadosDocumentosAlumno (id_alumno, id_documento, estado_entrega, fecha_entrega) VALUES
(1, 1, 'Entregado', '2025-03-01'), -- Carlos entregó DNI
(1, 2, 'Entregado', '2025-03-01'), -- Carlos entregó Formulario
(2, 1, 'Entregado', '2025-03-05'), -- Ana entregó DNI
(2, 2, 'Entregado', '2025-03-05'), -- Ana entregó Formulario
-- Alumno 3: No presentó Fotocopia DNI (Documento ID 1) -> Insertar como 'Pendiente'
(3, 1, 'Pendiente', NULL),
(3, 2, 'Entregado', '2025-03-08'), -- Pedro entregó Formulario
(4, 1, 'Entregado', '2025-03-10'), -- Laura entregó DNI
-- Alumno 4: No completó Formulario (Documento ID 2) -> Insertar como 'Pendiente'
(4, 2, 'Pendiente', NULL),
(5, 1, 'Entregado', '2025-03-12'), -- Diego entregó DNI
(5, 2, 'Entregado', '2025-03-12'), -- Diego entregó Formulario
(6, 1, 'Entregado', '2025-04-01'), -- Sofía entregó DNI
(6, 2, 'Entregado', '2025-04-01'), -- Sofía entregó Formulario
(7, 1, 'Entregado', '2025-04-05'), -- Martín entregó DNI
(7, 2, 'Entregado', '2025-04-05'), -- Martín entregó Formulario
(8, 1, 'Entregado', '2025-04-10'), -- Valeria entregó DNI
(8, 2, 'Entregado', '2025-04-10'), -- Valeria entregó Formulario
(9, 1, 'Entregado', '2025-04-15'), -- Gabriel entregó DNI
(9, 2, 'Entregado', '2025-04-15'), -- Gabriel entregó Formulario
(10, 1, 'Entregado', '2025-04-20'), -- Paula entregó DNI
(10, 2, 'Entregado', '2025-04-20'); -- Paula entregó Formulario

-- 9. Insertar Pagos (Tabla: Pagos)
--    Basado en los escenarios descritos para Mar 2025 (ID 1) y Abr 2025 (ID 2).
--    Cuota Base: 23000.00. Recargo: 3000.00 (Lógica de recargo > Día 13).
INSERT INTO Pagos (id_alumno, id_periodo, fecha_pago, monto_pagado, tipo_pago, es_pago_parcial, monto_recargo_aplicado) VALUES
-- Alumno 1 (Carlos López): No pagó Marzo (no hay insert para Mar 2025), Pagó Abril a tiempo (Total)
(1, 2, '2025-04-05', 23000.00, 'Efectivo', FALSE, 0.00),

-- Alumno 2 (Ana García): Pagó Marzo a tiempo (Total), Pagó Abril a tiempo (Total)
(2, 1, '2025-03-08', 23000.00, 'Transferencia', FALSE, 0.00),
(2, 2, '2025-04-09', 23000.00, 'Transferencia', FALSE, 0.00),

-- Alumno 3 (Pedro Martínez): No pagó Marzo, No pagó Abril (no hay inserts)

-- Alumno 4 (Laura Fernández): Pagó Marzo Tarde (Total, con recargo), Pagó Abril a tiempo (Total)
(4, 1, '2025-03-15', 26000.00, 'Efectivo', FALSE, 3000.00), -- Día 15 > Día 13
(4, 2, '2025-04-10', 23000.00, 'Efectivo', FALSE, 0.00),

-- Alumno 5 (Diego Pérez): Pagó Marzo a tiempo (Total), Pagó Abril Parcial ($20000) a tiempo
(5, 1, '2025-03-09', 23000.00, 'Transferencia', FALSE, 0.00),
(5, 2, '2025-04-08', 20000.00, 'Transferencia', TRUE, 0.00), -- Parcial de 20000 a tiempo

-- Alumno 6 (Sofía Gómez): Pagó Marzo a tiempo (Total), Pagó Abril Tarde (Total, con recargo)
(6, 1, '2025-03-07', 23000.00, 'Efectivo', FALSE, 0.00),
(6, 2, '2025-04-18', 26000.00, 'Efectivo', FALSE, 3000.00), -- Día 18 > Día 13

-- Alumno 7 (Martín Ruiz): Pagó Marzo a tiempo (Total), No pagó Abril
(7, 1, '2025-03-10', 23000.00, 'Transferencia', FALSE, 0.00),

-- Alumno 8 (Valeria Díaz): Pagó Marzo Tarde (Total, con recargo), No pagó Abril
(8, 1, '2025-03-20', 26000.00, 'Transferencia', FALSE, 3000.00), -- Día 20 > Día 13

-- Alumno 9 (Gabriel Sánchez): No pagó Marzo, Pagó Abril Tarde (Total, con recargo)
(9, 2, '2025-04-25', 26000.00, 'Efectivo', FALSE, 3000.00); -- Día 25 > Día 13

-- Alumno 10 (Paula Torres): No pagó Marzo, No pagó Abril (no hay inserts)

-- 10. Reactivar checks de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;