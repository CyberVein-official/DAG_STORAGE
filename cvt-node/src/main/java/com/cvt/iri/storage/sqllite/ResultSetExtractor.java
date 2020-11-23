package com.cvt.iri.storage.sqllite;

import java.sql.ResultSet;

public interface ResultSetExtractor<T> {

    T extractData(ResultSet rs);

}
