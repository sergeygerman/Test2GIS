package german.test2gis.settings;

public class OracleDBSettings {
    private String host;
    private Integer port;
    private String sid;
    private String username;
    private String password;

    public OracleDBSettings(String host, Integer port, String sid, String username, String password){
        this.host = host;
        this.port = port;
        this.sid = sid;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSid() {
        return sid;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
