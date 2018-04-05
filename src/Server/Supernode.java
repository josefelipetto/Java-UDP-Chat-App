package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Supernode implements Runnable{

    private List<Usuario> usuarios = new ArrayList<>();

    private MulticastSocket multicastSocket;

    private DatagramSocket datagramSocket;

    private InetAddress grupo;

    private static final int groupPort = 35000;

    private static final int serverPort = 35501;

    public Supernode() throws IOException
    {
        multicastSocket = new MulticastSocket(groupPort);

        datagramSocket = new DatagramSocket(serverPort);

        grupo = InetAddress.getByName("224.0.0.1");
    }

    @Override
    public void run()
    {

        while (true)
        {
            try
            {
                DatagramPacket clientMessage = this.listen();

                String[] args = this.parseCommand(clientMessage);

                switch (args[0])
                {
                    case "create" :
                        if( ! this.createUser(args[1],args[2], clientMessage) )
                        {
                            send("MESSAGE:ERROR",clientMessage);
                            continue;
                        }
                        send("MESSAGE:Usuario criado com sucesso",clientMessage);

                        break;
                    case "login" :

                        if(! this.login(args[1],args[2], clientMessage))
                        {
                            send("MESSAGE:Erro ao fazer o login",clientMessage);
                            continue;
                        }

                        send("LIST:"+this.getUserList(),clientMessage);

                        sendGroupMessage("LOGIN:" + args[1]+","+clientMessage.getAddress().toString()+","+Integer.toString(clientMessage.getPort()));

                        break;
                    case "logout" :
                        if( ! this.logout(args[1]))
                        {
                            send("MESSAGE:Erro ao fazer o logout",clientMessage);
                            continue;
                        }
                        send("MESSAGE:Usuario deslogado com sucesso", clientMessage);
                        sendGroupMessage("LOGOUT:"+ args[1]+","+clientMessage.getAddress().toString()+","+Integer.toString(clientMessage.getPort()));
                        break;
                    default:
                        send("Comando nao reconhecido",clientMessage);
                        System.out.println("Comando não reconhecido");
                        break;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void sendGroupMessage(String message) throws IOException
    {

        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(),message.length(), this.grupo, groupPort);

        multicastSocket.send(datagramPacket);

    }

    private DatagramPacket listen() throws IOException
    {
        byte[] buffer = new byte[65507];

        System.out.println("Começou a escuta");

        DatagramPacket clientMessage = new DatagramPacket(buffer, buffer.length);

        datagramSocket.receive(clientMessage);

        return clientMessage;
    }

    private String[] parseCommand(DatagramPacket datagramPacket)
    {

        String command = new String(
                datagramPacket.getData(),
                datagramPacket.getOffset(),
                datagramPacket.getLength()
        );

        return command.split(",");
    }

    private boolean createUser(String name, String password, DatagramPacket datagramPacket)
    {
        if(usuarios.size() > 0)
        {
            Usuario usuario = usuarios.stream()
                    .filter(a -> Objects.equals(a.getName(), name))
                    .findAny()
                    .orElse(null);

            if(usuario != null)
            {
                return false;
            }
        }

        usuarios.add( new Usuario(name, password, datagramPacket.getAddress().toString(), datagramPacket.getPort() ) );
        return true;
    }

    private boolean login(String name, String password, DatagramPacket datagramPacket)
    {
        Usuario usuario = usuarios.stream()
                .filter(a -> Objects.equals(a.getName(), name))
                .findFirst()
                .orElse(null);

        if( (usuario != null) )
        {
            if( usuario.compare(password) && ! usuario.isOnline() )
            {
                usuario.setStatus(true);
                usuario.setIp(datagramPacket.getAddress().toString());
                usuario.setPort(datagramPacket.getPort());
                return true;
            }
        }

        return false;
    }

    private boolean logout(String name)
    {
        Usuario usuario = usuarios.stream()
                .filter(a -> Objects.equals(a.getName(), name))
                .findFirst()
                .orElse(null);

        if(usuario == null)
        {
            return false;
        }

        usuario.setStatus(false);

        return true;
    }

    private void send(String message, DatagramPacket datagramPacket)
    {
        byte[] buffer = message.getBytes();

        DatagramPacket messageToUser = new DatagramPacket(
                buffer,
                buffer.length,
                datagramPacket.getAddress(),
                datagramPacket.getPort()
        );

        try
        {
            datagramSocket.send(messageToUser);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private String getUserList()
    {
        String list = "";

        for(Usuario usuario : usuarios)
        {
            if(usuario.isOnline())
                list = list.concat(usuario.toString());
        }

        return list;
    }
}
