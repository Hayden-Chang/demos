import com.github.pagehelper.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态参数查询，难度最大的是SQL语句动态拼接。（因为查询提交内容不定，查询提交个数不定）
 * @author Administrator
 */
public class PageQueryInvServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //解决请求体的中文乱码问题
        //request.setCharacterEncoding("GB18030");

        //创建分页对象
        Page<Investor> page = new Page<Investor>(request.getParameter("pageno"));

        //获取查询提交的数据
        String invregnum = request.getParameter("invregnum");
        String invname = request.getParameter("invname");
        String startdate = request.getParameter("startdate");
        String enddate = request.getParameter("enddate");

        //拼接业务SQL，注意其中的技巧，where 1=1，另外这里使用StringBuilder提高拼接的效率
        StringBuilder sql = new StringBuilder("select i.invregnum,i.invname,i.regdate,u.username,i.cty from t_invest i join t_user u on i.usercode=u.usercode where 1=1");
        StringBuilder totalsizeSql = new StringBuilder("select count(*) as totalsize from t_invest i join t_user u on i.usercode=u.usercode where 1=1");
        //创建list集合用来绑定下标和内容，利用的list下标和值对应关系的特点
        List<String> paramList = new ArrayList<String>();

        //动态参数拼接动态SQL语句
        if(StringUtil.isNotEmpty(invregnum)){
            sql.append(" and i.invregnum = ?");
            totalsizeSql.append(" and i.invregnum = ?");
            paramList.add(invregnum);
        }

        if(StringUtil.isNotEmpty(invname)){
            sql.append(" and i.invname like ?");
            totalsizeSql.append(" and i.invname like ?");
            paramList.add("%" + invname + "%");
        }

        if(StringUtil.isNotEmpty(startdate)){
            sql.append(" and i.regdate >= ?");
            totalsizeSql.append(" and i.regdate >= ?");
            paramList.add(startdate);
        }

        if(StringUtil.isNotEmpty(enddate)){
            sql.append(" and i.regdate <= ?");
            totalsizeSql.append(" and i.regdate <= ?");
            paramList.add(enddate);
        }

        //调用获取分页SQL
        String pageSql = page.getSql(sql.toString());

        //连接数据库查询数据
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(pageSql);

            //给?赋值（重点），这里list的巧妙使用
            for(int i=0;i<paramList.size();i++){
                ps.setString(i+1, paramList.get(i));
            }

            //执行查询语句，返回查询结果集
            rs = ps.executeQuery();

            //遍历结果集，每遍历一次，封装Investor对象，将其添加到List集合中
            while(rs.next()){
                Investor inv = new Investor();
                inv.setInvregnum(rs.getString("invregnum"));
                inv.setInvname(rs.getString("invname"));
                inv.setRegdate(rs.getString("regdate"));
                inv.setUsername(rs.getString("username"));
                inv.setCty(rs.getString("cty"));

                page.getDataList().add(inv);
            }

            //查询总记录条数，并且设置到分页对象中
            ps = conn.prepareStatement(totalsizeSql.toString());

            //给?赋值
            for(int i=0;i<paramList.size();i++){
                ps.setString(i+1, paramList.get(i));
            }

            rs = ps.executeQuery();

            if(rs.next()){
                page.setTotalsize(rs.getInt("totalsize"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            DBUtil.close(conn, ps, rs);
        }

        //将分页对象存储到request范围中
        request.setAttribute("pageObj", page);

        //转发

    }

}

