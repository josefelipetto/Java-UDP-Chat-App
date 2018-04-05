package Server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Usuario implements iUsuario {
    private String name;

    private String password;

    private boolean status;

    private String ip;

    private int port;

    public Usuario(String name, String password, String ip, int port)
    {
        this.name = name;
        this.password = this.hash(password);
        this.ip = ip;
        this.port = port;
        this.status = false;
    }


    @Override
    public String toString()
    {
        return this.name + "," + this.ip + "," + Integer.toString(this.port) + ";";
    }


    public boolean compare(String typedPassword)
    {
        return this.hash(typedPassword).equals(this.password);
    }

    public String hash(String password)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(password.getBytes());
            return new String(messageDigest.digest());
        }
        catch (NoSuchAlgorithmException exception)
        {
            exception.printStackTrace();
            return "";
        }
    }


    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public boolean isOnline() {
        return status;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
