/* CS 380 - Computer Networks
 * Project 5 : IPv4 w/ UDP
 * Ismail Abbas
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.text.*;
import java.util.*;

/*
 * This class represents the UDP client that will
 * utilize the Ipv4_Packet class 
 */
public class UdpClient {

	private static Ipv4_Packet IPV4;
    private static double TOTAL_RTT = 0;
    private static int DATA_SIZE = 2;

	// The main method
    public static void main(String[] args) {

        try {
            Socket s = new Socket("18.221.102.182", 38005);
            IPV4 = new Ipv4_Packet(s);
            OutputStream os = s.getOutputStream();

            os.write(IPV4.genHandShake());
            System.out.println("\nHandshake response: 0x" +recResponse(s));
            int portNumber = getPortNum(s);
            System.out.println("Port number received: " + portNumber);

            while(DATA_SIZE <= 4096) {
                System.out.println("\nSending packet with " + DATA_SIZE + " bytes of data");
                double start = System.currentTimeMillis();
                byte[] header =  udpHead(DATA_SIZE, portNumber);
                byte[] udpPack = IPV4.generateUDPPacket(DATA_SIZE, header);
                os.write(udpPack);
                System.out.println("Response: 0x" + recResponse(s));
                double end = System.currentTimeMillis();
                System.out.println("RTT: " + (end - start) + "ms");
                TOTAL_RTT += (end - start);
                DATA_SIZE *= 2;
            }
            double averageRTT = TOTAL_RTT / 12;
            DecimalFormat f = new DecimalFormat("#.##");
            System.out.println("\nAverage RTT: " + f.format(averageRTT) + "ms");

        } catch (Exception e) { e.printStackTrace(); }
    }

	/*
	 * This helper method performs the checksum, as well as 
	 * configures the UDP header
	 */
    private static byte[] udpHead(int size, int port) {
        byte[] data = new byte[size];
        new Random().nextBytes(data);

        byte[] packet = new byte[8 + data.length];

        // Source Port
        packet[0] = 0;
        packet[1] = 0;
        // Destination Port
        packet[2] = (byte) ((port & 0xFF00) >>> 8);
        packet[3] = (byte) (port & 0x00FF);
        // Length of UDP
        packet[4] = (byte) ((packet.length & 0xFF00) >>> 8);
        packet[5] = (byte) (packet.length & 0x00FF);
        packet[6] = 0;	// First CheckSum
        packet[7] = 0;

        int pos = 0;
        for(int i = 8; i < packet.length; ++i) {
            packet[i] = data[pos++];
        }

        // perform Check Sum
        byte[] checkSum = getUDPCheckSum(packet);
        packet[6] = checkSum[0];
        packet[7] = checkSum[1];

        return packet;

    }

	/*
	 * This method will get the CheckSum based on the 
	 * packet passed in
	 */
    private static byte[] getUDPCheckSum(byte[] packet) {
        byte[] pseudoHeader = psuedoHeader(packet);
        short checkSum = (short) IPV4.checkSum(pseudoHeader);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(checkSum);
        return buffer.array();
    }

    public static byte[] psuedoHeader(byte[] header) {

        byte[] source = IPV4.getSourceAddr();
        byte[] dest = IPV4.getDestintionAddr();
        int protocol = 17;
        int length = header.length;

        byte[] packet = new byte[header.length + 12];

        for(int i = 0; i < source.length; ++i) {
            packet[i] = source[i];
        }
        int count = 4;
        for(int k = 0; k < dest.length; ++k) {
            packet[count++] = dest[k];
        }

        packet[8] = 0;
        packet[9] = (byte) protocol;
        packet[10] = (byte)((length & 0xFF00) >>> 8);
        packet[11] = (byte)((length & 0x00FF));
        count = 0;
        for(int i = 12; i < packet.length; ++i) {
            packet[i] = header[count++];
        }
        return packet;
    }

	private static String recResponse(Socket s) {
        try {
            InputStream is = s.getInputStream();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < 4; ++i) {
                sb.append(Integer.toHexString(is.read()).toUpperCase());
            }
            return sb.toString();
        } catch (Exception e) { }
        return "error";
    }
	
    private static int getPortNum(Socket s) {
        try {
            int portNumber = -1;
            InputStream is = s.getInputStream();
            byte[] received = new byte[2];
            received[0] = (byte) is.read();
            received[1] = (byte) is.read();
            portNumber = ((received[0] & 0xFF) << 8) | (received[1] & 0xFF);
            return portNumber;
        } catch (Exception e) { }
        return -1;
    }

}