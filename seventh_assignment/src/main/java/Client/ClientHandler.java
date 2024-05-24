package Client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<String> messages = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private static File[] listOfFiles;
    private String clientUsername;
    private boolean isOnline;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter((socket.getOutputStream())));
            this.bufferedReader = new BufferedReader(new InputStreamReader((socket.getInputStream())));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            isOnline = false;
            broadcastMessage("Server :  " + clientUsername + " has connected the server !");
        } catch (IOException e) {
            close(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            String request = "";
            try {
                bufferedWriter.write("Menu\n O- Chat\n O- File\n Please enter your command  ");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                request = bufferedReader.readLine();
                switch (request.toLowerCase()) {
                    case "chat":
                        chat();
                        break;
                    case "file":
                        downloadFiles();
                        break;
                    default:
                        bufferedWriter.write("Invalid command");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void chat() {
        showChatHistory(messages.size());
        String clientMessage;
        isOnline = true;

        while (socket.isConnected()) {
            try {
                clientMessage = bufferedReader.readLine();
                if (clientMessage.equalsIgnoreCase("exit")) {
                    isOnline = false;
                    break;
                }
                broadcastMessage(clientUsername + " :  " + clientMessage);
            } catch (IOException e) {
                close(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void downloadFiles() throws IOException {
//        File folder = new File("E:\\Ali\\University\\Shahid Beheshti\\Semester-2\\AdvancedProgramming\\2_TA\\Assignment\\07\\seventh_assignment\\src\\main\\java\\Server\\data");
//        File folder = new File("data\\");
        File folder = new File("seventh_assignment/src/main/java/Server/data");
        listOfFiles = folder.listFiles();
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            while (true) {
                int counter = 0;
                assert listOfFiles != null;
                for (File file : listOfFiles) {
                    counter++;
                    bufferedWriter.write(counter + "." + file.getName());
                    bufferedWriter.newLine();
                }
                bufferedWriter.write("--Exit");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                String command = "";
                command = bufferedReader.readLine();
                if (command.equalsIgnoreCase("exit"))
                    break;
                File file = listOfFiles[Integer.parseInt(command) - 1];
                byte[] myByteArray = new byte[(int) file.length()];
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                bis.read(myByteArray, 0, myByteArray.length);
//                OutputStream os = socket.getOutputStream();
//                System.out.println("Downloading " + file.getName() + "(" + myByteArray.length + " bytes)");
//                os.write(myByteArray,0,myByteArray.length);
//                os.flush();

//                FileWriter downloadedFile = new FileWriter("E:\\Ali\\University\\Shahid Beheshti\\Semester-2\\AdvancedProgramming\\2_TA\\Assignment\\07\\data\\" + file.getName(), false);
//                FileWriter downloadedFile = new FileWriter("../../../../data" + file.getName(), false);
                FileWriter downloadedFile = new FileWriter("data\\" + file.getName(), false);
                PrintWriter printWriter = new PrintWriter(downloadedFile, false);
//                printWriter.write(Arrays.toString(myByteArray),0,myByteArray.length);
                printWriter.write(new String(myByteArray, StandardCharsets.UTF_8));

                downloadedFile.close();
//                if (os != null) os.close();
            }
        } catch (IOException e) {
            close(socket, bufferedReader, bufferedWriter);
        } finally {
            if (bis != null) {
            bis.close();
            }
        }
    }

    public void broadcastMessage(String message) {
        messages.add(message);
        for (var clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername) && clientHandler.isOnline) {
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                close(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("Server :  " + clientUsername + " has left the server");
    }

    private void showChatHistory(int n) {
        for (String m : ClientHandler.messages) {
            if (messages.indexOf(m) < messages.size() - n)
                continue;
            try {
                bufferedWriter.write(m);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void close(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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
}

