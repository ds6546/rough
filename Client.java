import java.io.Serializable;
import java.net.InetAddress;

/**
 * This is a Client class. Information is bundled into groups called clients and
 * they are stored in array list. A client represents a physical client host in
 * the network. It's IP Address and port number is stored. 
 * @author deepson
 */
public class Client implements Serializable{
	final private InetAddress IPAddress;
	boolean is_received;
        int port;
	
    /**
     * A client constructor
     * @param IP This is the client's IP Address
     * @param port This is the client's port number through which it recently send the data
     */
    public Client(InetAddress IP, int port)
    {
	this.IPAddress = IP;
        this.port = port;
        this.is_received = true;
    }
    
    /**
     * Gets the client object's IP Address
     * @return IP
     */
    public InetAddress getIP()
    {
    	return IPAddress;
    }
    
    /**
     * Prints the client object's IP Address
     */
    public void printIP() {
        System.out.println(IPAddress.getHostAddress());
    }
    
    /**
     * String representation of the class - here represented by the IP
     * @return String IP of the client object
     */
    @Override
    public String toString(){
        return IPAddress.getHostAddress();
    }
    
    /**
     * Gets Client's port number.
     * @return Port number
     */
    public int getPort(){
        return port;
    }
    
    /**
     * Updates port number
     * @param newport client's port through which data was sent
     */
    public void updatePort(int newport)
    {
        this.port = newport;
    }
    
    /**
     * Figures out if the client is still connected
     * @return true if data from the client was received
     */
    public boolean getis_received()
    {
        return is_received;
    }
    
    /**
     * Indicates information was received from the client
     */
    public void setReceivedTrue()
    {
        is_received = true;
    }
    
    /**
     * Indicates information was not received from the client
     */
    public void setReceivedFalse()
    {
        is_received = false;
    }
}
