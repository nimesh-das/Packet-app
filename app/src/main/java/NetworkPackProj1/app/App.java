/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package NetworkPackProj1.app;

// Declare imports from pcap, java and sl4j
import NetworkPackProj1.list.LinkedList;
import static NetworkPackProj1.utilities.StringUtils.join;
import static NetworkPackProj1.utilities.StringUtils.split;
import static NetworkPackProj1.app.MessageUtils.getMessage;
import java.net.InetAddress;
import java.net.Inet4Address;
import org.pcap4j.core.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import java.io.IOException;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;
import org.pcap4j.packet.IpV4Packet;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.util.*;

public class App {

    // Properties
        int timeout;
        int snapLen;
        int maxPackets;
        PacketListener listener;
        PcapHandle handle;
        PromiscuousMode mode;
        static InetAddress addr;
        static PcapNetworkInterface nic;

    // Constructor initialization
    App(String ipAddress) throws Exception {
        this.timeout = 50;
        this.snapLen = 65536;
        this.maxPackets = 10;
        SetupNicByAddr(ipAddress);
        SetupPcapHandle();
    }

    App(String ipAddress, String filter) throws Exception {
        this.timeout = 50;
        this.snapLen = 65536;
        this.maxPackets = 10;
        SetupNicByAddr(ipAddress);
        SetupPcapHandle(filter);
    }

    App() throws Exception {
        this.timeout = 50;
        this.snapLen = 65536;
        this.maxPackets = 10;
    }

    public static PcapNetworkInterface SetupNicBySelection() throws Exception {
        PcapNetworkInterface nic1 = null;
        nic1 = new NifSelector().selectNetworkInterface();
        return nic1;
    }

    // Setup the network interface on which you will do the capture by providing an address
    public static void SetupNicByAddr(String direct_address) throws Exception {

        addr = InetAddress.getByName(direct_address);
        nic = Pcaps.getDevByAddress(addr);

    }

    // Check available network interfaces
    public void checkNetworkInterfaces() throws Exception {
        List<PcapNetworkInterface> allDevs = null;
        allDevs = Pcaps.findAllDevs();
        for(int i = 0; i < allDevs.size(); i++) {
            System.out.println(allDevs.get(i));
        }
    }

    // Open a pcaphandle which is essentially the class responsible for capturing
    // streams of packets, sending packets, etc
    public void SetupPcapHandle() throws Exception {

        this.mode = PromiscuousMode.PROMISCUOUS;
        this.handle = nic.openLive(snapLen, mode, timeout);

    }

    // Apply a filter to the handle
    public void SetupPcapHandle(String filter) throws Exception {
        //System.out.println("I'm inside the PcapHandle");
        this.mode = PromiscuousMode.PROMISCUOUS;
        //System.out.println("Opening live handle");
        this.handle = nic.openLive(snapLen, mode, timeout);
        //System.out.println("Setting filter");
        this.handle.setFilter(filter, BpfCompileMode.OPTIMIZE);
    }

    // Create a listener that defines what to do with the received packets
    public void ProcessPackets() throws Exception {

        this.listener = new PacketListener() {
        @Override
        public void gotPacket(Packet packet) {
            // Override the default gotPacket() function and process packet
            System.out.println(handle.getTimestamp());
            System.out.println(packet);
            }
        };

        // Tell the handle to loop using the listener we created
        handle.loop(maxPackets, listener);
        
        // Cleanup when complete
        handle.close();
    }

    public static void main(String[] args) throws Exception {
        try {
            // Prior to running the app, ensure you have priviledges to read from /dev/bpf* -> sudo chmod o+r /dev/bpf*
            // Start App
            App myapp;
            String ipAddress = "";
            String userChoice = "";
            System.out.println("Hello, this is a Network Packet app based off of Pcap4j. What would you like to do ?\n 1) List all Network Interfaces\n 2) Process Packets");

            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            if (myObj.hasNextLine()) {
                userChoice = myObj.nextLine();  // Read user input
            }
            userChoice.replaceAll("\\s+","");
            System.out.println("User choice is: " + userChoice);

            if (userChoice.contains("1")) {
            //    System.out.println("Inside 1");
                myapp = new App();
                myapp.checkNetworkInterfaces();
            //    System.out.println("Finishing 1");
            } else if (userChoice.contains("2")) {
                System.out.println("Enter a source IP Address: ");
                if (myObj.hasNextLine()) {
                    ipAddress = myObj.nextLine();  // Read user input
                }
                ipAddress.replaceAll("\\s+","");
                System.out.println("Source IP Address is: " + ipAddress);  // Output user input
                System.out.println("Would you like to apply a Berkely Packet Filter? e.g. tcp port 80, dst host 198.51.100.200, vlan 100, more examples can be found in:\nhttps://docs.extrahop.com/8.2/bpf-syntax\nYes or No?");
                if (myObj.hasNextLine()) {
                    userChoice = myObj.nextLine();
                }
                userChoice.replaceAll("\\s+","");
                if (userChoice.contains("Yes")) {
                    System.out.println("Please enter the filter: ");
                    if (myObj.hasNextLine()) {
                        userChoice = myObj.nextLine();
                    }
                    userChoice.replaceAll("\\s+","");
                    System.out.println("Filter chosen: " + userChoice);
                    myapp = new App(ipAddress, userChoice);
                } else {
                    myapp = new App(ipAddress);
                }
                System.out.println("******************************** Processing packets from source *****************************************");
                myapp.ProcessPackets();
                myObj.close();
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
