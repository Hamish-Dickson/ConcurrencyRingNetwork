import java.io.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.util.*;

/**
 * @author Stirling student: 2520503
 */
public class CriticalSection extends Thread {
    /**
     * @param t_host        current host
     * @param t_id          current node id
     * @param n_host        next host
     * @param n_id          next node id
     * @param tokenObject   token to count passes
     * @param fileName      filename to write to
     * @param circulations  max passes to execute
     * @param extraTimeNode nth node to give extra time to
     * @param skippingNode  mth node to skip every second time on
     */
    public CriticalSection(String t_host, String t_id, String n_host, String n_id, int tokenObject, String fileName, int circulations, int extraTimeNode, String skippingNode) {
        this_host = t_host;
        this_id = t_id;
        next_host = n_host;
        next_id = n_id;
        objectCount = tokenObject;
        this.fileName = fileName;
        this.circulations = circulations;
        this.extraTimeNode = extraTimeNode;
        this.skippingNode = skippingNode;
        tokenVisits++;
    }

    /**
     * This method provides the functionality of writing to the file and passing to the next node.
     */
    public void run() {
        //construct the url of next node to be connected to.
        String nextNodeName = "//" + next_host + "/" + next_id;

        System.out.println("Skipping node is: " + skippingNode + " This node is: " + this_id);

        if (skippingNode.equals(this_id) && tokenVisits % 2 == 0) {//if this is the node to skip on, and the visit is a second one.

            System.out.println("Skipping node as it is a second visit. Passing token to: " + nextNodeName + tokenVisits);
            try {
                //pass the token on without delay. Extra task #5
                RingMember nextNode = (RingMember) Naming.lookup(nextNodeName);
                nextNode.takeToken(objectCount, fileName, circulations, extraTimeNode, skippingNode);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
        } else {//else if this is not a node to be skipped.
            // sleep to symbolise critical section duration
            try {
                //implementation of extra task #4. Checks if this pass is one that should be added time to.
                //if the result of objectCount%extraTimeNode is 0, add extra time, as this means this node should have extra time.
                //additionally check that count != 0 to avoid the fact that 0/anything is 0, therefore the remainder will be 0,
                // which would lead to incorrect sleeps being added.
                if (objectCount % extraTimeNode == 0 && objectCount != 0) {
                    System.out.println("Adding extra time to critical section");
                    sleep(2000);
                } else {
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Sleep failed");
            }

            // write timestamp (date) to file
            try {
                System.out.println("Writing to file: " + fileName);

                Date timestmp = new Date();
                String timestamp = timestmp.toString();
                // Next create fileWriter -true means writer *appends*
                FileWriter fw_id = new FileWriter(fileName, true);
                // Create PrintWriter -true = flushbuffer on each println
                PrintWriter pw_id = new PrintWriter(fw_id, true);// println means adds a newline (as distinct from print)
                pw_id.println("Record from ring node" + this_id + " on host " + this_host + "." + timestamp + ". "
                        + "Current object pass count is: " + (objectCount + 1));
                pw_id.close();
                fw_id.close();
            } catch (java.io.IOException e) {
                System.out.println("Error writing to file: " + e);
            }
            try {
                Thread.sleep(1000);
            } catch (
                    java.lang.InterruptedException e) {
                System.out.println("sleep failed: " + e);
            }

            // get remote reference to next ring element, and pass token on ...
            try {
                System.out.println("Passing token to: " + nextNodeName + "\n");
                RingMember nextNode = (RingMember) Naming.lookup(nextNodeName);
                objectCount++;//add one to the object pass counter
                nextNode.takeToken(objectCount, fileName, circulations, extraTimeNode, skippingNode);//pass token on
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private String this_id;
    private String this_host;
    private String next_id;
    private String next_host;
    private int objectCount;
    private String fileName;
    private int circulations;
    private int extraTimeNode;
    private static int tokenVisits = 0;
    private String skippingNode;
}