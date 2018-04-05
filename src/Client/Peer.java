package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Peer {

    private static DatagramSocket clientSocket;
    private static InetAddress address;
    private static String response;
    private static MulticastSocket multicastSocket;
    private static InetAddress grupo;
    private static final int PORT = 35501;
    private static final int groupPort = 35000;
    private static List<Contato> contatos = new ArrayList<>();

    static class MessageListener implements Runnable
    {
        @Override
        public void run() {
            while (true)
            {
                try
                {
                    DatagramPacket datagramResponse = new DatagramPacket(new byte[65507], 65507);

                    clientSocket.receive(datagramResponse);

                    response = new String(
                            datagramResponse.getData(),
                            datagramResponse.getOffset(),
                            datagramResponse.getLength()
                    );

                    String[] args = response.split(":");

                    switch (args[0])
                    {
                        case "MESSAGE" :
                            System.out.println(args[1]);
                            break;
                        case "LIST" :
                            String[] list = args[1].split(";");
                            for(String listItem : list)
                            {
                                if(!contatos.add(new Contato(listItem)))
                                {
                                    System.out.println("Erro ao criar lista de contatos");
                                }
                            }
                            System.out.println("Lista de contato recebida e parseada com sucesso!");
                            break;
                    }

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    static class GroupMessageListener implements Runnable
    {
        @Override
        public void run() {
            try
            {
                multicastSocket.joinGroup(grupo);
                while (true)
                {
                    DatagramPacket groupMessage = new DatagramPacket(new byte[65507], 65507);

                    System.out.println("Aguardando mensagem");

                    multicastSocket.receive(groupMessage);

                    byte[] mensagemRecebida = new byte[groupMessage.getLength()];

                    System.arraycopy(groupMessage.getData(),0,mensagemRecebida,0,mensagemRecebida.length);

                    String response = new String(groupMessage.getData(),groupMessage.getOffset(),groupMessage.getLength());

                    String[] args = response.split(":");

                    switch (args[0])
                    {
                        case "LOGIN":
                            break;
                        case "LOGOUT":
                            break;
                        // @todo : Continuar daqui
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        try
        {
            connect();

            Thread listener = new Thread( new MessageListener() );
            Thread groupListener = new Thread(new GroupMessageListener() );

            listener.start();
            groupListener.start();

            readCommand();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void connect() throws IOException
    {
        grupo = InetAddress.getByName("224.0.0.1");

        clientSocket = new DatagramSocket();

        address = InetAddress.getLocalHost();

        multicastSocket = new MulticastSocket(groupPort);
    }

    private static void readCommand() throws IOException
    {
        Scanner scanner = new Scanner(System.in);

        String command;

        while (true)
        {
            System.out.print("Digite um comando: ");

            command = scanner.nextLine();

            sendMessage(command);

        }
    }

    private static void sendMessage(String message) throws IOException
    {
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(),message.length(),address,PORT);
        clientSocket.send(datagramPacket);
    }

}
