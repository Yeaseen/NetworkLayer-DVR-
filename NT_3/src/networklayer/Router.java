/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author samsung
 */
public class Router implements Serializable{
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddrs;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIds;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state

    private Boolean routerStateUpdateOccur=false; // true if any change will occur

    public Router() {
        interfaceAddrs = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIds = new ArrayList<>();
        
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.80) state = true;
        else state = false;
        
        numberOfInterfaces = 0;
    }
    
    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddrs)
    {
        this.routerId = routerId;
        this.interfaceAddrs = interfaceAddrs;
        this.neighborRouterIds = neighborRouters;
        routingTable = new ArrayList<>();
        
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.80) state = true;
        else state = false;
        
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    @Override
    public String toString() {
        String temp = "";
        temp+="Router ID: "+routerId+"\n";
        temp+="Intefaces: \n";
        for(int i=0;i<numberOfInterfaces;i++)
        {
            temp+=interfaceAddrs.get(i).getString()+"\t";
        }
        temp+="\n";
        temp+="Neighbors: \n";
        for(int i=0;i<neighborRouterIds.size();i++)
        {
            temp+=neighborRouterIds.get(i)+"\t";
        }
        return temp;
    }
    
    
    
    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable()
    {

        if(state) //if this router is UP , then initialize this routerEntryTable
        {
            routingTable=new ArrayList<>(); // clears the routingTable
            ArrayList<Router> routerList=NetworkLayerServer.routers; //fecth all routers from NLS

            //
            for (Router ri:routerList
                 ) {

                 RoutingTableEntry routingTableEntry;
                 if(ri.getRouterId()==this.routerId) //ri --> dest rounter
                 {
                    routingTableEntry =new RoutingTableEntry(this.routerId,0,this.routerId); // set a row
                 }
                else
                 {
                     int isNeighbor=0;
                     for (Integer  ni:this.neighborRouterIds
                          ) {
                         if((ni==ri.getRouterId()) && ri.getState()==Boolean.TRUE)
                         {
                             isNeighbor=1;
                             break;
                         }
                     }
                     if(isNeighbor==1)
                     {
                         routingTableEntry = new RoutingTableEntry(ri.getRouterId(),1,ri.getRouterId());
                     }
                     else
                     {
                         routingTableEntry = new RoutingTableEntry(ri.getRouterId(),Constants.INFTY,-1);

                     }
                 }
                routingTable.add(routingTableEntry);
            }
        }
        else
        {
            this.clearRoutingTable();
        }

    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable()
    {
       routingTable.clear();
    }


    public double getNewDistance(double ditance_to_neighbor,double neighborEntry_distance)
    {
        double result=ditance_to_neighbor+neighborEntry_distance;
        if(result>Constants.INFTY) return Constants.INFTY;
        else return result;

    }


    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor 
     */
    public void updateRoutingTable(Router neighbor)
    {
        //System.out.println(neighbor.getState());
        if(neighbor.getState()==false)
        {
            //System.out.println("update er 1st if e");
            for (RoutingTableEntry currEntry : routingTable
                 ) {
                if (currEntry.getGatewayRouterId() == neighbor.getRouterId() && currEntry.getDistance() != Constants.INFTY) {
                    routerStateUpdateOccur = true;
                    currEntry.setDistance(Constants.INFTY);
                }

            }
        }

        else {
            ArrayList<RoutingTableEntry> neighborRotingTable = neighbor.routingTable;
            double dist_to_neig = 1.0;

            int j=0;
            //System.out.println("update er 2ndd if e");
            //System.out.println("size===="+routingTable.size());
            while (j< routingTable.size())
              //  for (int j = 0; j <routingTable.size() ; j++)
            {
               // System.out.println("while e");
                //System.out.println("j====="+j);
                RoutingTableEntry currEntry = routingTable.get(j);

                RoutingTableEntry neighborEntry = neighborRotingTable.get(j);

                double newDistance = getNewDistance(dist_to_neig,neighborEntry.getDistance());

                //System.out.println("getNewDistance err por");
                if(currEntry.getGatewayRouterId()==neighbor.getRouterId() || (newDistance<currEntry.getDistance() && this.routerId!=neighborEntry.getGatewayRouterId())){

                    if(newDistance==currEntry.getDistance()) { j++; continue; }
                    routerStateUpdateOccur=true;
                    currEntry.setDistance(newDistance);
                    currEntry.setGatewayRouterId(neighbor.getRouterId());
                }
                j++;

            }
        }
    }


    public void updateSimpleRoutingTable(Router neighbor)
    {
        //System.out.println(neighbor.getState());
        if(neighbor.getState()==false)
        {
            //System.out.println("update er 1st if e");
            for (RoutingTableEntry currEntry : routingTable
                    ) {
                if (currEntry.getGatewayRouterId() == neighbor.getRouterId() && currEntry.getDistance() != Constants.INFTY) {
                    routerStateUpdateOccur = true;
                    currEntry.setDistance(Constants.INFTY);
                }

            }
        }

        else {
            ArrayList<RoutingTableEntry> neighborRotingTable = neighbor.routingTable;
            double dist_to_neig = 1.0;

            int j=0;
            //System.out.println("update er 2ndd if e");
            //System.out.println("size===="+routingTable.size());
            while (j< routingTable.size())
            //  for (int j = 0; j <routingTable.size() ; j++)
            {
                // System.out.println("while e");
                //System.out.println("j====="+j);
                RoutingTableEntry currEntry = routingTable.get(j);

                RoutingTableEntry neighborEntry = neighborRotingTable.get(j);

                double newDistance = getNewDistance(dist_to_neig,neighborEntry.getDistance());

                //System.out.println("getNewDistance err por");
                if((newDistance<currEntry.getDistance() && this.routerId!=neighborEntry.getGatewayRouterId())){

                    if(newDistance==currEntry.getDistance()) { j++; continue; }
                    routerStateUpdateOccur=true;
                    currEntry.setDistance(newDistance);
                    currEntry.setGatewayRouterId(neighbor.getRouterId());
                }
                j++;

            }
        }
    }


    
    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState()
    {
        state = !state;

        if (state == true) this.initiateRoutingTable();
        else this.clearRoutingTable();

    }

    public void setRouterStateUpdateOccur(Boolean state) { this.routerStateUpdateOccur=state; }
    public Boolean getRouterStateUpdateOccur() { return routerStateUpdateOccur; }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddrs() {
        return interfaceAddrs;
    }

    public void setInterfaceAddrs(ArrayList<IPAddress> interfaceAddrs) {
        this.interfaceAddrs = interfaceAddrs;
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIds() {
        return neighborRouterIds;
    }

    public void setNeighborRouterIds(ArrayList<Integer> neighborRouterIds) {
        this.neighborRouterIds = neighborRouterIds;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }


    
}
