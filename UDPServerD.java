import java.net.*;
import java.util.Calendar;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the server instance of the device on which the application
 * is running.
 * @author deepson
 */
public class UDPServerD {
    private final List<Client> network;
    DatagramSocket socket;
    DatagramPacket receivePacket;
    
    /**
     * UDPServer constructor
     */
    public UDPServerD()
    {
        network = new ArrayList<>();
    }
    
    /**
     * UDPServer constructor
     * @param net network list passed when client upgrades to server
     */
    public UDPServerD(List<Client> net)
    {
        this.network = net;
    }
    
    /**
     * Executes the server
     */
    public void executeServer()
    {	
        try{
            socket = new DatagramSocket(1234);
            byte[] incomingData = new byte[1024];  
            
            // Calendar object that has current time
            Calendar wait_till = Calendar.getInstance();
            boolean is_first = true;
            
            while(true)
            {        
                if((!network.isEmpty())&& (is_first))
                {
                    setAllFalse();
                    System.out.println("first time");
                    is_first = false;
                    wait_till.add(Calendar.SECOND, 30);                  
                    
                }
                
                receivePacket = new DatagramPacket(incomingData, incomingData.length);    
                        
                if (network.isEmpty())
                {   
                    // Server listens for infinite time when the array list is empty
                    socket.setSoTimeout(0);
                    try{
                        print_network();
                        System.out.println("\n++++++++Server is listening +++++++++\n");
                        socket.receive(receivePacket);
                        
                        //Listens to the first client and adds to the arraylist
                        String message = new String(receivePacket.getData());
                        InetAddress address = receivePacket.getAddress();
			int port = receivePacket.getPort();
                        System.out.println("Received message: " + message);
                        System.out.println("Client IP: "+ address.getHostAddress());
                        System.out.println("Client port: "+ port);
                        //Creating newClient to add to arraylist
                        Client newClient = new Client(address, port);
                        network.add(newClient);
                        
                        // Generate a response and pack it into a packet
                        String response = "Thank you for the message ----- Response sent from server"
                                + InetAddress.getLocalHost().getHostName();
                        Packet sendPkt = new Packet(network, response);
                        
                        // Write the packet into an output stream and send it to the first sender
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(sendPkt);
                        byte[] sendData = outputStream.toByteArray();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                        socket.send(sendPacket); 
                        setAllFalse();  //Reset if response was received
                        
                        // After sending the packet wait for at most 30 seconds
                        wait_till = Calendar.getInstance();
                        wait_till.add(Calendar.SECOND, 30);   //Maximum wait time until next message
                    }
                    catch(IOException e)
                    {
                        System.out.println("There has been an exception");
                        e.printStackTrace();
                    }
                }  
                
                else{
                    // Network is not empty
                    // While response has not been received from all the clients
                    System.out.println("has all been received?: " + is_all_received());
                    while(!is_all_received())
                    {
                        
                        // The 30 second countdown was on as soon as response was sent
                        long times_out = wait_till.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                        socket.setSoTimeout((int)times_out);    // Open the socket only for the remaining time to 30 seconds
                        
                        // 30 seconds waittime for receiving a data has passed, so go to send data
                        if(receiveUnntilTimeout()==0) {break;} 

                        // Received message
                        String message = new String(receivePacket.getData());
                        InetAddress address = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        System.out.println("Received message: " + message);
                        System.out.println("Client IP: "+ address.getHostAddress());
                        System.out.println("Client port: "+ port);
                            
                        // If the received client was not in the arraylist, add it
                        if(!isIPpresent(address, port))
                        {
                            network.add(new Client(address, port));
                        }                       
                    }            
                    
                    // Create a data packet with network info and response and send it to all active clients
                    String response = "Thank you for the message ----- Response sent from server"
                            + InetAddress.getLocalHost().getHostAddress();
                    Packet sendPkt = new Packet(network, response);    
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try{
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(sendPkt);
                        byte[] sendData = outputStream.toByteArray();
                        sendPackets(sendData);
                        System.out.println("Data packets sent to all the active clients");  
                        remove_non_responding_clients();
                        setAllFalse();
                    }
                    catch(IOException e){
                        System.out.println("There has been an exception");
                        e.printStackTrace();
                    }      
                    
                    // After sending the data record time and calculate when to send data again
                    wait_till = Calendar.getInstance();
                    wait_till.add(Calendar.SECOND, 30);   //Maximum wait time until next message
                }
                // Cleaning the byte array
                incomingData = new byte[1024];
            }
        }
        
        catch(SocketException e)
        {
            e.printStackTrace();
        } 
        catch (UnknownHostException ex) 
        {
            Logger.getLogger(UDPServerD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Determines if response from all the clients have been received.
     * @return true or false
     */
    public boolean is_all_received()
    {
        for(int i = 0; i<network.size(); i++)
        {
            if (!network.get(i).getis_received())
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Determines if the client is present in our array list
     * @param address client's IP
     * @param port client's port
     * @return if the client is present in the array list
     */
    public boolean isIPpresent(InetAddress address, int port)
    {
        for(int i = 0; i<network.size(); i++)
        {
            if(network.get(i).getIP().equals(address)){
                network.get(i).setReceivedTrue();
                network.get(i).updatePort(port);
                return true;
            }
        }
        return false;
    }

    /**
     * Sends the packets to all the client's in the array list
     * @param data the data to be sent: message + network info
     */
    public void sendPackets(byte[] data)
    {
        for(int i = 0; i < network.size(); i++)
        {
            if(network.get(i).getis_received())
            {
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, network.get(i).getIP(), network.get(i).getPort());
                try{
                socket.send(sendPacket);
                }
                catch(IOException x)
                {
                    System.out.println("Can't send packets");
                }
            }
        }
    }
    
    /**
     * Set that response from no one was received after sending data to everyone
     */
    public void setAllFalse()
    {
        for(int i = 0; i < network.size(); i++)
        {
            network.get(i).setReceivedFalse();
        }
    }
    
    /**
     * Continues receiving packet until timeout
     * @return integer indicating timeout or not
     */
    public int receiveUnntilTimeout()
    {
        try
        {
            print_network();
            System.out.println("\n------------Server is listening -------------\n");
            socket.receive(receivePacket);
        }
        catch (SocketTimeoutException e)
        {
           return 0;
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(UDPServerD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 1;
    }

    /**
     * Remove non responding clients
     */
    public void remove_non_responding_clients()
    {
        for (int i=0; i<network.size(); i++)
        {
            if(!network.get(i).is_received)
            {
                System.out.println("Client: " + network.get(i) + "is down");
                network.remove(i);
            }
        }
    }
    
    public void print_network()
    {
        System.out.println("The clients in the array list: ");
        for (int i = 0; i < network.size(); i++)
        {
            System.out.println(network.get(i));
        }
        System.out.println("");
    }
} //end of class
