USE TaiChi; 

-- Sentencia DELETE: Eliminar la asignación del Alumno 7 (Martín Ruiz) de la Clase 2 ('adults-III')
DELETE FROM Alumnos_Clases
WHERE id_alumno = 7 AND id_clase = 2;

SELECT
    id_alumno,
    id_clase
FROM
    Alumnos_Clases
WHERE
    id_alumno = 7 AND id_clase = 2;
    
-- Sentencia DELETE: Eliminar al Alumno 10 (Paula Torres)
DELETE FROM Alumnos
WHERE id_alumno = 10;

SELECT
    id_alumno,
    nombre_completo
FROM
    Alumnos
ORDER BY
    id_alumno; 
    
UPDATE Pagos
SET
    monto_pagado = 23000.00, -- Establecer el monto pagado al monto base de la cuota
    es_pago_parcial = FALSE, -- Marcar el pago como 'no parcial'
    monto_recargo_aplicado = 0.00 -- Asegurar que el recargo sea 0 (ya lo era en este caso, pero es explícito)
WHERE
    id_alumno = 5 -- Para el Alumno 5 (Diego Pérez)
    AND id_periodo = 2 -- Para el Período Abril 2025
    AND es_pago_parcial = TRUE; -- Asegurarse de que estamos actualizando el registro que era parcial
    
SELECT
    A.nombre_completo AS NombreAlumno,
    CASE
        -- Contar los períodos distintos para los que el alumno tiene algún pago
        WHEN (SELECT COUNT(DISTINCT id_periodo) FROM Pagos P WHERE P.id_alumno = A.id_alumno)
             =
             -- Contar el total de períodos registrados en la tabla PeriodosCuota
             (SELECT COUNT(*) FROM PeriodosCuota)
        THEN 'Sí Pagó Todas las Cuotas Registradas' -- Si los conteos son iguales
        ELSE 'No Pagó Todas las Cuotas Registradas' -- Si los conteos son diferentes
    END AS EstadoGeneralPagos
FROM
    Alumnos A
WHERE
    A.id_alumno = 5; -- Filtramos para el Alumno 5 (Diego Pérez)
