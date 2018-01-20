/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package networklayer;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CSE_BUET
 */
public class NetworkLayerServer {
    static int clientCount = 1;
    static ArrayList<Router> routers = new ArrayList<>();
    static ArrayList<EndDevice> clients=new ArrayList<>();
    static RouterStateChanger stateChanger = null;
    /**
     * Each map entry represents number of client end devices connected to the interface
     */
    static Map<IPAddress,Integer> clientInterfaces = new HashMap<>();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /**
         * Task: Maintain an active client list
         */
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Server Ready: "+serverSocket.getInetAddress().getHostAddress());
        
        System.out.println("Creating router topology");
        
        readTopology();
        printRouters();
        
        /**
         * Initialize routing tables for all routers
         */
        initRoutingTables();


        System.out.println("Before DVR(1)====================");
        for (Router ri:routers
                ) {
            System.out.println("Table for"+ri.getRouterId()+"=================");

            for (RoutingTableEntry re:ri.getRoutingTable()
                    ) {
                System.out.println("goes to"+re.getRouterId()+" distance "+re.getDistance()+":NextHOP: "+re.getGatewayRouterId());

            }
        }
        /**
         * Update routing table using distance vector routing until convergence
         */
        DVR(1);


        System.out.println("After DVR(1)====================");
        for (Router ri:routers
                ) {
            System.out.println("Table for"+ri.getRouterId()+"=================");

            for (RoutingTableEntry re:ri.getRoutingTable()
                    ) {
                System.out.println("goes to"+re.getRouterId()+" distance "+re.getDistance()+":NextHOP: "+re.getGatewayRouterId());
            }
        }


        /**
         * Starts a new thread which turns on/off routers randomly depending on parameter Constants.LAMBDA
         */
        stateChanger = new RouterStateChanger();
        
