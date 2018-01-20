/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.Serializable;

/**
 *
 * @author samsung
 */
public class EndDevice implements Serializable {
    private IPAddress ip;
    private IPAddress gateway;

    public EndDevice(IPAddress ip, IPAddress gateway) {
        this.ip = ip;
        this.gateway = gateway;
    }

    public IPAddress getIp() {
        return ip;
    }

    public IPAddress getGateway() {
        return gateway;
    }
    
    
}
