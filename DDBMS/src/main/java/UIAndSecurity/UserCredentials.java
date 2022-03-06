package UIAndSecurity;

public class UserCredentials {
    private String username = null;
    private String password = null;
    private String securityQuestion1 = null;
    private String securityQuestion2 = null;
    private String securityQuestion3 = null;
    private String securityAnswer1 = null;
    private String securityAnswer2 = null;
    private String securityAnswer3 = null;

    public UserCredentials(String username, String password, String sq1, String sa1, String sq2, String sa2, String sq3, String sa3){
        this.username=username;
        this.password = password;
        this.securityQuestion1 = sq1;
        this.securityQuestion2 = sq2;
        this.securityQuestion3 = sq3;
        this.securityAnswer1 = sa1;
        this.securityAnswer2 = sa2;
        this.securityAnswer3 = sa3;
    }

    public String getPassword() {
        return password;
    }

    public String getSecurityAnswer1() {
        return securityAnswer1;
    }

    public String getSecurityAnswer2() {
        return securityAnswer2;
    }

    public String getSecurityAnswer3() {
        return securityAnswer3;
    }

    public String getSecurityQuestion1() {
        return securityQuestion1;
    }

    public String getSecurityQuestion2() {
        return securityQuestion2;
    }

    public String getSecurityQuestion3() {
        return securityQuestion3;
    }

    public String getUsername() {
        return username;
    }
}
