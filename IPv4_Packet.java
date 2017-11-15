/* CS 380 - Computer Networks
 * Project 5 : IPv4 w/ UDP
 * Ismail Abbas
 */

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
 
public class Ipv4_Packet {
	private static int packetSize = 2;
    private static String recievedResponse = "empty";
	private static final byte OFFSET = 0;
    private static final int TTL = 50;
	// This assignment has UDP protocol
    private static final int PROTOCOL = 17;
    private static final int VERSION = 4;
    private static final int H_LEN = 5;
    private static final int TOS = 0;
    private static final byte INDENT = 0;
    private static final int FLAG = 2;
   
   // Default address for Google
    private static final byte[] SRC_ADDR = {(byte) 172, (byte) 217, 11, 78};
    private static byte[] DEST_ADDR;

	/*
	 * This constructor is the default, allowing for the instantiation of the destination address
	 *
	 */
    public Ipv4_Packet(Socket s) {
        DEST_ADDR = s.getInetAddress().getAddress();
    }

	/*
	 * This method will generate the UDP packets that will be sent. The specifications 
	 * are based on standard UDP practices
	 */
    public byte[] generateUDPPacket(int size, byte[] udp) {
        int length = udp.length + 20;
        byte[] packet = new byte[length];

		// Gotta do it by the book of course
        packet[0] = (VERSION * 16) + H_LEN;
        packet[1] = TOS;
        packet[2] = (byte) ((length >>> 8) & 0xFF);
        packet[3] = (byte) (length & 0xFF);
        packet[4] = INDENT;
        packet[5] = INDENT;
        packet[6] = (byte) (FLAG * 32);
        packet[7] = OFFSET;
        packet[8] = TTL;
        packet[9] = PROTOCOL;

        int count = 0;
		// for each position in the packet array, increase the source address count by 1
        for(int i = 12; i < 16; ++i) {
            packet[i] = SRC_ADDR[count++];
        }
		// restart count so it can be done again
        count = 0;
        for(int k = 16; k < 20; ++k) {
            packet[k] = DEST_ADDR[count++];
        }

        byte[] checkSum = getCheck(packet);
        packet[10] = checkSum[0];
        packet[11] = checkSum[1];

        // generate Data
        count = 0;
        for(int i = 20; i < packet.length; ++i) {
            packet[i] = udp[count++];
        }

        return  packet;
    }

	/*
	 * This helper method will return the check in byte
	 * array form so the packet can be filled in right
	 */
    private byte[] getCheck(byte[] packet) {
        short checkSum = checkSum(packet);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(checkSum);
        return buffer.array();
    }

	/*
	 * This method will perform the handshake and return the packet.
	 * --> (a) build the packet --> (b) calc checksum --> (c) return the goods
	 */
    public byte[] genHandShake() {

        int length = 4 + 20;
        byte[] packet = new byte[length];

        packet[0] = (VERSION * 16) + H_LEN;
        packet[1] = TOS;
        packet[2] = (byte) (length >> 8);
        packet[3] = (byte) length;
        packet[4] = INDENT;
        packet[5] = INDENT;
        packet[6] = (byte) (FLAG * 32);
        packet[7] = OFFSET;
        packet[8] = TTL;
        packet[9] = PROTOCOL;

        int count = 0;
        for(int i = 12; i < 16; ++i) {
            packet[i] = SRC_ADDR[count++];
        }
        count = 0;
        for(int k = 16; k < 20; ++k) {
            packet[k] = DEST_ADDR[count++];
        }

        byte[] checkSum = getCheck(packet);
        packet[10] = checkSum[0];
        packet[11] = checkSum[1];

        packet[20] = (byte)0xDE;
        packet[21] = (byte)0xAD;
        packet[22] = (byte)0xBE;
        packet[23] = (byte)0xEF;

		// Finally
        return packet;
    }
    // Will calculate the check sum for the packet
	/*
	 * This method will do the calculations for the check for the packet
	 */
    public short checkSum(byte[] b) {
        long sum = 0;
        int i = 0;
        long highVal, lowVal, value;
		int length = b.length;

		// While the length of the byte is greater than one,
		// perform the additions, check for overflow, and deal 
		// with the leftover bits
        while(length > 1){
			// add the separate parts of the byte, then add that to the sum
            highVal = ((b[i] << 8) & 0xFF00);
            lowVal = ((b[i + 1]) & 0x00FF);
            value = highVal | lowVal;
            sum += value;

            // overflow funnnnnnnnnnnnnnnnnnnnnn check
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
            i += 2;
            length -= 2;
        }
        // for the leftover bits, add them to the sum as well
        if(length > 0){
            sum += (b[i] << 8 & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }
        sum = ~sum;
        sum = sum & 0xFFFF;
		// this could have been a long as well if needed
        return (short)sum;
    }

		// This method simply returns the destination address
    public byte[] getDestintionAddr() {
        return DEST_ADDR;
    }
	// This method simply returns the source address
    public byte[] getSourceAddr() {
        return SRC_ADDR;
    }
}