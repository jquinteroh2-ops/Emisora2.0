-- =====================================================================
-- Proyecto : Emisora 2.0 (Ejercicio 25) - Desarrollo Web Unidad 2
-- Script 01: Creación de base de datos y tablas relacionales
-- Motores  : MySQL 8.0+ / MariaDB / PostgreSQL / H2
-- Autor    : Jose Antonio Quintero Herrera (7502510055)
-- =====================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS emisora_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE emisora_db;

DROP TABLE IF EXISTS emisoras;
DROP TABLE IF EXISTS usuarios;

-- ---------------------------------------------------------------------
-- Tabla usuarios: entidad Usuario con id, clave, nombre y rol
-- Adicionalmente email y tokens para recuperación de contraseña por correo.
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    rol VARCHAR(20) NOT NULL DEFAULT 'CONSULTA',
    reset_token VARCHAR(100) NULL,
    reset_token_expires DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'OPERADOR', 'CONSULTA'))
);

-- ---------------------------------------------------------------------
-- Tabla emisoras: entidad Emisora (Ejercicio 25)
-- Atributos: nombre, canal, bandaFm, bandaAm, numLocutores, genero,
-- horario, patrocinador, pais, descripcion, numProgramas, numCiudades.
-- Clave primaria: id, y código identificador único (codigo).
-- ---------------------------------------------------------------------
CREATE TABLE emisoras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    canal VARCHAR(100) NOT NULL,
    banda_fm DECIMAL(4,1) NULL,
    banda_am INT NULL,
    num_locutores INT NOT NULL DEFAULT 0,
    genero VARCHAR(50) NOT NULL,
    horario VARCHAR(100) NOT NULL,
    patrocinador VARCHAR(100) NULL,
    pais VARCHAR(60) NOT NULL,
    descripcion VARCHAR(500) NULL,
    num_programas INT NOT NULL DEFAULT 0,
    num_ciudades INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_emisoras_fm CHECK (banda_fm IS NULL OR banda_fm BETWEEN 87.0 AND 108.0),
    CONSTRAINT chk_emisoras_am CHECK (banda_am IS NULL OR banda_am BETWEEN 530 AND 1710),
    CONSTRAINT chk_emisoras_numeros CHECK (num_locutores >= 0 AND num_programas >= 0 AND num_ciudades >= 0)
);

-- Índices para optimizar las consultas y reportes parametrizados
CREATE INDEX idx_emisoras_pais ON emisoras (pais);
CREATE INDEX idx_emisoras_genero ON emisoras (genero);
CREATE INDEX idx_emisoras_cobertura ON emisoras (num_ciudades, num_locutores);
CREATE INDEX idx_usuarios_rol ON usuarios (rol);
CREATE INDEX idx_usuarios_created_at ON usuarios (created_at);