        while(true){
            try {
                Socket clientSock = serverSocket.accept();
                System.out.println("Client attempted to connect");
                EndDevice ip=getClientDeviceSetup();
                clients.add(ip);
                System.out.println(ip.getIp());
                System.out.println(NetworkLayerServer.clients.get(0).getIp());
                new ServerThread(clientSock,ip);
            } catch (IOException ex) {
                Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
    
    public static void initRoutingTables()
    {
        for(int i=0;i<routers.size();i++)
        {
            routers.get(i).initiateRoutingTable();
        }
    }

    public static int getStartPos(int startingRouterId)
    {
        int res=0;
        for (int i = 0; i < routers.size(); i++) //finds position
        {
            if (routers.get(i).getRouterId() == startingRouterId) {
                res = i;
                break;
            }
        }

        return res;
    }

    public static boolean getDVR_convergence(int start,int end)
    { boolean res=true;

        //System.out.println("getDVR1");
        for (int i = start; i < end ; i++) {
            Router sourc_Router = routers.get(i);
            ArrayList<Integer> Neighbors = sourc_Router.getNeighborRouterIds();

            int j=0;
            //System.out.println("1st loop");
            while (j<Neighbors.size())
            {
                int t=Neighbors.get(j).intValue();
                int k=0;
                //System.out.println("2nd loop");
                while (k<routers.size())
                {
                    //System.out.println("3rd loop");
                    if(routers.get(k).getRouterId()==t){
                        t=k;
                        break;
                    }
                        k++;
                }
                Router dest_Router = routers.get(t);

                dest_Router.setRouterStateUpdateOccur(false);
                if(dest_Router.getState())
                {
                    //System.out.println("Update routing table");
                    dest_Router.updateRoutingTable(sourc_Router);
                    //System.out.println("Update routing table baire");

                }
                if(dest_Router.getRouterStateUpdateOccur())
                {
                    res=false;
                }
                j++;
            }
        }

        return res;
    }



    
    /**
     * Task: Implement Distance Vector Routing with Split Horizon and Forced Update
     */
    public static void DVR(int startingRouterId)
    {
        /**
         * pseudocode
         */
        /*
        while(convergence)
        {
            //convergence means no change in any routingTable before and after executing the following for loop
            for each router r <starting from the router with routerId = startingRouterId, in any order>
            {
                1. T <- getRoutingTable of the router r
                2. N <- find routers which are the active neighbors of the current router r
                3. Update routingTable of each router t in N using the 
                   routing table of r [Hint: Use t.updateRoutingTable(r)]
            }
        }
        */
        int startPos=getStartPos(startingRouterId);
        boolean convergence =false;
        while (!convergence)
        {
            //System.out.println("In DVR convo");
            convergence=true;
            boolean convergence1,convergence2;
            convergence1=getDVR_convergence(startPos,routers.size());
            //System.out.println("Covo1");
            convergence2=getDVR_convergence(0,startingRouterId);
            if(convergence1==false || convergence2==false) convergence=false;
            else convergence=true;
        }
    }



    public static boolean getSimpleDVR_convergence(int start,int end)
    { boolean res=true;

        //System.out.println("getDVR1");
        for (int i = start; i < end ; i++) {
            Router sourc_Router = routers.get(i);
            ArrayList<Integer> Neighbors = sourc_Router.getNeighborRouterIds();

            int j=0;
            //System.out.println("1st loop");
            while (j<Neighbors.size())
            {
                int t=Neighbors.get(j).intValue();
                int k=0;
                //System.out.println("2nd loop");
                while (k<routers.size())
                {
                    //System.out.println("3rd loop");
                    if(routers.get(k).getRouterId()==t){
                        t=k;
                        break;
                    }
                    k++;
                }
                Router dest_Router = routers.get(t);

                dest_Router.setRouterStateUpdateOccur(false);
                if(dest_Router.getState())
                {
                    //System.out.println("Update routing table");
                    dest_Router.updateSimpleRoutingTable(sourc_Router);
                    //System.out.println("Update routing table baire");

                }
                if(dest_Router.getRouterStateUpdateOccur())
                {
                    res=false;
                }
                j++;
            }
        }

        return res;
    }







    /**
     * Task: Implement Distance Vector Routing without Split Horizon and Forced Update
     */
    public static void simpleDVR(int startingRouterId)
    {
        int startPos=getStartPos(startingRouterId);
        boolean convergence =false;
        while (!convergence)
        {
            //System.out.println("In DVR convo");
            convergence=true;
            boolean convergence1,convergence2;
            convergence1=getSimpleDVR_convergence(startPos,routers.size());
            //System.out.println("Covo1");
            convergence2=getDVR_convergence(0,startingRouterId);
            if(convergence1==false || convergence2==false) convergence=false;
            else convergence=true;
        }
        
    }
    
    
    public static EndDevice getClientDeviceSetup()
    {
        Random random = new Random();
        int r =Math.abs(random.nextInt(clientInterfaces.size()));
        
        System.out.println("Size: "+clientInterfaces.size()+"\n"+r);
        
        IPAddress ip=null;
        IPAddress gateway=null;
        
        int i=0;
        for (Map.Entry<IPAddress, Integer> entry : clientInterfaces.entrySet()) {
            IPAddress key = entry.getKey();
            Integer value = entry.getValue();
            if(i==r)
            {
                gateway = key;
                ip = new IPAddress(gateway.getBytes()[0]+"."+gateway.getBytes()[1]+"."+gateway.getBytes()[2]+"."+(value+2));
                value++;
                clientInterfaces.put(key, value);
                break;
            }
            i++;
        }
        
        EndDevice device = new EndDevice(ip, gateway);
        System.out.println("Device : "+ip+"::::"+gateway);
        return device;
    }
    
    public static void printRouters()
    {
        for(int i=0;i<routers.size();i++)
        {
            System.out.println("------------------\n"+routers.get(i));
        }
    }
    
    public static void readTopology()
    {
        Scanner inputFile = null;
        try {
            inputFile = new Scanner(new File("topology.txt"));
            //skip first 27 lines
            int skipLines = 27;
            for(int i=0;i<skipLines;i++)
            {
                inputFile.nextLine();
            }
            
            //start reading contents
            while(inputFile.hasNext())
            {
                inputFile.nextLine();
                int routerId;
                ArrayList<Integer> neighborRouters = new ArrayList<>();
                ArrayList<IPAddress> interfaceAddrs = new ArrayList<>();
                
                routerId = inputFile.nextInt();
                
                int count = inputFile.nextInt();
                for(int i=0;i<count;i++)
                {
                    neighborRouters.add(inputFile.nextInt());
                }
                count = inputFile.nextInt();
                inputFile.nextLine();
                
                for(int i=0;i<count;i++)
                {
                    String s = inputFile.nextLine();
                    //System.out.println(s);
                    IPAddress ip = new IPAddress(s);
                    interfaceAddrs.add(ip);
                    
                    /**
                     * First interface is always client interface
                     */
                    if(i==0)
                    {
                        //client interface is not connected to any end device yet
                        clientInterfaces.put(ip, 0);
                    }
                }
                Router router = new Router(routerId, neighborRouters, interfaceAddrs);
                routers.add(router);
            }
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
