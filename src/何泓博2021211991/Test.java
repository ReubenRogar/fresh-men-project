package 何泓博2021211991;

public class Test {
    /**
        Account类记录账户
        Fields:
        private String hostName持卡人姓名
        private String password账户密码
        private long telephone电话号码
        private Card card卡对象
        Methods:
        public Account(String,String,long,long,long,double)构造器1：自定义卡号，余额
        public Account(String,String,long,long,double)构造器2：随机卡号，自定义余额
        public Account(String,String,long,Card)构造器3：已有卡添加到账户
        public void changeMoney(double)改变余额
        public void showAccount()输出姓名，电话号码和卡号；

        Card类记录卡
        Fields:
        private long cardId卡号
        private long password卡密码
        private double money余额
        Methods:
        public Card(long,long,double)建立卡
        public changeMoney(double)改变余额
     */
}
