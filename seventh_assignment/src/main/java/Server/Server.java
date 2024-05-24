package Server;

import Client.ClientHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// Server Class
public class Server {
    private Socket clientSocket;
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                clientSocket = serverSocket.accept();
                System.out.println("a new client has connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    private void closeServerSocket() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2468);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}