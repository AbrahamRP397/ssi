-- ==========================================================
-- SEED para MySQL
-- ==========================================================

-- ---------- USUARIOS ----------
INSERT IGNORE INTO users (id_user, name, surname, lastname, phone, email, password, role, status, must_change_password)
VALUES
(1, 'Laura',     'Castañeda', 'Ríos',  '7771000000', 'admin@tsjmorelos.gob.mx',                 '$2a$10$w8N0IYF4lGkxp7dWzC3J5.3KZbPbS7lK1iCwCwv4mGxTz0V1M5B0W', 'Administrador', 1, 0),
(2, 'Alejandro', 'Ramírez',   'Lozano','7771000001', 'alejandro.ramirez@tsjmorelos.gob.mx',     '$2a$10$w8N0IYF4lGkxp7dWzC3J5.3KZbPbS7lK1iCwCwv4mGxTz0V1M5B0W', 'Magistrado',    1, 0),
(3, 'Elena',     'Salgado',   'Ríos',  '7771000002', 'elena.salgado@tsjmorelos.gob.mx',         '$2a$10$w8N0IYF4lGkxp7dWzC3J5.3KZbPbS7lK1iCwCwv4mGxTz0V1M5B0W', 'Magistrado',    1, 0),
(4, 'Roberto',   'Vélez',     'Castro','7771000003', 'roberto.velez@tsjmorelos.gob.mx',         '$2a$10$w8N0IYF4lGkxp7dWzC3J5.3KZbPbS7lK1iCwCwv4mGxTz0V1M5B0W', 'Magistrado',    1, 0),
(5, 'Sofía',     'Cortés',    'Díaz',  '7771000004', 'sofia.cortes@tsjmorelos.gob.mx',          '$2a$10$w8N0IYF4lGkxp7dWzC3J5.3KZbPbS7lK1iCwCwv4mGxTz0V1M5B0W', 'Magistrado',    1, 0);

-- ---------- DISTRITOS ----------
INSERT IGNORE INTO districts (id_district, name, head_city, status) VALUES
(1, 'Primer Distrito Judicial',  'Cuernavaca',        1),
(2, 'Segundo Distrito Judicial', 'Jojutla',           1),
(3, 'Tercer Distrito Judicial',  'Cuautla',           1),
(4, 'Cuarto Distrito Judicial',  'Jojutla de Juárez', 1);

-- ---------- SALAS ----------
INSERT IGNORE INTO rooms (id_room, id_district, name, address, status) VALUES
(1,  1, 'Sala Primera',         'Av. Morelos Sur #100, Col. Centro, Cuernavaca', 1),
(2,  1, 'Sala Segunda',         'Av. Morelos Sur #102, Col. Centro, Cuernavaca', 1),
(3,  1, 'Sala Tercera',         'Av. Morelos Sur #104, Col. Centro, Cuernavaca', 1),
(4,  3, 'Sala Cuarta',          'Calle Juárez #50, Col. Centro, Jojutla', 1),
(5,  2, 'Sala Quinta',          'Av. Universidad #200, Col. Chamilpa, Cuernavaca', 0),
(6,  1, 'Sala Sexta',           'Av. Morelos Sur #106, Col. Centro, Cuernavaca', 1),
(7,  1, 'Sala Séptima',         'Av. Morelos Sur #108, Col. Centro, Cuernavaca', 1),
(8,  2, 'Sala Octava',          'Av. Universidad #202, Col. Chamilpa, Cuernavaca', 1),
(9,  3, 'Sala Novena',          'Calle Juárez #52, Col. Centro, Jojutla', 1),
(10, 3, 'Sala Décima',          'Calle Juárez #54, Col. Centro, Jojutla', 1),
(11, 2, 'Sala Décima Primera',  'Av. Universidad #204, Col. Chamilpa, Cuernavaca', 1);

-- ---------- MAGISTRADOS ----------
INSERT IGNORE INTO magistrates (id_magistrate, id_user, id_room, status) VALUES
(1, 2, 3, 1),
(2, 3, 1, 1),
(3, 4, 2, 1),
(4, 5, 4, 1);

-- ---------- TOCAS ----------
INSERT IGNORE INTO tocas (id_toca, toca_number, subject, original_case_number, appellant, id_room, id_magistrate, status, reception_date) VALUES
(1, 'SSI-03-0142/2025', 'Civil',     'CIV-142/2025', 'Grupo Constructor Morelos, S.A. de C.V.', 3, 1, 'Nueva',       '2025-06-18'),
(2, 'SSI-03-0138/2025', 'Familiar',  'FAM-311/2025', 'María Fernanda López Ruiz',               3, 1, 'En ponencia', '2025-06-16'),
(3, 'SSI-03-0129/2025', 'Mercantil', 'MER-087/2025', 'Comercializadora del Sur, S.A.',          3, 1, 'En ponencia', '2025-06-11'),
(4, 'SSI-03-0117/2025', 'Civil',     'CIV-117/2025', 'Inmobiliaria Cuernavaca, S.A.',           3, 1, 'Resuelto',    '2025-06-06'),
(5, 'SSI-03-0104/2025', 'Mercantil', 'MER-104/2025', 'Transportes Morelos, S.A.',               3, 1, 'Resuelto',    '2025-05-29');

