-- 1. Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS taichi_academia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos recién creada
USE taichi_academia;

-- 2. Tabla Alumnos
CREATE TABLE IF NOT EXISTS alumnos (
    id_alumno INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL,
    telefono VARCHAR(50),
    email VARCHAR(100),
    fecha_nacimiento DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Tabla Profesores
CREATE TABLE IF NOT EXISTS profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL,
    telefono VARCHAR(50),
    email VARCHAR(100),
    fecha_contratacion DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. Tabla Clases
CREATE TABLE IF NOT EXISTS clases (
    id_clase INT AUTO_INCREMENT PRIMARY KEY,
    nombre_clase VARCHAR(100) NOT NULL,
    horario TIME NOT NULL, -- Hora de inicio de la clase
    dia_semana VARCHAR(20) NOT NULL, -- Ej: 'Lunes', 'Martes'
    id_profesor INT,
    capacidad_maxima INT NOT NULL DEFAULT 15,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE SET NULL
);

-- 5. Tabla Periodos_Cuota
CREATE TABLE IF NOT EXISTS periodos_cuota (
    id_periodo INT AUTO_INCREMENT PRIMARY KEY,
    nombre_periodo VARCHAR(50) UNIQUE NOT NULL, -- Ej: "Enero 2025"
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    monto_esperado DECIMAL(10, 2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_fechas CHECK (fecha_fin >= fecha_inicio)
);

-- 6. Tabla Pagos
CREATE TABLE IF NOT EXISTS pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    id_periodo INT NOT NULL,
    fecha_pago DATE NOT NULL,
    monto_pagado DECIMAL(10, 2) NOT NULL,
    metodo_pago VARCHAR(50), -- Ej: "Efectivo", "Transferencia", "Tarjeta"
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id_alumno) ON DELETE CASCADE,
    FOREIGN KEY (id_periodo) REFERENCES periodos_cuota(id_periodo) ON DELETE RESTRICT -- No permitir borrar periodo si tiene pagos
);

-- 7. Tabla Documentos_Estado
CREATE TABLE IF NOT EXISTS documentos_estado (
    id_documento INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    tipo_documento VARCHAR(100) NOT NULL, -- Ej: "Ficha Médica", "Autorización", "DNI Copia"
    fecha_presentacion DATE,
    presentado BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id_alumno) ON DELETE CASCADE
);

-- 8. Tabla Roles_Usuario
CREATE TABLE IF NOT EXISTS roles_usuario (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL -- Ej: "ADMINISTRADOR", "EMPLEADO"
);

-- Insertar roles básicos (si no existen)
INSERT IGNORE INTO roles_usuario (nombre_rol) VALUES ('DIRECTOR');
INSERT IGNORE INTO roles_usuario (nombre_rol) VALUES ('SECRETARIO');

-- 9. Tabla Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL, -- Para almacenar el hash BCrypt
    id_rol INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES roles_usuario(id_rol) ON DELETE RESTRICT
);

-- 10. Tabla Inscripciones_Clase
CREATE TABLE IF NOT EXISTS inscripciones_clase (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    id_clase INT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id_alumno) ON DELETE CASCADE,
    FOREIGN KEY (id_clase) REFERENCES clases(id_clase) ON DELETE CASCADE,
    UNIQUE (id_alumno, id_clase) -- Un alumno solo puede estar inscrito una vez a una clase específica
);

-- 11. Tabla Asistencia
CREATE TABLE IF NOT EXISTS asistencia (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    id_clase INT NOT NULL,
    fecha_asistencia DATE NOT NULL,
    presente BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id_alumno) ON DELETE CASCADE,
    FOREIGN KEY (id_clase) REFERENCES clases(id_clase) ON DELETE CASCADE,
    UNIQUE (id_alumno, id_clase, fecha_asistencia) -- Un alumno solo puede tener un registro de asistencia por clase y fecha
);