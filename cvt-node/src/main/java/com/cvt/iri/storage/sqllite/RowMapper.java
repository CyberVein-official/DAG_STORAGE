package com.cvt.iri.storage.sqllite;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    T mapRow(ResultSet rs, int index) throws SQLException;
}
