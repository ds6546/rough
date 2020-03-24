import java.io.Serializable;
import java.util.List;

/**
 * A packet contains information about the current state of network and message to be delivered.
 * It is serialized and sent to all the client's in the network.
 * @author deepson
 */
public class Packet implements Serializable{
    private List<Client> network;
    private String msg;
    
    /**
     * A packet constructor
     * @param clients info about clients in the servers array list
     * @param msg message to be sent
     */
    public Packet(List<Client> clients, String msg)
    {
        this.network = clients;
        this.msg = msg;
    }
    
    /**
     * Returns client array
     * @return client array
     */
    public List<Client> getClientArray()
    {
        return network;
    }
    
    /**
     * Returns message in the packet
     * @return packet message
     */
    public String getMsg()
    {
        return msg;
    }
    
    /**
     * Prints Client Array
     */
    public void printClientArray()
    {
        for (int i=0; i<network.size(); i++)
        {
            network.get(i).printIP();
        }
    }
    
    /**
     * Removes non responding clients
     */
    public void print_and_remove_nonresponding_Clients()
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
