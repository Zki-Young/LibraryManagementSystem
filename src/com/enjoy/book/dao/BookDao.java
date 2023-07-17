package com.enjoy.book.dao;

import com.enjoy.book.bean.Book;
import com.enjoy.book.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BookDao {
    QueryRunner queryRunner = new QueryRunner();

    /**
     * 根据类型查询对应的书籍类型
     * @param typeId
     * @return
     * @throws SQLException
     */
    public List<Book> getBooksByTypeId(long typeId) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book where typeId = ?";
        List<Book> books = queryRunner.query(conn, sql, new BeanListHandler<Book>(Book.class), typeId);
        DBHelper.close(conn);
        return books;
    }

    /**
     * 添加书籍
     * @param typeId
     * @param name
     * @param price
     * @param desc
     * @param pic
     * @param publish
     * @param author
     * @param stock
     * @param address
     * @return
     * @throws SQLException
     */
    public int add(long typeId, String name, double price, String desc, String pic, String publish,
                   String author, long stock, String address) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "insert into book(typeId, `name`, price, `desc`, pic, publish, author, stock, address) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ";
        int count = queryRunner.update(conn, sql, typeId, name, price, desc, pic, publish, author, stock, address);

        DBHelper.close(conn);
        return count;
    }

    /**
     * 修改书籍
     * @param id
     * @param typeId
     * @param name
     * @param price
     * @param des
     * @param pic
     * @param publish
     * @param author
     * @param stock
     * @param address
     * @return
     */
    public int modify(long id, long typeId, String name, double price, String desc, String pic, String publish,
                      String author, long stock, String address) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "UPDATE book set typeId=?, `name` = ?, price = ?, `desc` = ?, pic=?, " +
                "publish = ?, author=?, stock=?, address=? " +
                "WHERE id=? ";
        int count = queryRunner.update(conn, sql, typeId, name, price, desc, pic, publish, author, stock, address, id);

        DBHelper.close(conn);
        return count;
    }

    /**
     * 修改书籍的数量
     * @param id
     * @param amount 正数：加一本     负数：减一本
     * @return
     * @throws SQLException
     */
    public int modify(long id, int amount) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "UPDATE book set stock=stock+? WHERE id=? ";
        int count = queryRunner.update(conn, sql, amount, id);

        DBHelper.close(conn);
        return count;
    }

    public int remove(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "delete from book where id=?";
        int count = queryRunner.update(conn, sql, id);
        DBHelper.close(conn);
        return count;
    }

    /**
     * 分页查询（暂时不考虑排序）
     * @param pageIndex 表示第几页，从1开始
     * @param pageSize  每页多少行
     * @return  当前页的信息
     * @throws SQLException
     */
    public List<Book> getByPage(int pageIndex, int pageSize) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book limit ?,?";
        int offset = (pageIndex-1)*pageSize;
        List<Book> books = queryRunner.query(conn, sql, new BeanListHandler<Book>(Book.class), offset, pageSize);
        DBHelper.close(conn);
        return books;
    }

    public Book getById(long id) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book where id=?";
        Book book = queryRunner.query(conn, sql, new BeanHandler<Book>(Book.class), id);
        DBHelper.close(conn);
        return book;
    }

    /**
     * 获取书籍数量的方法
     * @return
     * @throws SQLException
     */
    public int getCount() throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select count(id) from book";
//        Object data = queryRunner.query(conn, sql, new ScalarHandler<>());
        Number data = queryRunner.query(conn, sql, new ScalarHandler<>());
//        System.out.println(data.getClass());
//        int count = (int)((long)data);
        int count = data.intValue();
        DBHelper.close(conn);
        return count;
    }

    /**
     * 根据书籍名称拆查询书籍信息
     * @param bookName
     * @return
     * @throws SQLException
     */
    public Book getByName(String bookName) throws SQLException {
        Connection conn = DBHelper.getConnection();
        String sql = "select * from book where name=?";
        Book book = queryRunner.query(conn, sql, new BeanHandler<Book>(Book.class), bookName);
        DBHelper.close(conn);
        System.out.println("daoBookName:"+bookName);
        System.out.println("daoSql:"+sql);
        System.out.println("dao:"+book);
        return book;
    }

    public static void main(String[] args) {
        BookDao bookDao = new BookDao();
        List<Book> books;
        try {
//            books = bookDao.getBooksByTypeId(2);
//            System.out.println(books);//[]:books对象是有的，但是没有数据
//            for(Book book : books){
//                System.out.println(book);
//            }
            bookDao.modify(1,-10);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
