/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samsung
 */
public class RouterStateChanger implements Runnable{

    Thread t;
    public static boolean stateupdater=true;
    
    public RouterStateChanger() {
        t=new Thread(this);
        t.start();

    }
    
    @Override
    public void run() {
        Random random = new Random();
            while (true) {
                if(stateupdater) {
                    double p = random.nextDouble();
                    if (p < Constants.LAMBDA) {
                        revertRandomRouter();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RouterStateChanger.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }



    
    public void revertRandomRouter()
    {
        /**
        * Randomly select a router and revert its state
        */
        Random random = new Random();
        int id = random.nextInt(NetworkLayerServer.routers.size());
        NetworkLayerServer.routers.get(id).revertState();
        System.out.println("State Changed; Router ID: "+NetworkLayerServer.routers.get(id).getRouterId());
    }
}
