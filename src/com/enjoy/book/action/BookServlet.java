package com.enjoy.book.action;

import com.alibaba.fastjson.JSON;
import com.enjoy.book.bean.Book;
import com.enjoy.book.biz.BookBiz;
import com.enjoy.book.util.DateHelper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/book.let")
public class BookServlet extends HttpServlet {
    BookBiz bookBiz = new BookBiz();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /**
     * /book.let?type=add   添加图书
     * /book.let?type=modifypre&id=xx   修改前准备
     * /book.let?type=modify    修改
     * /book.let?type=remove&id=xx  删除
     * /book.let?type=query&pageIndex = 1 ：分页查询（request：转发）
     * /book.let?type=detaills&id=xx    展示书籍详细信息
     * /book.let?type=doajax&name=xx    使用ajax查询图书名对应的图书信息
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();

        //验证用户是否登录
        HttpSession session = req.getSession();
        if(session.getAttribute("user")==null){
            out.println("<script>alert('请登录');parent.window.location.href='login.html';</script>");
            return;
        }

        String type = req.getParameter("type");
        switch (type){
            case "add" :
                try {
                    add(req, resp, out);
                } catch (FileUploadException e) {
                    e.printStackTrace();
                    resp.sendError(500, "文件上传失败");
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(500, e.getMessage() + "导致失败");
                }
                break;
            case "modifypre" :
                long bookId = Long.parseLong(req.getParameter("id"));
                Book book = bookBiz.getById(bookId);
                req.setAttribute("book", book);
                req.getRequestDispatcher("book_modify.jsp").forward(req,resp);
                break;
            case "modify" :
                try {
                    modify(req, resp, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "remove" :
                remove(req, resp, out);
                break;
            case "query" :
                query(req, resp, out);
                break;
            case "details" :
                details(req, resp, out);
                break;
            case "doajax" :
                String name = req.getParameter("name");
                System.out.println("servletBookName"+name);
                Book book1 = bookBiz.getByName(name);
                if(book1==null){
                    out.print("{}");//null json 对象
                }else{
                    out.print(JSON.toJSONString(book1));
                }
                break;
            default :
                resp.sendError(404);
                break;
        }
    }

    /**
     * 添加书籍
     * 1.enctype="multipart/form-data"    和以前不同
     *  获取表单元素 req.getParameter("name") : error
     *  2.文件上传：图片文件从浏览器端保存到服务器端（第三方 FileUpload+io）
     *  3.路径
     *      图片：
     *      H:\NUC\Study\网络自学内容\20220629_凤姐图书管理系统\图书管理系统-JavaWeb\01-数据库脚本\cover\文城.png  实际路径
     *      http://localhos:8888/mybook_explored_war/Images/cover/文城.png    虚拟路径（服务器）
     * @param req
     * @param resp
     * @param out
     */
    private void add(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        //构建磁盘工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        //设置大小
        factory.setSizeThreshold(1024*9);
        //临时仓库
        File file = new File("c:\\temp");
        if(!file.exists()){
            file.mkdir();//创建文件夹
        }
        factory.setRepository(file);

        //文件上传+表单数据
        ServletFileUpload fileUpload = new ServletFileUpload(factory);

        //将请求解析成一个个FileItem(文件+表单元素)
        List<FileItem> fileItems = fileUpload.parseRequest(req);

        //遍历FileItem
        Book book = new Book();
        for(FileItem item : fileItems){
            if(item.isFormField()){
                //元素名称和用户填写的值 name：文城
                String name = item.getFieldName();
                String value = item.getString("utf-8");//防止乱码
                switch(name){
                    case "typeId":
                        book.setTypeId(Long.parseLong(value));
                        break;
                    case "name":
                        book.setName(value);
                        break;
                    case "price":
                        book.setPrice(Double.parseDouble(value));
                        break;
                    case "desc":
                        book.setDesc(value);
                        break;
                    case "publish":
                        book.setPublish(value);
                        break;
                    case "author":
                        book.setAuthor(value);
                        break;
                    case "stock":
                        book.setStock(Long.parseLong(value));
                        break;
                    case "address":
                        book.setAddress(value);
                        break;
                }
            }else{
                //文件：图片的文件名 文城.png
                String fileName = item.getName();
                //避免文件替换：当前的系统的时间.png
                //获取后缀名.png
                String filterName = fileName.substring(fileName.lastIndexOf("."));
                //修改文件名
                fileName = DateHelper.getImageName()+filterName;
                //保存到哪里
                //虚拟路径：Images/cover/1-1.png
                //文件的读写：实际路径 D://xx 虚拟路径Images/cover对应的实际路径
                String path = req.getServletContext().getRealPath("/Images/cover");
                String filePath = path+"/"+fileName;
                //数据库表中的路径：Images/cover/1-1.png：相对项目的根目录的位置
                String dbPath = "Images/cover/" + fileName;
                book.setPic(dbPath);

                //保存文件
                item.write(new File(filePath));
            }
        }

        //将信息保存到数据库
        int count = bookBiz.add(book);

        if(count>0){
            out.println("<script>alert('添加书籍成功');location.href='book.let?type=query&pageIndex = 1';</script>");
        } else {
            out.println("<script>alert('添加书籍失败');location.href='book_add.jsp';</script>");

        }
    }

