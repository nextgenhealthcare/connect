package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.data.DonkeyDaoException;

public class PostgresqlDaoFactory extends JdbcDaoFactory {
    private final static String COMMAND = "SET LOCAL synchronous_commit TO OFF; COMMIT;";
    private final static int REQUIRED_MAJOR_VERSION = 8;
    private final static int REQUIRED_MINOR_VERSION = 3;

    private Boolean supported;
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public JdbcDao getDao() {
        JdbcDao dao = super.getDao();
        dao.setAsyncCommitCommand(getAsyncCommitCommand(dao));
        return dao;
    }

    private String getAsyncCommitCommand(JdbcDao dao) {
        if (supported == null) {
            try {
                DatabaseMetaData metaData = dao.getConnection().getMetaData();
                int majorVersion = metaData.getDatabaseMajorVersion();
                int minorVersion = metaData.getDatabaseMinorVersion();

                if (majorVersion > REQUIRED_MAJOR_VERSION || (majorVersion == REQUIRED_MAJOR_VERSION && minorVersion >= REQUIRED_MINOR_VERSION)) {
                    supported = true;
                    logger.debug("Detected support for asynchronous commits in PostgreSQL");
                } else {
                    supported = false;
                    logger.debug("Asynchronous commits are not supported by the current PostgreSQL connection");
                }
            } catch (SQLException e) {
                throw new DonkeyDaoException(e);
            }
        }

        return (supported) ? COMMAND : null;
    }
}
