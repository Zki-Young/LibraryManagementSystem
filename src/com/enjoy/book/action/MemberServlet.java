package com.enjoy.book.action;

import com.alibaba.fastjson.JSON;
import com.enjoy.book.bean.Member;
import com.enjoy.book.bean.MemberType;
import com.enjoy.book.bean.Record;
import com.enjoy.book.biz.MemberBiz;
import com.enjoy.book.biz.MemberTypeBiz;
import com.enjoy.book.biz.RecordBiz;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/member.let")
public class MemberServlet extends HttpServlet {

    MemberBiz memberBiz = new MemberBiz();
    MemberTypeBiz memberTypeBiz = new MemberTypeBiz();
    RecordBiz recordBiz = new RecordBiz();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    /**
     * member.let?type=addpre 添加准备（MemberTypes）
     * member.let?type=add
     * member.let?type=modifypre&id=xx    修改准备（MemberTypes， Member）
     * member.let?type=modify
     * member.let?type=remove&id=xx 删除
     * member.let?type=query
     * member.let?type=modifyrecharge 充值
     * member.let?type=doajax&idn=xx    通过ajax请求会员信息
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        //验证用户是否登录
        HttpSession session = request.getSession();
        if(session.getAttribute("user")==null){
            out.println("<script>alert('请登录');parent.window.location.href='login.html';</script>");
            return;
        }
        String type = request.getParameter("type");
        switch (type){
            case "addpre" :
                //获取所有的会员类型
                List<MemberType> memberTypes = memberTypeBiz.getAll();
                //存request
                request.setAttribute("memberTypes", memberTypes);
                //转发
                request.getRequestDispatcher("mem_add.jsp").forward(request, response);
                break;
            case "add" :
                String name = request.getParameter("name");
                String pwd = request.getParameter("pwd");
                long memberTypeId = Long.parseLong(request.getParameter("memberType"));
                double balance = Double.parseDouble(request.getParameter("balance"));
                String tel = request.getParameter("tel");
                String idNumber = request.getParameter("idNumber");

                Member member = new Member();
                member.setName(name);
                member.setPwd(pwd);
                member.setTypeId(memberTypeId);
                member.setBalance(balance);
                member.setTel(tel);
                member.setIdNumber(idNumber);
                if(memberBiz.add(member) > 0){
                    out.println("<script>alert('会员开卡成功');location.href='member.let?type=query'</script>");
                } else {
                    out.println("<script>alert('会员开卡失败');location.href='member.let?type=query'</script>");
                }

                break;
            case "modifypre" :
                //类型&会员信息
                long id = Long.parseLong(request.getParameter("id"));
                Member member1 = memberBiz.getById(id);
                //获取所有的会员类型
                List<MemberType> memberTypes2 = memberTypeBiz.getAll();

                request.setAttribute("member", member1);
                request.setAttribute("memberTypes", memberTypes2);

                request.getRequestDispatcher("mem_modify.jsp").forward(request, response);
                 break;
            case "modify" :
                long id2 = Long.parseLong(request.getParameter("id"));
                String name2 = request.getParameter("name");
                String pwd2 = request.getParameter("pwd");
                long memberTypeId2 = Long.parseLong(request.getParameter("memberType"));
                double balance2 = Double.parseDouble(request.getParameter("balance"));
                String tel2 = request.getParameter("tel");
                String idNumber2 = request.getParameter("idNumber");

                Member member2 = new Member();
                member2.setId(id2);
                member2.setName(name2);
                member2.setPwd(pwd2);
                member2.setTypeId(memberTypeId2);
                member2.setBalance(balance2);
                member2.setTel(tel2);
                member2.setIdNumber(idNumber2);

                System.out.println(member2);
                if(memberBiz.modify(member2) > 0){
                    out.println("<script>alert('会员修改成功');location.href='member.let?type=query'</script>");
                } else {
                    out.println("<script>alert('会员修改失败');location.href='member.let?type=query'</script>");
                }
                break;
            case "remove" :
                long memId = Long.parseLong(request.getParameter("id"));
                try {
                    if(memberBiz.remove(memId) > 0){
                        out.println("<script>alert('会员删除成功');location.href='member.let?type=query'</script>");
                    } else {
                        out.println("<script>alert('会员删除失败');location.href='member.let?type=query'</script>");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    out.println("<script>alert('"+e.getMessage()+"');location.href='member.let?type=query'</script>");
                }
                break;
            case "query" :
                List<Member> memberList = memberBiz.getAll();
                request.setAttribute("memberList", memberList);
                request.getRequestDispatcher("mem_list.jsp").forward(request, response);
                break;
            case "modifyrecharge" :
                //获取身份证号和金额
                String idNumber3 = request.getParameter("idNumber");
                Double amount = Double.parseDouble(request.getParameter("amount"));
                if(memberBiz.modifyBalance(idNumber3, amount) > 0){
                    out.println("<script>alert('会员充值成功');location.href='member.let?type=query'</script>");
                } else {
                    out.println("<script>alert('会员充值失败');location.href='member.let?type=query'</script>");
                }

                break;
            case "doajax" :
                //获取身份证号
                String idNum = request.getParameter("idn");
                //获取member对象
                Member member3 = memberBiz.getByIdNumber(idNum);
                //修改member的借书数量
                List<Record> records = recordBiz.getRecordsByMemberId(member3.getId());
                if(records.size()>0){
                    long size = member3.getType().getAmount() - records.size();
                    member3.getType().setAmount(size);
                }
                // member3-->json字符串
                String memberJson = JSON.toJSONString(member3);
                //响应客户端 注意：打印不能换行
                out.print(memberJson);
                break;
            default:
                response.sendError(404);
                break;
        }
    }
}
