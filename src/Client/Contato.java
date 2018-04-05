package Client;

public class Contato {

    private String name;

    private String address;

    private int port;

    public Contato(String name, String address, int port)
    {
        this.name = name;

        this.address = address;

        this.port = port;
    }

    public Contato(String line)
    {
        String[] params = line.split(",");

        this.name = params[0];

        this.address = params[1];

        this.port = Integer.parseInt(params[2]);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }


    public String toString()
    {
        return this.name + "," + this.address + "," + Integer.toString(this.port) + ";\n";
    }
}
