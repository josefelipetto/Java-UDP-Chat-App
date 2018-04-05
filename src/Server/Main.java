package Server;

import java.io.IOException;

public class Main {
    public static void main(String args[])
    {
        try
        {
            Thread supernode = new Thread(new Supernode());
            supernode.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
