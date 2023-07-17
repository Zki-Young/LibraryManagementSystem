package com.enjoy.book.dao;

import com.enjoy.book.bean.Record;
import com.enjoy.book.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;

public class RecordDao {

    QueryRunner queryRunner = new QueryRunner();

    public List<Record> getRecordByBookId(long bookId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where bookId=?";
        List<Record> records = queryRunner.query(conn, sql, new BeanListHandler<Record>(Record.class), bookId);
        DBHelper.close(conn);
        return records;
    }

    /**
     * 根据用户的身份证号查询用户借阅信息
     * @param idNum
     * @return
     */
    public List<Record> getRecordsByIdNum(String idNum) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where bookId=?";
        List<Record> records = queryRunner.query(conn, sql, new BeanListHandler<Record>(Record.class), idNum);
        DBHelper.close(conn);
        return records;
    }

    /**
     * 根据用户的会员编号查询用户借阅信息
     * @param memberId
     * @return
     */
    public List<Record> getRecordsByMemberId(long memberId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where memberId=? and backDate is null";
        List<Record> records = queryRunner.query(conn, sql, new BeanListHandler<Record>(Record.class), memberId);
        DBHelper.close(conn);
        return records;
    }

    /**
     * 添加借阅记录
     * @param memberId
     * @param bookId
     * @param deposit
     * @param userId
     * @return
     * @throws SQLException
     */
    public int add(long memberId, long bookId, double deposit, long userId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "INSERT into record VALUES(null, ?, ?, CURRENT_DATE, null, ?, ?, '978-7-302-12260-9') ";
        int count = queryRunner.update(conn, sql, memberId, bookId, deposit, userId);
        DBHelper.close(conn);
        return count;
    }

    /**
     *
     * @param deposit   押金：过期归还，>0      准时归还：清零
     * @param userId    管理员编号
     * @param id    记录编号
     * @return
     * @throws SQLException
     */
    public int modify(double deposit, long userId, long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "UPDATE record SET backDate=CURRENT_DATE, deposit = ?, userId = ? where id = ? ";
        int count = queryRunner.update(conn, sql, deposit, userId, id);
        DBHelper.close(conn);
        return count;
    }

    public int modify(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "UPDATE record SET rentDate=CURRENT_DATE where id = ? ";
        int count = queryRunner.update(conn, sql, id);
        DBHelper.close(conn);
        return count;
    }

    public Record getById(long recordId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from record where id = ?";
        Record record = queryRunner.query(conn, sql, new BeanHandler<Record>(Record.class), recordId);
        DBHelper.close(conn);
        return record;
    }

    /**
     *
     * @param typeId
     * 0：全部
     * 1：已归还
     * 2：未归还
     * 3：最近一周归还
     *
     * @param keyWork
     * @return
     * @throws SQLException
     */
    public List<Map<String, Object>> query(int typeId, String keyWord) throws SQLException {
        Connection conn = DBHelper.getConnection();
//        String sql = "SELECT * from recordView where 1=1 ";
        StringBuilder sb = new StringBuilder("SELECT * from recordView where 1=1 ");
        switch(typeId){
            case 0 :
                break;
            case 1 :
                sb.append(" and backDate is not null ");
                break;
            case 2 :
                sb.append(" and backDate is null ");
                break;
            case 3 :
                sb.append(" and backDate is null and returnDate < DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY)");
                break;
        }

        if(keyWord!=null){
            sb.append("and ( bookName like '%"+keyWord+"%' " +
                    " or memberName like '%"+keyWord+"%' " +
                    " or concat(rentDate, '') like '%"+keyWord+"%') ");

        }

        List<Map<String, Object>> date = queryRunner.query(conn, sb.toString(), new MapListHandler());
        DBHelper.close(conn);
        return date;

    }

    public static void main(String[] args) {
        RecordDao recordDao = new RecordDao();
        try {
            List<Map<String, Object>> date = recordDao.query(0, null);
            for (Map<String, Object> row:date) {
                for(String key:row.keySet()){
                    System.out.print(key+":"+row.get(key)+"\t");
                }
                System.out.println();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}
