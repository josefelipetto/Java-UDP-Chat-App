package Client;

import java.io.IOException;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;

public class Chat {

    private static String name;
    private static InetAddress ipAddress;
    private static int port;
    private static DatagramSocket clientSocket;

    public Chat(String line)
    {
        String[] args = line.split(",");

        name = args[0];

        try
        {
            ipAddress = InetAddress.getByName(args[1]);
            clientSocket = new DatagramSocket();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        port = Integer.parseInt(args[2]);

    }

    public void run()
    {

        Scanner scanner = new Scanner(System.in);

        String message;

        System.out.print("Digite uma mensagem: ");

        message = scanner.nextLine();

        try
        {
            sendMessage("MESSAGE:"+message);

            DatagramPacket datagramResponse = new DatagramPacket(new byte[65507], 65507);

            System.out.println("Esperando uma resposta...");
            clientSocket.receive(datagramResponse);

            String response = new String(
                    datagramResponse.getData(),
                    datagramResponse.getOffset(),
                    datagramResponse.getLength()
            );

            System.out.print("Resposta recebida: ");

            System.out.println(response);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String message) throws IOException
    {

        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(),message.length(),ipAddress,port);

        clientSocket.send(datagramPacket);
    }



}
