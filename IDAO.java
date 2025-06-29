package taichi.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica para las operaciones básicas de acceso a datos (CRUD).
 * Define un contrato común para todas las entidades que pueden ser persistidas.
 * T representa el tipo de la entidad (ej., Usuario, Profesor, Alumno).
 * ID representa el tipo del identificador de la entidad (ej., Integer).
 *
 * Esta interfaz asegura que todos los DAOs de la aplicación de la institución de idiomas
 * sigan un patrón consistente para la interacción con la base de datos.
 */
public interface IDAO<T, ID> {

    /**
     * Crea un nuevo registro de la entidad en la base de datos.
     * @param entity El objeto de la entidad a crear.
     * @return La entidad creada, con su ID asignado si es generado por la BD.
     * @throws SQLException Si ocurre un error de base de datos durante la creación.
     */
    T crear(T entity) throws SQLException;

    /**
     * Obtiene una entidad por su identificador único.
     * @param id El identificador de la entidad.
     * @return La entidad encontrada, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos durante la consulta.
     */
    T obtenerPorId(ID id) throws SQLException;

    /**
     * Obtiene todas las entidades de la base de datos.
     * @return Una lista de todas las entidades. Puede estar vacía si no hay registros.
     * @throws SQLException Si ocurre un error de base de datos durante la consulta.
     */
    List<T> obtenerTodos() throws SQLException;

    /**
     * Actualiza un registro existente de la entidad en la base de datos.
     * @param entity El objeto de la entidad con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos durante la actualización.
     */
    boolean actualizar(T entity) throws SQLException;

    /**
     * Elimina un registro de la entidad por su identificador único.
     * @param id El identificador de la entidad a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos durante la eliminación.
     */
    boolean eliminar(ID id) throws SQLException;
}