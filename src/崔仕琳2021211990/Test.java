package 崔仕琳2021211990;

/**
 Account类记录账户
 Fields:
 private String hostName   账户用户名
 private String password   账户登陆密码
 private String IdNumber   账户人身份证号
 private List<Card> card   此账户下拥有的卡对象
 Constructors:
 public Account(String,String,String)      构造器1：用户名，密码，身份证号
 public Account(String,String)             构造器2：用户名，身份证号（密码默认为身份证号后六位）
 Methods:
 public void showAccount()              输出用户名，身份证号和所有卡号（格式:小明, 123456789123456789, [642554554241, 524525522, 525525, ...]）

 Card类记录卡
 Fields:
 private long cardId卡号
 private long password卡密码
 private double money余额
 Methods:
 public Card(long,long,double)建立卡
 public void changeMoney(double)改变余额
 public double getMoney()获取余额
 public long getCardId()获取卡号
 */
public class Test {

}
