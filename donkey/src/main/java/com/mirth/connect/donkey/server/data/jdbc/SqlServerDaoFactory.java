package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;

import com.mirth.connect.donkey.server.data.DonkeyDaoException;

public class SqlServerDaoFactory extends JdbcDaoFactory {
    private Integer deadlockPriority = 8;

    public Integer getDeadlockPriority() {
        return deadlockPriority;
    }

    public void setDeadlockPriority(Integer deadlockPriority) {
        this.deadlockPriority = deadlockPriority;
    }

    @Override
    public JdbcDao getDao() {
        JdbcDao dao = super.getDao();

        if (deadlockPriority != null) {
            Statement statement = null;

            try {
                statement = dao.getConnection().createStatement();
                statement.executeUpdate("SET DEADLOCK_PRIORITY " + deadlockPriority);
            } catch (SQLException e) {
                throw new DonkeyDaoException(e);
            } finally {
                DbUtils.closeQuietly(statement);
            }
        }

        return dao;
    }
}
