package com.application.model.support;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

/**
 * Mapea la columna Postgres "text[]" a un campo String[] en las entidades JPA. Hibernate 6.2.x trae
 * un bug conocido en su ArrayJdbcType/ArrayJavaType genérico (falla con
 * "Could not convert 'java.lang.String' to 'java.lang.Class' ... to unwrap" al hacer el INSERT), así
 * que en vez de @JdbcTypeCode(SqlTypes.ARRAY) este UserType arma el java.sql.Array a mano vía JDBC
 * puro, igual que VectorType hace para la columna "vector(n)" de pgvector.
 */
public class TextArrayType implements UserType<String[]> {

    private static final String POSTGRES_ARRAY_TYPE_NAME = "text";

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Class<String[]> returnedClass() {
        return String[].class;
    }

    @Override
    public boolean equals(String[] x, String[] y) {
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(String[] x) {
        return Arrays.hashCode(x);
    }

    @Override
    public String[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Array array = rs.getArray(position);
        if (array == null) {
            return new String[0];
        }
        Object[] raw = (Object[]) array.getArray();
        String[] result = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            result[i] = raw[i] != null ? raw[i].toString() : null;
        }
        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, String[] value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.ARRAY);
        } else {
            Array array = st.getConnection().createArrayOf(POSTGRES_ARRAY_TYPE_NAME, value);
            st.setArray(index, array);
        }
    }

    @Override
    public String[] deepCopy(String[] value) {
        return value == null ? null : value.clone();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(String[] value) {
        return value == null ? null : value.clone();
    }

    @Override
    public String[] assemble(Serializable cached, Object owner) {
        return cached == null ? null : ((String[]) cached).clone();
    }
}
