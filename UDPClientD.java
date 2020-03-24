import java.io.ByteArrayInputStream;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the client instance of the host on which this application
 * is running
 * @author deepson
 */
public class UDPClientD
{
    private List<Client> network;
    private InetAddress server_address;
    DatagramSocket socket;
    DatagramPacket incomingPacket;
        
    /**
     * UDPClient constructor
     * @param add server IP address passed from the user input
     */
    public UDPClientD(InetAddress add)
    {
        server_address = add;
        network = new ArrayList<>();
    }
        
    /**
     * Set server address
     * @param add new server's address
     */
    public void setServerAddress(InetAddress add)
    {
        server_address = add;
    }
    
    /**
     * Executes client
     * @return list of clients in this client's array list that is useful when 
     *      upgrading to server in the driver class
     */
    public List<Client> executeClient() 
    {
        Random rand = new Random();
        
        boolean hasConnectionEstablished = false;
        while(true)
        {
            // Send the first message as soon as possible i.e 0 seconds
            int random_int = 0;
            if (hasConnectionEstablished == true)
            {
                // Send the later messages randomly in 0-30 seconds
                random_int = rand.nextInt(30000);
            }
            
            try 
            {
                Thread.sleep(random_int);
                
                try{
                    // Send hello message from the client to the server
                    socket = new DatagramSocket(); 
                    InetAddress address = server_address;
                    String message = "Hello from Client";
                    byte[] sendMessage = message.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, address, 1234);
                    socket.send(sendPacket);
                    System.out.println("\nMessage sent from Client");
                    
                    // Maximum wait time until figuring out the server is out is 30 seconds
                    Calendar wait_from_server_till = Calendar.getInstance();
                    wait_from_server_till.add(Calendar.SECOND,30);
                    
                    byte[] incomingData = new byte[1024];
                    incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                    
                    socket.setSoTimeout(30000);
                    
                    // If timeout of 30 seconds occured while waiting for server response,find
                    // out the first client in the arraylist
                    if(receiveUnntilTimeout()==0)
                    {
                        if(network.size()==0)
                        {
                            continue;
                        }
                        // Return to the driver class to be upgraded to server if I'm the first client in list
                        if(network.get(0).getIP().equals(InetAddress.getLocalHost()))
                        {
                            // remove myself from the array list because I'm going to become the server
                            remove_myself();
                            return network;
                        }
                        else{
                            // Change server address to the address of the first client in the list
                            setServerAddress(network.get(0).getIP());
                        }
    
                        continue;                                      
                    }
                    
                    // 30 second timeout didn't occur and receiver came back with data from the
                    // server
                    hasConnectionEstablished = true;
                    byte[] data = incomingPacket.getData();
                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    ObjectInputStream is = new ObjectInputStream(in);
                    try {
                        Packet pkt = (Packet)is.readObject();
                        System.out.println(pkt.getMsg());    
                        pkt.print_and_remove_nonresponding_Clients();
                        
                        System.out.println("Active clients:-");
                        pkt.printClientArray();
                        
                        // Save the received client array information to this client's own List
                        network = pkt.getClientArray();  
                    } 
                    catch (ClassNotFoundException e) 
                    {
                        System.out.println("Problem finding the packet class");
                        e.printStackTrace();
                    }
                }
                catch(SocketException z)
                {
                    System.out.println("Problem with socket");
                    z.printStackTrace();
                }   
                catch(IOException i)
                {
                    i.printStackTrace();
                }     
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    } //end of execute Client method
    
    /**
     * Continues receiving packet until timeout
     * @return integer indicating timeout or not
     */
    public int receiveUnntilTimeout()
    {
        try{
            socket.receive(incomingPacket);
        }
        catch (SocketTimeoutException e){
           System.out.println("Did not hear back from server for 30 seconds");
           return 0;
        } catch (IOException ex) {
            Logger.getLogger(UDPServerD.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return 1; //Still not finished 30 second cycle
    }
    
    public void remove_myself()
    {
        int i = 0;
        boolean has_found = false;
        while((!has_found) && (i < network.size()))
        {
            try {
                if(network.get(i).getIP().equals(InetAddress.getLocalHost()))
                {
                    has_found = true;
                    network.remove(i);    
                }
            } 
            catch (UnknownHostException ex) 
            {
                Logger.getLogger(UDPClientD.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }
    }
    
}
        
        
    
