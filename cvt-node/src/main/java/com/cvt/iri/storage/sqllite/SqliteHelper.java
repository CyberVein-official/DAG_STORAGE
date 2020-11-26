package com.cvt.iri.storage.sqllite;

import com.cvt.iri.controllers.TransactionViewModel;
import com.cvt.iri.model.Hash;
import com.cvt.iri.model.Transaction;
import com.cvt.iri.storage.CvtPersistable;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * sqlite帮助类，直接创建该类示例，并调用相应的借口即可对sqlite数据库进行操作
 * <p>
 * 本类基于 sqlite jdbc v56
 *
 * @author haoqipeng
 */
abstract public class SqliteHelper {
    final static Logger logger = LoggerFactory.getLogger(SqliteHelper.class);

    public static final String TABLE_NAME_TRANSACTION = "t_transaction";
    public static final String TABLE_NAME_BALANCE = "t_balance";

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;
    private static String dbFilePath;

    synchronized public static void init(String dbFilePath) {
        try {
            File dir = new File(dbFilePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            SqliteHelper.dbFilePath = dbFilePath + File.separator + "cvt.db";
            connection = getConnection();
            String tableCheckSql = "select count(*) from sqlite_master where type = 'table' and name = '" + TABLE_NAME_TRANSACTION + "'";
            Long count = executeQuery(tableCheckSql,
                    rs -> {
                        try {
                            return rs.getLong(1);
                        } catch (SQLException e) {
                            logger.error("查询表失败", e);
                            return 0L;
                        }
                    });
            if (null == count || count == 0) {
                executeUpdate("CREATE TABLE `t_transaction` (\n" +
                        "  `hash` VARCHAR(100) NOT NULL,\n" +
                        "  `address` VARCHAR(100) NOT NULL,\n" +
                        "  `value` BIGINT(20) NOT NULL,\n" +
                        "  `timestamp` BIGINT(20) NOT NULL)");
                executeUpdate("CREATE TABLE `t_balance` (\n" +
                        "  `address` VARCHAR(100) NOT NULL,\n" +
                        "  `value` BIGINT(20) NOT NULL,\n" +
                        "  `timestamp` BIGINT(20) NOT NULL)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Sqlite Initialize Error!", e);
        }
    }

    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) {
            connection = getConnection(dbFilePath);
        }
        return connection;
    }

