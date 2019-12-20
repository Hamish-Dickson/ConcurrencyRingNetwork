import java.io.FileWriter;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * @author Stirling student: 2520503
 */
public class RingManager {
    /**
     * @param ring_node_host host name to target injection on
     * @param ring_node_id   node to inject token into
     * @param fileName       filename to be written to
     * @param circulations   desired number of passes
     * @param extraTimeNode  nth node to grant extra time to
     * @param skippingNode   mth node to skip ever second visit to
     */
    public RingManager(String ring_node_host, String ring_node_id, String fileName, int circulations, int extraTimeNode, String skippingNode) {

        System.setSecurityManager(new SecurityManager());

        // create fileWriter and clear file
        System.out.println("Clearing " + fileName + " file");
        try {
            FileWriter fw_id = new FileWriter(fileName, false);//append false means it will clear the file
            fw_id.close();
        } catch (java.io.IOException e) {
            System.err.println("Exception in clearing file: " + e);
        }

        // get remote reference to ring element/node and inject token by calling takeToken()
        System.out.println("Connecting to Node " + ring_node_id + " on " + ring_node_host);

        try {
            String nodeName = "//" + ring_node_host + "/" + ring_node_id;
            RingMember node = (RingMember) Naming.lookup(nodeName);

            //the token to be passed, keeping count of the number of executions. Initially 0. implementation of extra task #1
            int tokenObject = 0;

            node.takeToken(tokenObject, fileName, circulations, extraTimeNode, skippingNode);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * This constructor is used by the terminating configuration, and will terminate all nodes on a given hostname.
     *
     * @param hostname hostname to terminate nodes on
     */
    public RingManager(String hostname) {
        System.setSecurityManager(new SecurityManager());

        try {
            String[] nodeNames = Naming.list(hostname); //lookup all nodes registered on the host name given.
            System.out.println("Cleaning up nodes: " + Arrays.toString(nodeNames));

            for (String nodeName : nodeNames) { //for each node name
                RingMember node = (RingMember) Naming.lookup(nodeName);//lookup the node
                node.terminate();//call terminate()
            }
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main entry point for RingManager
     *
     * @param argv usage either: for injecting token: (current host) (node id) (filename) (number of circulations>)
     *             (nth node for extra time on) (nth node to skip every second visit on)
     *             Or: for terminating nodes: (host to terminate)
     */
    public static void main(String argv[]) {
        String hostname;
        if (argv.length != 6 && argv.length != 1) {//if arguments not correct length
            System.out.println("usage: (current host) (node id) (filename) (number of circulations) (nth node for extra time on) (nth node to skip every second visit on)");
            System.out.println("Or usage: (host to terminate)");
            System.exit(1);
        }
        if (argv.length == 1) { //if terminating nodes
            //implementation of extra task #6
            hostname = argv[0];
            System.out.println("Running cleanup on all nodes on host: " + hostname);
            RingManager rm = new RingManager(hostname);
        } else { //if injecting token
            hostname = argv[0];
            String initialNode = argv[1];
            String fileName = argv[2]; //implementation of extra task #2
            int circulations = Integer.parseInt(argv[3]); //implementation of extra task #3
            int extraTimeNode = Integer.parseInt(argv[4]); //implementation of extra task #4
            String skippingNode = argv[5]; //implementation of extra task #5

            System.out.println("Ring manager host is: " + hostname
                    + "\nInjecting node is: " + initialNode
                    + "\nUsing file: " + fileName
                    + "\nFor " + circulations + " Circulations"
                    + "\nAdding extra time every: " + extraTimeNode + " passes"
                    + "\nRing node " + skippingNode + " is skipping every second usage");

            // instantiate RingManager with parameters
            RingManager rm = new RingManager(hostname, initialNode, fileName, circulations, extraTimeNode, skippingNode);
        }

    }
}  