    /**
     * 查询
     * book.let?type=query&pageIndex = 1
     * 页数：biz
     * 当前页码：pageIndex=1
     * 存：request，转发
     * @param req
     * @param resp
     * @param out
     */
    private void query(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws ServletException, IOException {

        //获取信息（页数，页码，信息）
        int pageSize = 3;
        int pageCount = bookBiz.getPageCount(pageSize);
        int pageIndex = Integer.parseInt(req.getParameter("pageIndex"));
        if(pageIndex<1){
            pageIndex = 1;
        }
        if(pageIndex>pageCount){
            pageIndex = pageCount;
        }
        List<Book> books = bookBiz.getByPage(pageIndex, pageSize);
        //存
        req.setAttribute("pageCount", pageCount);
        req.setAttribute("books", books);

        //转发到jsp页面
        req.getRequestDispatcher("book_list.jsp?pageIndex="+pageIndex).forward(req, resp);
    }

    /**
     * 查看图书详情
     * @param req
     * @param resp
     * @param out
     */
    private void details(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws ServletException, IOException {
        //获取图书的编号
        long id = Long.parseLong(req.getParameter("id")) ;
        //根据编号获取图书对象
        Book book = bookBiz.getById(id);
        //将对象保存到req当中
        req.setAttribute("book", book);
        //转发到jsp页面
        req.getRequestDispatcher("book_details.jsp").forward(req, resp);
    }

    /**
     * 修改图书信息的方法
     * @param req
     * @param resp
     * @param out
     */
    private void modify(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        //构建磁盘工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        //设置大小
        factory.setSizeThreshold(1024*9);
        //临时仓库
        File file = new File("c:\\temp");
        if(!file.exists()){
            file.mkdir();//创建文件夹
        }
        factory.setRepository(file);

        //文件上传+表单数据
        ServletFileUpload fileUpload = new ServletFileUpload(factory);

        //将请求解析成一个个FileItem(文件+表单元素)
        List<FileItem> fileItems = fileUpload.parseRequest(req);

        //遍历FileItem
        Book book = new Book();
        for(FileItem item : fileItems){
            if(item.isFormField()){
                //元素名称和用户填写的值 name：文城
                String name = item.getFieldName();
                String value = item.getString("utf-8");//防止乱码
                switch(name){
                    case "id" :
                        book.setId(Long.parseLong(value));
                        break;
                    case "pic" :
                        book.setPic(value);
                        break;
                    case "typeId":
                        book.setTypeId(Long.parseLong(value));
                        break;
                    case "name":
                        book.setName(value);
                        break;
                    case "price":
                        book.setPrice(Double.parseDouble(value));
                        break;
                    case "desc":
                        book.setDesc(value);
                        break;
                    case "publish":
                        book.setPublish(value);
                        break;
                    case "author":
                        book.setAuthor(value);
                        break;
                    case "stock":
                        book.setStock(Long.parseLong(value));
                        break;
                    case "address":
                        book.setAddress(value);
                        break;
                }
            }else{
                //文件：图片的文件名 文城.png      用户不选择图片时：fileName的数据为""
                String fileName = item.getName();
                //避免文件替换：当前的系统的时间.png
                if(fileName.trim().length()>0) {
                    //获取后缀名.png
                    String filterName = fileName.substring(fileName.lastIndexOf("."));
                    //修改文件名
                    fileName = DateHelper.getImageName()+filterName;
                    //保存到哪里
                    //虚拟路径：Images/cover/1-1.png
                    //文件的读写：实际路径 D://xx 虚拟路径Images/cover对应的实际路径
                    String path = req.getServletContext().getRealPath("/Images/cover");
                    String filePath = path+"/"+fileName;
                    //数据库表中的路径：Images/cover/1-1.png：相对项目的根目录的位置
                    String dbPath = "Images/cover/" + fileName;
                    book.setPic(dbPath);

                    //保存文件
                    item.write(new File(filePath));
                }
            }
        }

        //将信息保存到数据库
        int count = bookBiz.modify(book);

        if(count>0){
            out.println("<script>alert('修改书籍成功');location.href='book.let?type=query&pageIndex=1';</script>");
        } else {
            out.println("<script>alert('修改书籍失败');location.href='book.let?type=query&pageIndex=1';</script>");

        }
    }

    private void remove(HttpServletRequest req, HttpServletResponse resp, PrintWriter out){
        //获取删除的bookid
        long id = Long.parseLong(req.getParameter("id"));
        //调用biz删除方法
        try {
            int count = bookBiz.remove(id);
            if(count>0){
                out.println("<script>alert('删除成功');location.href='book.let?type=query&pageIndex=1'</script>");
            } else {
                out.println("<script>alert('删除失败');location.href='book.let?type=query&pageIndex=1'</script>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<script>alert('"+e.getMessage()+"');location.href='book.let?type=query&pageIndex=1'</script>");
        }

        //提示+跳转=>(查询的servlet)
    }


}
