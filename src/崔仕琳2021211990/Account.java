package 崔仕琳2021211990;

import java.util.List;

public class Account {

    private String hostName;    //账户用户名
    private String password;    //账户登陆密码
    private String idNumber;    //账户人身份证号
    private List<Card> cards;   //此账户下拥有的卡对象

    /**
     * 构造器1：用户名，密码，身份证号
     * @param hostName 用户名
     * @param password 密码
     * @param idNumber 身份证号
     */
    public Account(String hostName, String password, String idNumber) {
        this.hostName = hostName;
        this.password = password;
        this.idNumber = idNumber;
    }

    /**
     * 构造器2：用户名，身份证号（密码默认为身份证号后六位）
     * @param hostName 用户名
     * @param idNumber 身份证号
     */
    public Account(String hostName, String idNumber) {
        this.hostName = hostName;
        this.idNumber = idNumber.substring(12,18);
    }

    /**
     * 重写toString方法
     * @return 返回用户名，身份证号和所有卡号及金额（格式:小明, 123456789123456789, [642554554241(52.2), 524525522(535.0), 525525(0.0), ...]）
     */
    @Override
    public String toString() {
        StringBuilder a = new StringBuilder();
        for(int i=0 ; i< cards.size();i++)
        {
            Card b = cards.get(i);
            if(i==0)
            {
                a.append(b);
                a.append(',');
            }
            else
            {
                a.append(',');
                a.append(b);
            }
        }
        return hostName + idNumber + '[' + a + ']';
    }

    /**
     * 修改账户密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功返回ture，失败返回false
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if(!oldPassword.equals(newPassword))
        {
            this.password = newPassword;
            return true;
        }
        return false;
    }

    /**
     * 向用户中添加卡对象
     * @param card 需要添加的卡对象
     */
    public void addCard(Card card) {

        cards.add(card);
    }
}