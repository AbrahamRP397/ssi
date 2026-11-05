-- ==========================================================
-- SSI — Sistema de Segunda Instancia
-- Esquema para MySQL
-- ==========================================================

-- ----------------------------------------------------------
-- USUARIOS
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
                                     id_user BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    phone VARCHAR(10) NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('Magistrado','Administrador') NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    must_change_password TINYINT NOT NULL DEFAULT 0,
    temporary_password_expiration TIMESTAMP NULL,
    last_access_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- ----------------------------------------------------------
-- TOKENS DE RECUPERACIÓN
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_tokens (
                                                     id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     id_user BIGINT NOT NULL,
                                                     token VARCHAR(100) NOT NULL UNIQUE,
    expiration TIMESTAMP NOT NULL,
    used TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE
    );

-- ----------------------------------------------------------
-- DISTRITOS
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS districts (
                                         id_district BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(150) NOT NULL,
    head_city VARCHAR(150) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1
    );

-- ----------------------------------------------------------
-- SALAS
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS rooms (
                                     id_room BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     id_district BIGINT NOT NULL,
                                     name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_district FOREIGN KEY (id_district) REFERENCES districts(id_district)
    );

-- ----------------------------------------------------------
-- MAGISTRADOS
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS magistrates (
                                           id_magistrate BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           id_user BIGINT NOT NULL UNIQUE,
                                           id_room BIGINT NULL,
                                           status TINYINT NOT NULL DEFAULT 1,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           CONSTRAINT fk_mag_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE,
    CONSTRAINT fk_mag_room FOREIGN KEY (id_room) REFERENCES rooms(id_room)
    );

-- ----------------------------------------------------------
-- TOCAS
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS tocas (
                                     id_toca BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     toca_number VARCHAR(40) NOT NULL UNIQUE,
    subject VARCHAR(50) NOT NULL,
    original_case_number VARCHAR(60) NOT NULL,
    appellant VARCHAR(200) NULL,
    id_room BIGINT NOT NULL,
    id_magistrate BIGINT NULL,
    status ENUM('Nueva','En ponencia','Resuelto') NOT NULL DEFAULT 'Nueva',
    reception_date DATE NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_toca_room FOREIGN KEY (id_room) REFERENCES rooms(id_room),
    CONSTRAINT fk_toca_mag FOREIGN KEY (id_magistrate) REFERENCES magistrates(id_magistrate)
    );

-- ----------------------------------------------------------
-- DOCUMENTOS
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS documents (
                                         id_document BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         id_toca BIGINT NOT NULL,
                                         title VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    pages INT NULL,
    size_bytes BIGINT NULL,
    file_url VARCHAR(500) NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_toca FOREIGN KEY (id_toca) REFERENCES tocas(id_toca) ON DELETE CASCADE
    );

-- ----------------------------------------------------------
-- PONENCIAS
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ponencias (
                                         id_ponencia BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         id_toca BIGINT NOT NULL,
                                         id_magistrate BIGINT NOT NULL,
                                         session_date DATE NOT NULL,
                                         start_time TIME NOT NULL,
                                         end_time TIME NOT NULL,
                                         kind VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pon_toca FOREIGN KEY (id_toca) REFERENCES tocas(id_toca) ON DELETE CASCADE,
    CONSTRAINT fk_pon_mag FOREIGN KEY (id_magistrate) REFERENCES magistrates(id_magistrate)
    );

-- ----------------------------------------------------------
-- RESOLUCIONES
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS resolutions (
                                           id_resolution BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           id_toca BIGINT NOT NULL,
                                           id_magistrate BIGINT NOT NULL,
                                           outcome ENUM('Confirma','Revoca','Modifica') NOT NULL,
    body LONGTEXT,
    observations LONGTEXT,
    pdf_url VARCHAR(500) NULL,
    emitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_toca FOREIGN KEY (id_toca) REFERENCES tocas(id_toca) ON DELETE CASCADE,
    CONSTRAINT fk_res_mag FOREIGN KEY (id_magistrate) REFERENCES magistrates(id_magistrate)
    );

-- ----------------------------------------------------------
-- REASIGNACIONES
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS reassignments (
                                             id_reassignment BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             id_toca BIGINT NOT NULL,
                                             id_room_from BIGINT NOT NULL,
                                             id_room_to BIGINT NULL,
                                             reason VARCHAR(300) NOT NULL,
    status ENUM('Requiere validación','Aprobada','Rechazada') NOT NULL DEFAULT 'Requiere validación',
    requested_by BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_re_toca FOREIGN KEY (id_toca) REFERENCES tocas(id_toca) ON DELETE CASCADE,
    CONSTRAINT fk_re_from FOREIGN KEY (id_room_from) REFERENCES rooms(id_room),
    CONSTRAINT fk_re_to   FOREIGN KEY (id_room_to)   REFERENCES rooms(id_room),
    CONSTRAINT fk_re_user FOREIGN KEY (requested_by) REFERENCES users(id_user)
    );

-- ----------------------------------------------------------
-- HISTORIAL DE LA TOCA
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS toca_history (
                                            id_history BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            id_toca BIGINT NOT NULL,
                                            description VARCHAR(300) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_toca FOREIGN KEY (id_toca) REFERENCES tocas(id_toca) ON DELETE CASCADE
    );

-- ----------------------------------------------------------
-- BRANDING
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS branding (
                                        id_branding BIGINT DEFAULT 1 PRIMARY KEY,
                                        logo_url VARCHAR(500) NULL,
    primary_color VARCHAR(7) NOT NULL DEFAULT '#2e3b2b',
    accent_color VARCHAR(7) NOT NULL DEFAULT '#6f7857',
    updated_by BIGINT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_branding_row CHECK (id_branding = 1),
    CONSTRAINT fk_branding_user FOREIGN KEY (updated_by) REFERENCES users(id_user)
    );

-- ----------------------------------------------------------
-- AUDITORÍA
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_events (
                                            id_event BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            id_user BIGINT NULL,
                                            actor_name VARCHAR(150) NOT NULL,
    role VARCHAR(50) NOT NULL,
    action VARCHAR(200) NOT NULL,
    detail VARCHAR(500) NULL,
    tone ENUM('green','gold','blue','crimson') NOT NULL DEFAULT 'blue',
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE SET NULL
    );