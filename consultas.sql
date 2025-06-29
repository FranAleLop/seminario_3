USE TaiChi; 

SELECT
    id_alumno,
    nombre_completo
FROM
    Alumnos
ORDER BY
    id_alumno;
    
SELECT
    A.id_alumno,
    A.nombre_completo
FROM
    Alumnos A
LEFT JOIN Pagos PM -- Unir con pagos para Marzo 2025
    ON A.id_alumno = PM.id_alumno
    AND PM.id_periodo = (SELECT id_periodo FROM PeriodosCuota WHERE nombre_periodo = 'Marzo 2025')
LEFT JOIN Pagos PA -- Unir con pagos para Abril 2025
    ON A.id_alumno = PA.id_alumno
    AND PA.id_periodo = (SELECT id_periodo FROM PeriodosCuota WHERE nombre_periodo = 'Abril 2025')
WHERE
    PM.id_pago IS NULL -- No tienen pago registrado para Marzo 2025
    AND PA.id_pago IS NULL; -- Y no tienen pago registrado para Abril 2025
    
SELECT
    A.id_alumno,
    A.nombre_completo
FROM
    Alumnos A
JOIN Alumnos_Clases AC ON A.id_alumno = AC.id_alumno -- Unir Alumnos con la tabla de unión
JOIN Clases C ON AC.id_clase = C.id_clase         -- Unir la tabla de unión con Clases
WHERE
    C.nombre_clase = 'juniors-II'; -- Filtrar por el nombre de la clase
    
SELECT DISTINCT -- DISTINCT porque un alumno puede deber más de un documento, queremos listarlo una sola vez
    A.id_alumno,
    A.nombre_completo
FROM
    Alumnos A
JOIN EstadosDocumentosAlumno EDA ON A.id_alumno = EDA.id_alumno -- Unir Alumnos con la tabla de estados
WHERE
    EDA.estado_entrega = 'Pendiente'; -- Filtrar por estado 'Pendiente'
    
SELECT
    tipo_pago AS MetodoPago,
    COUNT(*) AS CantidadPagos
FROM
    Pagos
GROUP BY
    tipo_pago;
    
SELECT
    P.nombre_completo AS NombreProfesor,
    COUNT(DISTINCT AC.id_alumno) AS CantidadAlumnosAsignados
FROM
    Profesores P
LEFT JOIN Clases C ON P.id_profesor = C.id_profesor -- Une profesores con sus clases (incluye prof sin clases)
LEFT JOIN Alumnos_Clases AC ON C.id_clase = AC.id_clase -- Une clases con asignaciones de alumnos (incluye clases sin alumnos)
GROUP BY
    P.id_profesor, P.nombre_completo -- Agrupa por profesor
ORDER BY
    P.nombre_completo;
    
SELECT DISTINCT
    A.id_alumno,
    A.nombre_completo
FROM
    Alumnos A
JOIN Pagos PM ON A.id_alumno = PM.id_alumno -- Une con pagos para encontrar pagos de Marzo
JOIN Pagos PA ON A.id_alumno = PA.id_alumno -- Une con pagos para encontrar pagos de Abril
JOIN PeriodosCuota PerM ON PM.id_periodo = PerM.id_periodo AND PerM.nombre_periodo = 'Marzo 2025' -- Filtra solo pagos de Marzo
JOIN PeriodosCuota PerA ON PA.id_periodo = PerA.id_periodo AND PerA.nombre_periodo = 'Abril 2025'; -- Filtra solo pagos de Abril

-- Consulta: Nombre de los alumnos que pagaron con recargo y el período correspondiente
SELECT
    A.nombre_completo AS NombreAlumno,
    PC.nombre_periodo AS PeriodoPagoConRecargo
FROM
    Alumnos A -- Tabla de Alumnos para obtener el nombre
JOIN
    Pagos P ON A.id_alumno = P.id_alumno -- Unir con la tabla Pagos para ver los registros de pago
JOIN
    PeriodosCuota PC ON P.id_periodo = PC.id_periodo -- Unir con la tabla PeriodosCuota para obtener el nombre del período
WHERE
    P.monto_recargo_aplicado > 0; -- Filtrar solo los pagos donde el monto del recargo es mayor a 0
    
SELECT
    COUNT(*) AS CantidadPagosConRecargo -- Cuenta todas las filas que cumplen la condición
FROM
    Pagos -- De la tabla de Pagos
WHERE
    monto_recargo_aplicado > 0; -- Donde el monto del recargo aplicado es mayor a 0