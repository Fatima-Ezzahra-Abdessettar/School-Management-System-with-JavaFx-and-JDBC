package com.ensa.v2school.sm.DAO;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CRUD<T,PK>  {
    T create(T t) throws SQLException;
    T update(T t) throws SQLException;
    T delete(T t) throws SQLException;
    Optional<T> get(PK pk) throws SQLException;
    List<T> getAll() throws SQLException;
}
