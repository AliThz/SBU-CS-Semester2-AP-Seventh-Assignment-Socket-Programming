package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// Client Class
public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            close(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String clientMessage = scanner.nextLine();
                bufferedWriter.write(username + " :  " + clientMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            close(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String messageFromGroupChat;

            while (socket.isConnected()) {
                try {
                    messageFromGroupChat = bufferedReader.readLine();
                    System.out.println(messageFromGroupChat);
                } catch (IOException e) {
                    close(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    private void close(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username :  ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 2468);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}