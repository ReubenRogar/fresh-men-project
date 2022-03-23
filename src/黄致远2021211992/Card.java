package 黄致远2021211992;
import java.util.Formatter;
public class Card {
    private String id;          //卡号
    private String password;    //卡密码
    private double money;       //余额（始终保留2位小数；若小数点后多于2位，从小数点后第3位开始直接截断，而不是四舍五入）

    /**
     * 构造器1：卡号，密码，初始余额为0.00  (money为保留两位小数)
     * @param id 设置卡号
     * @param password 设置密码
     */
    public Card(String id, String password) {
        this.id = id;
        this.password = password;
        this.money = 0;
    }

    /**
     * 构造器2：卡号，密码，初始余额  (money为保留两位小数;若小数点后多于2位,从小数点后第3位开始直接截断，而不是四舍五入)
     * @param id 设置卡号
     * @param password 设置密码
     * @param money 存入初始余额
     */
    public Card(String id, String password, double money) {
        this.id = id;
        this.password = password;
        this.money = Double.parseDouble(String.format("%.2f", money));
    }

    /**
     * 重写toString方法
     * @return 返回该卡相应信息（格式:id(money)） (money为保留两位小数)
     */
    @Override
    public String toString() {
        if(this.id == null) {
            return null;
        }
        else{
            return id+'('+money+')';
        }
    }

    /**
     * 修改卡中金额
     * @param money 修改的金额
     * @return 修改后的卡中余额 （截断为2位小数，不是四舍五入）
     */
    public double changeMoney(double money) {
        this.money = money;
        return money;
    }
}
