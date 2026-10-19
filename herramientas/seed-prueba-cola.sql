-- ============================================================================
-- Datos de prueba para disparar el cron productor de donaciones.
--
-- Objetivo: dejar UNA donación segmentada en estado 'LISTA_PARA_ENTREGAR'
-- asociada a una entidad beneficiaria CON dirección válida, que es lo que el
-- cron (DonacionesListasParaEntregarCron) necesita para publicar un lote en la
-- cola 'logistica.planificar.queue'.
--
-- Ejecutar en el cliente de BD conectado a la base del servicio de donaciones
-- (TiDB: gateway01...tidbcloud.com:4000 / donatrack), con el SERVICIO APAGADO
-- para no competir con Hibernate.
--
-- IMPORTANTE: asume que las tablas ya existen (el servicio se levantó al menos
-- una vez con ddl-auto=update/create y generó el esquema). Si no existen,
-- levantá el servicio una vez y luego corré este script.
--
-- Nota sobre IDs: se usan IDs altos y fijos (9001, 9002, ...) para no chocar
-- con los autogenerados. Si ya existieran, borralos antes (ver bloque final).
-- ============================================================================

-- 1) Cadena geográfica: Pais -> Provincia -> Ciudad
INSERT INTO paises (id_pais, nombre) VALUES (9001, 'Argentina');

INSERT INTO provincias (id_provincia, nombre, id_pais) VALUES (9001, 'Buenos Aires', 9001);

INSERT INTO ciudades (id_ciudad, nombre, id_provincia) VALUES (9001, 'La Plata', 9001);

-- 2) Dirección (calle1 NO puede ser vacío: el cron descarta las que no tienen
--    texto de dirección resoluble)
INSERT INTO direcciones (id_direccion, calle1, calle2, altura, sin_altura, piso, cuerpo, departamento, id_ciudad)
VALUES (9001, 'Calle 7', NULL, 1234, FALSE, 0, 0, NULL, 9001);

-- 3) Persona (tabla padre de la herencia JOINED) + PersonaJuridica (tabla hija)
--    El id de la persona jurídica es el que se usa como entidad_beneficiaria_asignada_id.
INSERT INTO personas (id_persona, id_direccion, fecha_ultima_interaccion, tipo_notificador)
VALUES (9001, 9001, NULL, 'EMAIL');

INSERT INTO personas_juridicas (id_persona, razon_social, tipo_organizacion, cuit, rubro)
VALUES (9001, 'Comedor Los Pibes', 'ONG', '30712345679', 'Asistencia social');

-- 4) Entidad beneficiaria (apunta a la persona jurídica)
INSERT INTO entidades_beneficiarias (id_entidad_beneficiaria, id_persona_juridica)
VALUES (9001, 9001);

-- 5) SubCategoria (la segmentada la usa para obtener la unidad)
INSERT INTO subcategorias (id_subcategoria, categoria_bien, descripcion, unidad)
VALUES (9001, 'ALIMENTOS', 'Fideos', 'KG');

-- 6) Donación padre (el repositorio recorre findAll() de donaciones y navega
--    sus segmentadas; por eso la segmentada debe colgar de una donación)
INSERT INTO donaciones (id_donacion, id_donante, descripcion, fecha_ingreso)
VALUES (9001, NULL, 'Donacion de prueba (seed cola)', NOW());

-- 7) Donación segmentada en estado LISTA_PARA_ENTREGAR, asignada a la entidad
--    (entidad_beneficiaria_asignada_id = id de la persona juridica = 9001)
INSERT INTO donaciones_segmentadas (
    id_donacion_segmentada, cantidad, id_subcategoria, estado_donacion,
    donante_id, entidad_beneficiaria_asignada_id, id_comprobante, id_donacion
) VALUES (
    9001, 10, 9001, 'LISTA_PARA_ENTREGAR',
    NULL, 9001, NULL, 9001
);

-- 8) Al menos un bien para esa segmentada (el cron usa bienes.size()).
--    Bien es herencia JOINED: fila en 'bienes' (padre) + fila en la hija.
--    Usamos BienDuradero (tabla 'bienes_duraderos').
INSERT INTO bienes (id_bien, nombre, descripcion, foto, id_subcategoria, id_donacion, id_donacion_segmentada)
VALUES (9001, 'Paquete de fideos', '500g', NULL, 9001, 9001, 9001);

INSERT INTO bienes_duraderos (id_bien, estado_bien) VALUES (9001, 'NUEVO');

-- ============================================================================
-- Verificación: debería devolver 1 fila
-- ============================================================================
-- SELECT ds.id_donacion_segmentada, ds.estado_donacion, ds.entidad_beneficiaria_asignada_id,
--        d.getDireccion AS dummy
-- FROM donaciones_segmentadas ds
-- WHERE ds.estado_donacion = 'LISTA_PARA_ENTREGAR';

-- ============================================================================
-- LIMPIEZA (ejecutar para borrar los datos de prueba cuando termines)
-- ============================================================================
-- DELETE FROM bienes_duraderos WHERE id_bien = 9001;
-- DELETE FROM bienes WHERE id_bien = 9001;
-- DELETE FROM donaciones_segmentadas WHERE id_donacion_segmentada = 9001;
-- DELETE FROM donaciones WHERE id_donacion = 9001;
-- DELETE FROM subcategorias WHERE id_subcategoria = 9001;
-- DELETE FROM entidades_beneficiarias WHERE id_entidad_beneficiaria = 9001;
-- DELETE FROM personas_juridicas WHERE id_persona = 9001;
-- DELETE FROM personas WHERE id_persona = 9001;
-- DELETE FROM direcciones WHERE id_direccion = 9001;
-- DELETE FROM ciudades WHERE id_ciudad = 9001;
-- DELETE FROM provincias WHERE id_provincia = 9001;
-- DELETE FROM paises WHERE id_pais = 9001;
