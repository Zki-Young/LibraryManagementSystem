package com.enjoy.book.action;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

@WebServlet(urlPatterns = "/code.let", loadOnStartup = 1)
public class ValCodeServlet extends HttpServlet {
    Random random = new Random();

    //获取随机字符串
    private String getRandomStr(){
        String str="23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            char letter = str.charAt(random.nextInt(str.length()));
            sb.append(letter);
        }
        return sb.toString();
    }

    //获取背景色
    private Color getBackColor(){
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return new Color(red, green, blue);
    }

    //获取前景色
    private Color getForeColor(Color bgColor){
        int red = 255 - bgColor.getRed();
        int green = 255 - bgColor.getGreen();
        int blue = 255 - bgColor.getBlue();

        return new Color(red, green, blue);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //设置响应格式为图片：jpg
        resp.setContentType("image/jpeg");
        //图片对象
        BufferedImage bufferedImage = new BufferedImage(80, 30, BufferedImage.TYPE_INT_RGB);
        //获取画布对象
        Graphics g = bufferedImage.getGraphics();
        //设置背景颜色
        Color bgColor = getBackColor();
        g.setColor(bgColor);
        //画背景
        g.fillRect(0, 0, 80, 30);
        //设置前景色
        Color foreColor = getForeColor(bgColor);
        g.setColor(foreColor);
        //设置字体
        g.setFont(new Font("黑体", Font.BOLD, 26));
        //将随机字符串存到session
        String randomStr = getRandomStr();
        HttpSession session = req.getSession();
        session.setAttribute("code", randomStr);
        //将字符串画在画布上
        g.drawString(randomStr, 10, 28);//设置要画的字符和位置
        //画噪点
        for (int i = 0; i < 30; i++) {
            g.setColor(Color.white);
            int x = random.nextInt(80);
            int y = random.nextInt(30);
            g.fillRect(x,y,1,1);
        }
        //将这张内存的图片输出到响应流
        ServletOutputStream sos = resp.getOutputStream();
        ImageIO.write(bufferedImage, "jpeg", sos);
    }
}