-- ---------- DOCUMENTOS ----------
INSERT IGNORE INTO documents (id_document, id_toca, title, type, pages, size_bytes) VALUES
(1, 1, 'Demanda inicial',               'Demanda',   24, 5033164),
(2, 1, 'Acta de audiencia de pruebas',  'Audiencia', 18, 3355443),
(3, 1, 'Dictamen pericial contable',    'Peritaje',  32, 7444889),
(4, 1, 'Sentencia de primera instancia','Sentencia', 41, 9017753);

-- ---------- HISTORIAL TOCA ----------
INSERT IGNORE INTO toca_history (id_history, id_toca, description) VALUES
(1, 1, 'Recepción automática vía API'),
(2, 1, 'Sala Tercera validada y asignada al Magistrado'),
(3, 1, 'Expediente original consultado en modo lectura');

-- ---------- PONENCIAS ----------
INSERT IGNORE INTO ponencias (id_ponencia, id_toca, id_magistrate, session_date, start_time, end_time, kind) VALUES
(1, 2, 1, '2025-06-25', '10:00:00', '12:00:00', 'Ponencia 2'),
(2, 3, 1, '2025-06-26', '15:00:00', '17:00:00', 'Ponencia 1'),
(3, 1, 1, '2025-06-27', '09:00:00', '11:00:00', 'Ponencia 1');

-- ---------- RESOLUCIONES ----------
INSERT IGNORE INTO resolutions (id_resolution, id_toca, id_magistrate, outcome, body, pdf_url) VALUES
(1, 4, 1, 'Confirma', 'Se confirma la sentencia recurrida.',                    '/uploads/resolutions/SSI-03-0117-2025.pdf'),
(2, 5, 1, 'Modifica', 'Se modifica la sentencia en los términos expuestos.',   '/uploads/resolutions/SSI-03-0104-2025.pdf');

-- ---------- REASIGNACIONES ----------
INSERT IGNORE INTO reassignments (id_reassignment, id_toca, id_room_from, id_room_to, reason, status, requested_by) VALUES
(1, 1, 3, NULL, 'Licencia del magistrado', 'Requiere validación', 1);

-- ---------- BRANDING ----------
INSERT IGNORE INTO branding (id_branding, logo_url, primary_color, accent_color, updated_by) VALUES
(1, NULL, '#2e3b2b', '#6f7857', 1);

-- ---------- AUDITORÍA ----------
INSERT IGNORE INTO audit_events (id_event, id_user, actor_name, role, action, detail, tone, occurred_at) VALUES
(1, 1, 'Laura Castañeda',   'administrador', 'Actualizó una sala',     'Sala Quinta · cambió el estado a En configuración', 'blue',    '2025-06-25 09:18:00'),
(2, 2, 'Alejandro Ramírez', 'magistrado',    'Emitió una resolución',  'Toca SSI-03-0117/2025 · sentido: Confirma',        'green',   '2025-06-25 08:56:00'),
(3, 1, 'Laura Castañeda',   'administrador', 'Registró un magistrado', 'Mtra. Sofía Cortés Díaz · Sala Cuarta',            'gold',    '2025-06-24 16:34:00'),
(4, 2, 'Alejandro Ramírez', 'magistrado',    'Consultó un expediente', 'Toca SSI-03-0142/2025 · expediente CIV-142/2025',  'crimson', '2025-06-24 13:07:00');

-- Resetear AUTO_INCREMENT
ALTER TABLE users             AUTO_INCREMENT = 100;
ALTER TABLE districts         AUTO_INCREMENT = 100;
ALTER TABLE rooms             AUTO_INCREMENT = 100;
ALTER TABLE magistrates       AUTO_INCREMENT = 100;
ALTER TABLE tocas             AUTO_INCREMENT = 100;
ALTER TABLE documents         AUTO_INCREMENT = 100;
ALTER TABLE toca_history      AUTO_INCREMENT = 100;
ALTER TABLE ponencias         AUTO_INCREMENT = 100;
ALTER TABLE resolutions       AUTO_INCREMENT = 100;
ALTER TABLE reassignments     AUTO_INCREMENT = 100;
ALTER TABLE audit_events      AUTO_INCREMENT = 100;