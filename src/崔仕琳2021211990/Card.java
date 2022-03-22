package 崔仕琳2021211990;

public class Card {
    private String id;          //卡号
    private String password;    //卡密码
    private double money;       //余额

    /**
     * 构造器1：卡号，密码，初始余额为0
     * @param id 设置卡号
     * @param password 设置密码
     */
    public Card(String id, String password) {

    }

    /**
     * 构造器2：卡号，密码，初始余额
     * @param id 设置卡号
     * @param password 设置密码
     * @param money 存入初始余额
     */
    public Card(String id, String password, double money) {

    }

    /**
     * 重写toString方法
     * @return 返回该卡相应信息（格式:id(money)）
     */
    @Override
    public String toString() {
        return null;
    }

    /**
     * 修改卡中金额
     * @param money 修改的金额
     * @return 修改后的卡中余额
     */
    public double changeMoney(double money) {
        return 0;
    }
}
