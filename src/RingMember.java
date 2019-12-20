import java.rmi.RemoteException;

/**
 * @author Stirling student: 2520503
 */
public interface RingMember extends java.rmi.Remote {

    public void takeToken(int countObject, String fileName, int circulations, int extraTimeNode, String skippingNode) throws RemoteException;

    public void terminate() throws RemoteException;
}
