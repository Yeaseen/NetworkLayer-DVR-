/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samsung
 */
public class Client implements Serializable {

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }






    public static void main(String[] args)
    {
        Socket socket;
        ObjectInputStream input = null;
        ObjectOutputStream output = null;

        try {
            socket = new Socket("localhost", 1234);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Connected to server");
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
            handles a list of active clients.]        
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive 
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.   
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */

        try {

            EndDevice ip= (EndDevice) input.readObject();
            //String s=ip.getIp().toString();
            System.out.println(ip.getIp());

            while (true) {
                System.out.println("Start for Y");
                Scanner scn = new Scanner(System.in);
                String ans = scn.nextLine();

                if (ans.equals("yes")) {
                    output.writeObject(ans);

                    for (int i = 0; i < 100; i++) {

                        Packet pckt;

                        if (i == 20) {
                            pckt = new Packet(getSaltString(), "Show Route", (IPAddress) ip.getIp(), null);

                        } else {
                            pckt = new Packet(getSaltString(), "", (IPAddress) ip.getIp(), null);

                        }
                        output.writeObject(pckt);

                        if (i == 20) {
                            String feed = input.readObject().toString();
                            System.out.println(feed);
                            ArrayList<Router> routers = (ArrayList<Router>) input.readObject();
                            for (Router ri : routers
                                    ) {
                                System.out.println("Table for" + ri.getRouterId() + "=================");

                                for (RoutingTableEntry re : ri.getRoutingTable()
                                        ) {
                                    System.out.println("goes to" + re.getRouterId() + " distance " + re.getDistance() + ":NextHOP: " + re.getGatewayRouterId());
                                }
                            }
                        }
                        else {

                            String feed = input.readObject().toString();

                              //  System.out.println("Packet Dropped");
                            System.out.println(feed);




                        }

                    }

                    String rate = (String) input.readObject();
                    //String rate= (String) input.readObject();
                    System.out.println(rate);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

    }


}

