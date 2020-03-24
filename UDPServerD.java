import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
//import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;  
import java.text.SimpleDateFormat;  
import java.util.Date; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPServerD {
	
    private final List<Client> network;
    DatagramSocket socket;
    DatagramPacket receivePacket;
    
    public UDPServerD()
    {
        network = new ArrayList<>();
    }
    
    public UDPServerD(List<Client> net)
    {
        this.network = net;
    }
	
    public void executeServer()
    {	
        try{
            socket = new DatagramSocket(1234);
            byte[] incomingData = new byte[1024];  
            
            Calendar wait_till = Calendar.getInstance();
            
            
            while(true)
            {
                
                
                receivePacket = new DatagramPacket(incomingData, incomingData.length);    
                        
                if (network.isEmpty())
                {   
                    
                    socket.setSoTimeout(0);
                    try{
                        System.out.println("\n--------Server is listening ----------\n");
                        socket.receive(receivePacket);
                        
                        String message = new String(receivePacket.getData());
                        InetAddress address = receivePacket.getAddress();
			int port = receivePacket.getPort();
                        System.out.println("Received message: " + message);
                        System.out.println("Client IP: "+ address.getHostAddress());
                        System.out.println("Client port: "+ port);
                        //Creating newClient to add to arraylist
                        Client newClient = new Client(address, port);
                        network.add(newClient);
                        
                        String response = "Thank you for the message ----- Response sent from server";
                        Packet sendPkt = new Packet(network, response);
                        
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(sendPkt);
                        byte[] sendData = outputStream.toByteArray();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                        System.out.println("Sending packet");
                        socket.send(sendPacket);
                        System.out.println("Packet sent");  
                        setAllFalse();
                        
                        wait_till = Calendar.getInstance();
                        wait_till.add(Calendar.SECOND, 30);   //Maximum wait time until next message
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }  
                
                //Network is not empty
                else{
                    while(!is_all_received())
                    {
                        long times_out = wait_till.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                        socket.setSoTimeout((int)times_out);
                        if(receiveUnntilTimeout()==0) 
                        {
                            System.out.println("Now towards sending data");
                            break;
                            
                        }
                            
                        String message = new String(receivePacket.getData());
                        InetAddress address = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        System.out.println("Received messagezz: " + message);
                        System.out.println("Client IP: "+ address.getHostAddress());
                        System.out.println("Client port: "+ port);
                                            
                        if(!isIPpresent(address, port))
                        {
                            System.out.println("Should not go here");
                            network.add(new Client(address, port));
                        }                       
                    }
                    
                    
                    String response = "Thank you for the message ----- Response sent from server";
                    Packet sendPkt = new Packet(network, response);    
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try{
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(sendPkt);
                        byte[] sendData = outputStream.toByteArray();
                        sendPackets(sendData);
                        System.out.println("Packets sent");  
                        remove_non_responding_clients();
                        setAllFalse();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }      
                    
                    wait_till = Calendar.getInstance();
                    wait_till.add(Calendar.SECOND, 30);   //Maximum wait time until next message
                }
                incomingData = new byte[1024];
            }
        }
        
        catch(SocketException e)
        {
            e.printStackTrace();
        }
    }

    public boolean is_all_received()
    {
        System.out.println("is all received entered");
        for(int i = 0; i<network.size(); i++)
        {
            if (!network.get(i).getis_received())
            {
                return false;
            }
        }
        return true;
    }
    
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

    public void sendPackets(byte[] data)
    {
        for(int i = 0; i < network.size(); i++)
        {
            if(network.get(i).getis_received())
            {
                System.out.println("packet pathayo");
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
    

    
    public void setAllFalse()
    {
        for(int i = 0; i < network.size(); i++)
        {
            network.get(i).setReceivedFalse();
        }
    }
    
    public int receiveUnntilTimeout()
    {
        try{
            System.out.println("\n--------Server is listening ----------\n");
            socket.receive(receivePacket);
            System.out.println("received");
        }
        catch (SocketTimeoutException e){
           return 0;
        } catch (IOException ex) {
            Logger.getLogger(UDPServerD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 1;
    }

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
}
