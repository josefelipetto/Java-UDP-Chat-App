package Client;

import java.awt.*;
import java.io.Console;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                                Contato toAdd = new Contato(listItem);

                                Contato contato = contatos.stream()
                                        .filter(a -> Objects.equals(a.getName(),toAdd.getName()))
                                        .findAny()
                                        .orElse(null);

                                if(contato != null)
                                {
                                    System.out.println("Usuário já está na lista");
                                    continue;
                                }

                                if(!contatos.add(toAdd))
                                {
                                    System.out.println("Erro ao adicionar usuário na lista de contatos");
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
                        case "LOGIN": // Alguem logou no sistema. Vamos acrescentar essa pessoa na nossa lista de contatos

                            Contato toAdd = new Contato(args[1]);

                            Contato contato = contatos.stream()
                                    .filter(a -> Objects.equals(a.getName(),toAdd.getName()))
                                    .findAny()
                                    .orElse(null);

                            if(contato != null)
                            {
                                System.out.println("Usuário já está na lista");
                                continue;
                            }

                            if(!contatos.add(toAdd))
                            {
                                System.out.println("Erro ao adicionar contato na lista de disponíves");
                            }

                            System.out.println("Usuário adicionado à lista de contatos");
                            break;
                        case "LOGOUT": // Alguem fez logout. Vamos retirar essa pessoa da nossa lista de contatos
                            contatos.remove(new Contato(args[1]));
                            break;

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

            if(command.equals("printList"))
            {
                System.out.println(getUserList());
                continue;
            }

            String[] args = command.split(",");

            if(args[0].equals("talkTo"))
            {
                Thread chat = new Thread( new Chat(args[1]+","+args[2]+","+args[3]) );
                chat.start();
            }

            sendMessage(command);

        }
    }

    private static void sendMessage(String message) throws IOException
    {
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(),message.length(),address,PORT);
        clientSocket.send(datagramPacket);
    }


    private static String getUserList()
    {
        String list = "";

        for(Contato contato : contatos)
        {
            list = list.concat(contato.toString());
        }

        return list;
    }

}
