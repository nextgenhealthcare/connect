package com.mirth.connect.donkey.server.data.jdbc;

public class MysqlDaoFactory extends JdbcDaoFactory {
    @Override
    public JdbcDao getDao() {
        JdbcDao dao = super.getDao();
        dao.setQuoteChar('`');
        return dao;
    }
}
