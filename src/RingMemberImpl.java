import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Stirling student: 2520503
 */
public class RingMemberImpl extends java.rmi.server.UnicastRemoteObject implements RingMember {
    /**
     * @param t_node host to register on
     * @param t_id   node id to register on
     * @param n_node next node host
     * @param n_id   next node id
     * @throws RemoteException issue with RMI
     */
    public RingMemberImpl(String t_node, String t_id, String n_node, String n_id) throws RemoteException {
        this_host = t_node;
        this_id = t_id;
        next_host = n_node;
        next_id = n_id;
    }

    /**
     * @param tokenObject   token to count passes
     * @param fileName      filename to write to
     * @param circulations  max passes to execute
     * @param extraTimeNode nth node to give extra time to
     * @param skippingNode  mth node to skip every second time on
     */
    public synchronized void takeToken(int tokenObject, String fileName, int circulations, int extraTimeNode, String skippingNode) {
        System.out.println("Token Received!");
        if (tokenObject == circulations) {//if the current pass count == the max circulations
            System.out.println("Maximum circulations reached. Stopping ring network");
            //don't execute critical section
        } else {
            // start critical section by instantiating and starting critical section thread
            c = new CriticalSection(this_host, this_id, next_host, next_id, tokenObject, fileName, circulations, extraTimeNode, skippingNode);
            c.start();
        }
    }

    /**
     * This method is used by the terminate configuration of RingManager, and allows the node to unExport itself from the RMI server
     *
     * @throws RemoteException issue with RMI
     */
    public synchronized void terminate() throws RemoteException {
        System.out.println("Exiting this node!");

        UnicastRemoteObject.unexportObject(this, true); //unExport this object from the RMI server. this terminates the process.

        System.out.println("Node unbound!");
    }

    /**
     * Main entry point for each node instance.
     *
     * @param argv usage: (this host) (this id) (next host) (next id)
     */
    public static void main(String argv[]) {
        System.setSecurityManager(new SecurityManager());
        RingMemberImpl node = null;
        try {
            if (argv.length != 4) {
                System.out.println("usage: (this host) (this id) (next host) (next id)");
                System.exit(1);
            }
            // instantiate RingMemberImpl class with appropriate parameters
            node = new RingMemberImpl(argv[0], argv[1], argv[2], argv[3]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // register object with RMI registry
        try {
            String tempNodeName = "//" + argv[0] + "/" + argv[1];//the composite URL to be registered to
            System.out.println("registering node:" + tempNodeName);
            Naming.rebind(tempNodeName, node); //bind the object to the RMI server
            System.out.println("node registered!");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private String next_id;
    private String next_host;
    private String this_id;
    private String this_host;
    private CriticalSection c;
}