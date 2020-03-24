/**
 * This HAC program implements High Availability Cluster in Client-Server model
 * @author Team3
 * @version 1.0
 */

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.net.UnknownHostException;

public class HAC {
	/**
         * This is the driver method.
	 * @param args
	 */
    public static void main(String[] args) {
		
        System.out.println("Are you a client or a server? \n"
                + "Enter \"C\" if you are a client and \"S\" if you are a server");
        Scanner keyboard = new Scanner(System.in);
        char input = keyboard.next().charAt(0);
        
        if(input == 'C' || input == 'c')
        {   
            List<Client> nettoserver = new ArrayList<>();
            System.out.println("Hey client, what is the server IP address you want to connect to?");
            Scanner k2 = new Scanner(System.in);
            String server_address = k2.nextLine();
            try{ 
                //if the application is run as client, server IP address is passed into client constructor
            	InetAddress add = InetAddress.getByName(server_address); 
            	UDPClientD c = new UDPClientD(add);
            	nettoserver = c.executeClient();
            }
            catch(UnknownHostException e)
            {
                System.out.println("Server not found!");
            }
            // Client is upgraded to server
            UDPServerD theServer = new UDPServerD(nettoserver);
            theServer.executeServer();
        }
        
        else if(input == 'S' || input == 's')
        {
            System.out.println("I am the server, I am receiving the data");
            UDPServerD theServer = new UDPServerD();
            theServer.executeServer();
        }
        keyboard.close();
    }
}
