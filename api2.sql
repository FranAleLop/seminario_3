-- 1. Crear la Base de Datos
--    Usamos IF NOT EXISTS para evitar errores si la base de datos ya existe.
CREATE DATABASE IF NOT EXISTS TaiChi
CHARACTER SET utf8mb4 -- Soporte para caracteres especiales y emojis
COLLATE utf8mb4_unicode_ci; -- Collation para ordenamiento sensible a mayúsculas/minúsculas y acentos

-- Seleccionar la base de datos para usar
USE TaiChi;

-- 2. Desactivar temporalmente los checks de claves foráneas
--    Esto es útil para crear tablas en cualquier orden y luego añadir las FKs.
--    En este script ya las creamos en orden, pero es una práctica común.
SET FOREIGN_KEY_CHECKS = 0;

-- 3. Crear las Tablas

-- Tabla: Profesores (Se crea primero porque es referenciada por 'Clases')
CREATE TABLE Profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    telefono_contacto VARCHAR(50)
);

-- Tabla: PeriodosCuota (Se crea primero porque es referenciada por 'Pagos')
CREATE TABLE PeriodosCuota (
    id_periodo INT AUTO_INCREMENT PRIMARY KEY,
    nombre_periodo VARCHAR(50) UNIQUE NOT NULL, -- Ej. "Mayo 2025"
    fecha_limite_sin_recargo DATE NOT NULL,
    monto_base DECIMAL(10, 2) NOT NULL
);

-- Tabla: DocumentosRequeridos (Se crea primero porque es referenciada por 'EstadosDocumentosAlumno')
CREATE TABLE DocumentosRequeridos (
    id_documento INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(100) UNIQUE NOT NULL -- Ej. 'Copia DNI', 'Certificado Nacimiento'
);

-- Tabla: UsuariosSistema (Se crea antes de cualquier tabla que pudiera tener FK hacia ella, si aplicara)
CREATE TABLE UsuariosSistema (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Almacenar hashes seguros, no contraseñas en texto plano
    rol VARCHAR(50) NOT NULL -- Ej. 'Recepcionista', 'Director'
);

-- Tabla: Alumnos (Se crea antes de tablas que la referencian como 'Pagos', 'Alumnos_Clases', 'EstadosDocumentosAlumno')
CREATE TABLE Alumnos (
    id_alumno INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    dni VARCHAR(20) UNIQUE, -- DNI único, pero permitimos NULL por si no aplica o no se conoce
    fecha_nacimiento DATE,
    direccion VARCHAR(255),
    telefono_contacto VARCHAR(50),
    email VARCHAR(100)
);

-- Tabla: Clases (Referencia a Profesores)
CREATE TABLE Clases (
    id_clase INT AUTO_INCREMENT PRIMARY KEY,
    nombre_clase VARCHAR(100) NOT NULL,
    horario VARCHAR(50),
    dia VARCHAR(50),
    id_profesor INT, -- Clave Foránea
    FOREIGN KEY (id_profesor) REFERENCES Profesores(id_profesor)
        ON DELETE SET NULL -- Si se elimina un profesor, las clases quedan sin profesor asignado (NULL)
        ON UPDATE CASCADE   -- Si cambia el ID del profesor, se actualiza en Clases
);

-- Tabla de Unión para Alumnos_Clases (Relación N:M entre Alumnos y Clases)
CREATE TABLE Alumnos_Clases (
    id_alumno INT,
    id_clase INT,
    PRIMARY KEY (id_alumno, id_clase), -- Clave Primaria Compuesta
    FOREIGN KEY (id_alumno) REFERENCES Alumnos(id_alumno)
        ON DELETE CASCADE -- Si se elimina un alumno, se elimina su registro en esta tabla
        ON UPDATE CASCADE,
    FOREIGN KEY (id_clase) REFERENCES Clases(id_clase)
        ON DELETE CASCADE -- Si se elimina una clase, se elimina su registro en esta tabla para ese alumno
        ON UPDATE CASCADE
);

-- Tabla: Pagos (Referencia a Alumnos y PeriodosCuota)
CREATE TABLE Pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL, -- Clave Foránea
    id_periodo INT NOT NULL, -- Clave Foránea
    fecha_pago DATE NOT NULL,
    monto_pagado DECIMAL(10, 2) NOT NULL,
    tipo_pago VARCHAR(50),
    es_pago_parcial BOOLEAN DEFAULT FALSE, -- Por defecto, no es parcial
    monto_recargo_aplicado DECIMAL(10, 2) DEFAULT 0.00, -- Por defecto, sin recargo
    FOREIGN KEY (id_alumno) REFERENCES Alumnos(id_alumno)
        ON DELETE RESTRICT -- No permitir eliminar un alumno si tiene pagos asociados
        ON UPDATE CASCADE,
    FOREIGN KEY (id_periodo) REFERENCES PeriodosCuota(id_periodo)
        ON DELETE RESTRICT -- No permitir eliminar un período si tiene pagos asociados
        ON UPDATE CASCADE
);

-- Tabla de Unión para EstadosDocumentosAlumno (Relación N:M entre Alumnos y DocumentosRequeridos con atributo extra)
CREATE TABLE EstadosDocumentosAlumno (
    id_alumno INT,
    id_documento INT,
    estado_entrega VARCHAR(50) NOT NULL, -- Ej. 'Pendiente', 'Entregado'
    fecha_entrega DATE, -- Opcional, solo si estado_entrega es 'Entregado'
    PRIMARY KEY (id_alumno, id_documento), -- Clave Primaria Compuesta
    FOREIGN KEY (id_alumno) REFERENCES Alumnos(id_alumno)
        ON DELETE CASCADE -- Si se elimina un alumno, se eliminan sus estados de documentos
        ON UPDATE CASCADE,
    FOREIGN KEY (id_documento) REFERENCES DocumentosRequeridos(id_documento)
        ON DELETE RESTRICT -- No permitir eliminar un tipo de documento si hay estados asociados
        ON UPDATE CASCADE
);

-- 4. Reactivar los checks de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;