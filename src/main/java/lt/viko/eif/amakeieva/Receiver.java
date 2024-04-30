package lt.viko.eif.amakeieva;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(3031)) {
            System.out.println("Receiver Listening on port 3031. Waiting for client connection...\n");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {  // Accept client connections
                    System.out.println("Client connected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                    // Read data from the client
                    String receivedPublicKey = reader.readLine();  // Read the public key
                    String receivedSignature = reader.readLine();  // Read the signature
                    String receivedMessage = reader.readLine();    // Read the message

                    // Print received data for debugging
                    System.out.println("Received public key: " + receivedPublicKey);
                    System.out.println("Received signature: " + receivedSignature);
                    System.out.println("Received message: " + receivedMessage);

                    // Verify the signature
                    boolean result = verifySignature(receivedMessage, receivedSignature, receivedPublicKey);
                    System.out.println(result ? "Signature verification successful" : "Signature verification failed");

                    // Send the verification result back to the server
                    writer.println(result ? "Signature verification successful" : "Signature verification failed");
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Receiver Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean verifySignature(String message, String signature, String publicKeyStr) {
        try {
            String[] keys = publicKeyStr.split(",");
            BigInteger e = new BigInteger(keys[0]);
            BigInteger n = new BigInteger(keys[1]);
            String[] signatureParts = signature.split(" ");

            StringBuilder decryptedMessageBuilder = new StringBuilder();
            for (String part : signatureParts) {
                BigInteger sigPart = new BigInteger(part);
                BigInteger decryptedChar = sigPart.modPow(e, n);
                decryptedMessageBuilder.append((char) decryptedChar.intValue());
            }
            return decryptedMessageBuilder.toString().equals(message);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid signature format: " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