    /**
     * 获取数据库连接
     *
     * @param dbFilePath db文件路径
     * @return 数据库连接
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Connection conn;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        return conn;
    }

    /**
     * 执行sql查询
     *
     * @param sql sql select 语句
     * @param rse 结果集处理类对象
     * @return 查询结果
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static <T> T executeQuery(String sql, ResultSetExtractor<T> rse) throws SQLException, ClassNotFoundException {
        try {
            resultSet = getStatement().executeQuery(sql);
            T rs = rse.extractData(resultSet);
            return rs;
        } finally {
            destroyed();
        }
    }

    /**
     * 执行select查询，返回结果列表
     *
     * @param sql sql select 语句
     * @param rm  结果集的行数据处理类对象
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static <T> List<T> executeQuery(String sql, RowMapper<T> rm) throws SQLException, ClassNotFoundException {
        List<T> rsList = new ArrayList<T>();
        try {
            resultSet = getStatement().executeQuery(sql);
            while (resultSet.next()) {
                rsList.add(rm.mapRow(resultSet, resultSet.getRow()));
            }
        } finally {
            destroyed();
        }
        return rsList;
    }

    /**
     * 执行数据库更新sql语句
     *
     * @param sql
     * @return 更新行数
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static int executeUpdate(String sql) throws SQLException, ClassNotFoundException {
        try {
            int c = getStatement().executeUpdate(sql);
            return c;
        } finally {
            destroyed();
        }

    }

    /**
     * 执行多个sql更新语句
     *
     * @param sqls
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void executeUpdate(String... sqls) throws SQLException, ClassNotFoundException {
        try {
            for (String sql : sqls) {
                getStatement().executeUpdate(sql);
            }
        } finally {
            destroyed();
        }
    }

    /**
     * 执行数据库更新 sql List
     *
     * @param sqls sql列表
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void executeUpdate(List<String> sqls) throws SQLException, ClassNotFoundException {
        try {
            for (String sql : sqls) {
                getStatement().executeUpdate(sql);
            }
        } finally {
            destroyed();
        }
    }

    private static Statement getStatement() throws SQLException, ClassNotFoundException {
        if (null == statement) statement = getConnection().createStatement();
        return statement;
    }

    /**
     * 数据库资源关闭和释放
     */
    public static void destroyed() {
        try {
            if (null != statement) {
                statement.close();
                statement = null;
            }

            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != resultSet) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            logger.error("Sqlite数据库关闭时异常", e);
        }
    }

    /**
     * 执行select查询，返回结果列表
     *
     * @param sql   sql select 语句
     * @param clazz 实体泛型
     * @return 实体集合
     * @throws SQLException           异常信息
     * @throws ClassNotFoundException 异常信息
     */
    public static <T> List<T> executeQueryList(String sql, Class<T> clazz) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<T> rsList = new ArrayList<T>();
        try {
            resultSet = getStatement().executeQuery(sql);
            while (resultSet.next()) {
                T t = clazz.newInstance();
                for (Field field : t.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = resultSet.getObject(field.getName());
                    if (field.getType().equals(Long.class) && value.getClass().equals(Integer.class)) {
                        value = Long.valueOf(value.toString());
                    }
                    field.set(t, value);
                }
                rsList.add(t);
            }
        } finally {
            destroyed();
        }
        return rsList;
    }

    /**
     * 执行sql查询,适用单条结果集
     *
     * @param sql   sql select 语句
     * @param clazz 结果集处理类对象
     * @return 查询结果
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static <T> T executeQuery(String sql, Class<T> clazz) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            resultSet = getStatement().executeQuery(sql);
            if (resultSet.next()) {
                T t = clazz.newInstance();
                for (Field field : t.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = resultSet.getObject(field.getName());
                    if (field.getType().equals(Long.class) && value.getClass().equals(Integer.class)) {
                        value = Long.valueOf(value.toString());
                    }
                    field.set(t, value);
                }
                return t;
            }
            return null;
        } finally {
            destroyed();
        }
    }

    /**
     * 执行数据库更新sql语句
     *
     * @param tableName 表名
     * @param param     key-value键值对,key:表中字段名,value:值
     * @return 更新行数
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static int executeInsert(String tableName, Map<String, Object> param) throws SQLException, ClassNotFoundException {
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("INSERT INTO ");
            sql.append(tableName);
            sql.append(" ( ");
            for (String key : param.keySet()) {
                sql.append(key);
                sql.append(",");
            }
            sql.delete(sql.length() - 1, sql.length());
            sql.append(")  VALUES ( ");
            for (String key : param.keySet()) {
                sql.append("'");
                sql.append(param.get(key));
                sql.append("',");
            }
            sql.delete(sql.length() - 1, sql.length());
            sql.append(");");
            int c = getStatement().executeUpdate(sql.toString());
            return c;
        } finally {
            destroyed();
        }

    }

    public static List<CvtPersistable> getTransactions() throws Exception {
        return executeQueryList("select * from " + TABLE_NAME_TRANSACTION, CvtPersistable.class);
    }

    public static CvtPersistable getTransaction(String address) throws Exception {
        return executeQuery("select * from " + TABLE_NAME_TRANSACTION
                + " where address='" + address + "'", CvtPersistable.class);
    }

    public static CvtPersistable getTransactionByHash(Hash hash) throws Exception {
        return executeQuery("select * from " + TABLE_NAME_TRANSACTION
                + " where hash='" + hash.toString() + "'", CvtPersistable.class);
    }

    public static List<CvtPersistable> getBalances() throws Exception {
        return executeQueryList("select * from " + TABLE_NAME_BALANCE, CvtPersistable.class);
    }

    public static CvtPersistable getBalance(String address) throws Exception {
        return executeQuery("select * from " + TABLE_NAME_BALANCE
                + " where address='" + address + "'", CvtPersistable.class);
    }

    synchronized public static void updateBalance(String address, Long value) throws Exception {
        executeUpdate("update " + TABLE_NAME_BALANCE
                + " set value='" + value + "'"
                + " where address='" + address + "'");
    }

    synchronized public static void saveBalance(CvtPersistable cvtPersistable) throws Exception {
        CvtPersistable exist = getBalance(cvtPersistable.getAddress());
        if (null == exist) {
            executeInsert(SqliteHelper.TABLE_NAME_BALANCE, cvtPersistable.toMap());
        } else {
            logger.error("一个地址只能有一个余额: {}", new Gson().toJson(exist));
        }
    }

    synchronized public static void saveTransaction(TransactionViewModel transactionViewModel) throws Exception {
        if (transactionViewModel.getHash().equals(Hash.NULL_HASH)) {
            logger.info("空交易，跳过");
            return;
        }
        Transaction transaction = transactionViewModel.getTransaction();
        if (null == transaction.address) {
            logger.info("地址为空，跳过");
            return;
        }
        saveTransaction(transactionViewModel.getHash(), new CvtPersistable(transaction.address.toString(), transaction.value, transaction.timestamp));
    }

    synchronized public static void saveTransaction(Hash hash, CvtPersistable cvtPersistable) throws Exception {
        if (null != getTransactionByHash(hash)) {
            logger.info("交易已经存在");
        }

        logger.info("保存交易信息：{}", new Gson().toJson(cvtPersistable));
        if (cvtPersistable.getValue() < 0) {
            CvtPersistable balance = SqliteHelper.getBalance(cvtPersistable.getAddress());
            if (null != balance) {
                SqliteHelper.updateBalance(cvtPersistable.getAddress(), balance.getValue() + cvtPersistable.getValue());
            }
        } else {
            saveBalance(cvtPersistable);
        }

        Map<String, Object> transactionMap = cvtPersistable.toMap();
        transactionMap.put("hash", hash);
        SqliteHelper.executeInsert(SqliteHelper.TABLE_NAME_TRANSACTION, transactionMap);
    }

    public static void printTransaction() throws Exception {
        List<CvtPersistable> transactionList = executeQueryList("select * from " + SqliteHelper.TABLE_NAME_TRANSACTION, CvtPersistable.class);
        logger.info("交易信息：{} ，详情如下：", transactionList.size());
        logger.info(StringUtils.center("CustomTransactionPersistable List", 80, "="));
        for (CvtPersistable transaction : transactionList) {
            logger.info(new Gson().toJson(transaction));
        }
        logger.info(StringUtils.center("CustomTransactionPersistable List", 80, "="));
    }

}
