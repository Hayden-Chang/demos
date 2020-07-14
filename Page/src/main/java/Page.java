import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象
 * @author Administrator
 */
public class Page<T> {
    /**
     * 页码
     */
    private int pageno;

    /**
     * 每页显示的记录条数
     */
    private int pagesize;

    /**
     * 数据集合（需要显示在网页中的数据）
     */
    private List<T> dataList;

    /**
     * 总记录条数
     */
    private int totalsize;


    public Page(String pageno) {
        this.pageno = (pageno == null ? 1 : Integer.parseInt(pageno));
        this.pagesize = Const.PAGE_SIZE;
        this.dataList = new ArrayList<T>();
    }

    public int getPageno(){
        return pageno;
    }

    public int getPagesize(){
        return pagesize;
    }

    public List<T> getDataList(){
        return dataList;
    }

    public void setTotalsize(int totalsize){
        this.totalsize = totalsize;
    }

    public int getTotalsize(){
        return totalsize;
    }

    public int getPagecount(){
        return totalsize%pagesize == 0 ? totalsize/pagesize : totalsize/pagesize + 1;
    }

    /**
     * 通过业务SQL语句获取分页SQL语句
     * @param sql 业务SQL
     * @return 分页SQL语句
     * 这是非常核心的，通过多次嵌套，嵌套出分页sql语句的编写
     */
    public String getSql(String sql){
        return "select t1.* from (select t.*,rownum as linenum from ("+sql+") t where rownum<=" + pageno*pagesize + ") t1 where t1.linenum>" + (pageno-1)*pagesize;
    }
}
