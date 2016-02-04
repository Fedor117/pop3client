package model;

public class UserInfo {

    private String host;
    private String login;
    private String password;
    private String provider;

    public UserInfo() {
    }

    public UserInfo(String host, String login, String password, String provider) {
        this.host     = host;
        this.login    = login;
        this.password = password;
        this.provider = provider;
    }

    public String getHost() {
        return host;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getProvider() {
        return provider;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

}