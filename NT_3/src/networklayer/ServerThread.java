/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import javax.xml.crypto.dom.DOMCryptoContext;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samsung
 */
public class ServerThread implements Runnable {
    private Thread t;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private EndDevice ip;
    public static String ask=null;
    public static String routPath=null;
    public static double hopCnt=0;
    public static double hopTotal=0;
    public static double dropCOunt=0;
    public static double success=0;

    public static double sddrop=0;


    public ServerThread(Socket socket,EndDevice ip){

        this.socket = socket;
        this.ip=ip;
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Server Ready for client "+NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;

        t=new Thread(this);
        t.start();
    }









    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        /*
        Tasks:
        1. Upon receiving a packet server will assign a recipient.
        [Also modify packet to add destination]
        2. call deliverPacket(packet)
        3. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        4. Either send acknowledgement with number of hops or send failure message back to client
        */

        try {
            output.writeObject(ip);


            while (true){
                hopTotal=0;
                success=0;
                sddrop=0;
               String s=(String ) input.readObject();

               if(s.equals("yes")){

                   dropCOunt=0;
                   for (int i = 0; i <100 ; i++) {
                       Packet pckt=(Packet) input.readObject();
                       Random random=new Random();
                       int j=Math.abs(random.nextInt(NetworkLayerServer.clients.size()));
                       //System.out.println(i);
                       IPAddress rIp=NetworkLayerServer.clients.get(j).getIp();
                       pckt.setDestinationIP(rIp);

                      //boolean DoN=deliverPacket(pckt);

                      if(deliverPacket(pckt)){
                          if(pckt.getSpecialMessage().equals("Show Route")){
                              String ss = "dropped ===== "+"route: "+routPath;
                              output.writeObject(ss);
                              output.writeObject(NetworkLayerServer.routers);
                          }
                          else {
                              String ss="Dropped";
                              output.writeObject(ss);
                          }

                      }
                      else {
                          if(pckt.getSpecialMessage().equals("Show Route")){
                              String ss = "Successfully Done, hop count:"+hopCnt+"=====Path:"+routPath;
                              hopTotal=hopTotal+hopCnt;
                              output.writeObject(ss);
                              output.writeObject(NetworkLayerServer.routers);
                          }
                          else {
                              String ss = "Successfully Sent, hop count:" + hopCnt+"===Path: "+routPath;
                              hopTotal=hopTotal+hopCnt;
                              output.writeObject(ss);
                          }
                      }
                   }
                   System.out.println("HT"+hopTotal);
                   System.out.println("success"+success);
                   System.out.println("Sddrop"+sddrop);
                   System.out.println("Drpcount"+dropCOunt);

                   double hopAvg;
                   double dropAvg;
                   if(success==0){
                       hopAvg=0;
                   }
                   else
                   {
                       hopAvg=hopTotal/success;
                   }
                   if((100.0-sddrop)==0){
                       dropAvg=0;
                   }
                   else {
                       dropAvg=dropCOunt/100.0-sddrop;
                   }

                   String rate=dropAvg+","+hopAvg;
                   try {
                       output.writeObject(rate);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }

               }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //System.out.println(NetworkLayerServer.clients.get(0));




    }

    Router getRouter(IPAddress ip)
    {
        Router res=null;

        for (Router x:NetworkLayerServer.routers
             ) {
            //System.out.println(x.getRouterId());
            //System.out.println(x.getInterfaceAddrs().get(0).getStr());
            //System.out.println(ip.getStr());
            if(x.getInterfaceAddrs().get(0).getStr().equals(ip.getStr())) {

                //System.out.println("asi");
                res=x;
                break;
            }
            //break;


        }

        return res;
    }


    /**
     * Returns true if successfully delivered
     * Returns false if packet is dropped
     * @param p
     * @return 
     */
    public Boolean deliverPacket(Packet p)
    {



        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination, 
                and eventually the packet reaches to destination router d.
                
            3(a) If, while forwarding, any gateway x, found from routingTable of router x is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t
                            
            3(b) If, while forwarding, a router x receives the packet from router y, 
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t
                            
        4. If 3(a) occurs at any stage, packet will be dropped, 
            otherwise successfully sent to the destination router
        */

        IPAddress sIp=p.getSourceIP();
        //System.out.println(sIp);
        //System.out.println(i);
        IPAddress rIp=p.getDestinationIP();
        //System.out.println(rIp);


        Router srcRouter=getRouter(sIp);
        System.out.println("Source Router: "+srcRouter.getRouterId());

        Router desRouter=getRouter(rIp);
        System.out.println("Destination Router: "+desRouter.getRouterId());

        boolean drop=false;

        if(!srcRouter.getState() || srcRouter.getRouterId()==desRouter.getRouterId() || !desRouter.getState()) {
            routPath+="dropped \n";
            sddrop++;
            System.out.println("dropped from src down");
            return true;
        }
        else {
            routPath="start-->"+String.valueOf(srcRouter.getRouterId());
            System.out.println(routPath+"\n");
        }

        Router nxtRouter = null;


        while(srcRouter.getRouterId()!=desRouter.getRouterId())
        {
            ArrayList<RoutingTableEntry> srcRoutingTable=srcRouter.getRoutingTable();
            int j=0;
            int gatewayId=-1;
            int curr_entry=0;
            while(j<srcRoutingTable.size()) {

                if (srcRoutingTable.size() == 0) {
                    gatewayId=-1;

                    break;
                }
                if(srcRoutingTable.get(j).getRouterId()==desRouter.getRouterId()){
                    curr_entry=j;
                    gatewayId=srcRoutingTable.get(j).getGatewayRouterId();
                    break;

                }
                j++;
            }

            if(gatewayId==-1){
                routPath+="dropped \n";
                System.out.println("dropped from gateway");
                drop=true;
                dropCOunt++;
                //System.out.println("dropped\n");
                //hopCnt++;
                break;
            }
            //System.out.println(gatewayId);
            for (Router x:NetworkLayerServer.routers
                    ) {
                //System.out.println(x.getRouterId());

                if(x.getRouterId()==gatewayId){
                    nxtRouter=x;
                    break;
                }
            }


            if(!nxtRouter.getState()){
                drop=true;
                srcRoutingTable.get(curr_entry).setDistance(Constants.INFTY);
                srcRoutingTable.get(curr_entry).setGatewayRouterId(-1);
                RouterStateChanger.stateupdater=false;
                if(srcRouter.getState()) NetworkLayerServer.DVR(srcRouter.getRouterId());
                RouterStateChanger.stateupdater=true;
                //drop=true;
                dropCOunt++;
                System.out.println("dropped from nxt state down");
                break;
            }

            ArrayList<RoutingTableEntry> nxtRoterTableEntry=nxtRouter.getRoutingTable();
            int k=0;
            while (k<nxtRoterTableEntry.size()) {
               if(nxtRoterTableEntry.get(k).getRouterId()==srcRouter.getRouterId()) {
                   if(nxtRoterTableEntry.get(k).getDistance()==Constants.INFTY){
                       nxtRoterTableEntry.get(k).setDistance(1);
                       RouterStateChanger.stateupdater=false;
                       if(srcRouter.getState()) NetworkLayerServer.DVR(nxtRouter.getRouterId());
                       RouterStateChanger.stateupdater=true;
                   }
               }
               k++;
            }

            routPath=routPath+"-->"+nxtRouter.getRouterId();
            srcRouter=nxtRouter;
            hopCnt++;
        }

        if(drop==true){
            hopCnt=0;
        }
        else {
            success++;
        }
        return drop;
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

}
