package com.enjoy.book.dao;

import com.enjoy.book.bean.Type;
import com.enjoy.book.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class TypeDao {
    //构建Query Runner对象
    QueryRunner queryRunner = new QueryRunner();

    /**
     * 添加图书类型
     * 主键ID自增列
     * @param name
     * @param parentId
     * @return
     */
    public int add(String name, long parentId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "INSERT INTO type(name, parentId) VALUES(?, ?) ";
        int count = queryRunner.update(conn, sql, name, parentId);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 获取所有的类型
     * @return
     */
    public List<Type> getAll() throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from type ";
        List<Type> types = queryRunner.query(conn, sql, new BeanListHandler<Type>(Type.class));
        DBHelper.close(conn);
        return types;
    }

    /**
     * 根据类型编号获取类型对象
     * @param typeId
     * @return
     */
    public Type getById(long typeId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from type where id=?";
        Type type = queryRunner.query(conn, sql, new BeanHandler<Type>(Type.class), typeId);

        DBHelper.close(conn);
        return type;
    }

    /**
     * 修改图书类型
     * @param id 需要修改的类型编号
     * @param name
     * @param parentId
     * @return
     */
    public int modify(long id, String name, long parentId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "update type set name=?, parentId=? where id=? ";
        int count = queryRunner.update(conn, sql, name, parentId, id);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 根据id删除类型
     * @param id
     * @return
     */
    public int remove(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "delete from type where id=? ";
        int count = queryRunner.update(conn, sql, id);
        DBHelper.close(conn);
        return count;
    }

    public static void main(String[] args) {
        TypeDao typeDao = new TypeDao();

        try {
            List<Type> types = typeDao.getAll();
            System.out.println(types);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
