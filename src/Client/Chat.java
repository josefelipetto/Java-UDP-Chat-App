package Client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Chat implements Runnable{

    private static String name;
    private static InetAddress ipAddress;
    private static int port;
    private static DatagramSocket clientSocket;

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

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

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
        Thread messageListener = new Thread(new MessageListener());
        messageListener.start();

        Scanner scanner = new Scanner(System.in);

        String message;

        while (true)
        {
            System.out.print("Digite um comando: ");

            message = scanner.nextLine();

            try
            {
                sendMessage(message);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void sendMessage(String message) throws IOException
    {

        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(),message.length(),ipAddress,port);

        clientSocket.send(datagramPacket);
    }



}
