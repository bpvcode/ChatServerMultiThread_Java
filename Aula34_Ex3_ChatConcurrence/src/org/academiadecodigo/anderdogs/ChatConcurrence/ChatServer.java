package org.academiadecodigo.anderdogs.ChatConcurrence;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    //Properties
    private int portNumber = getPortNumber();
    ServerSocket serverSocket;
    Socket clientSocket;
    ServerWorker serverWorker;
    private LinkedList<ServerWorker> linkedList;

    //Constructor

    //Methods
    private static int getPortNumber(){
        System.out.print("Port? ");
        Scanner reader = new Scanner(System.in);
        int number = Integer.parseInt(reader.nextLine());
        return number;
    }

    private void setServerToStayListen(){
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Binding on port: " + serverSocket.getLocalPort());
            System.out.println("Server started: ServerSocket [" + serverSocket.getLocalSocketAddress()+"]");
            System.out.println("Waiting for a client connection...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setClientSocket(){
        clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
            System.out.println("Client Accepted: Socket [" + clientSocket.getLocalSocketAddress() + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendAll(String messageReceive){
        for(ServerWorker s: linkedList){
            s.send(messageReceive);
        }
    }

    protected void init(){
        setServerToStayListen();
        ExecutorService fixedPool = Executors.newFixedThreadPool(4);
        linkedList = new LinkedList<ServerWorker>();
        while (true){
            setClientSocket();
            serverWorker = new ServerWorker(clientSocket);
            linkedList.add(serverWorker);
            fixedPool.execute(serverWorker);
        }
    }

    // =============================== SERVER WORKER - Inner Class =============================== \\


    public class ServerWorker implements Runnable {

        //Properties
        private Socket clientSocket;
        private BufferedReader inputBufferedReader;
        private BufferedWriter outputBufferedWriter;


        //Constructor
        public ServerWorker(Socket clientSocket){
            this.clientSocket = clientSocket;
        }

        //Methods

        private void setChangingInfo(){ //Deveria ser INPUTDATASTREAM e OUTPUTDATASTREAM para podermos enviar imagens???
            try {
                inputBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outputBufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String inputReceiver(){ //POSSO ESTAR A RECEBER SÒ UMA LINHA???? OU LÊ VARIAS LINHAS VISTO QUE NUM CHAT SO SE CONVERSA NUMA LINHA???
            String messageReceived = "";
            try {
                messageReceived = inputBufferedReader.readLine() + "\n";
                System.out.println(Thread.currentThread().getName()+": "+messageReceived);

                if(messageReceived.contains("/quit") || messageReceived.equals(null)){
                    this.inputBufferedReader.close();
                    //this.outputBufferedWriter.close();
                    this.clientSocket.close();
                    linkedList.remove(this);
                    System.out.println("All closed for: "+ Thread.currentThread().getName());

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return messageReceived;
        }



        private void send(String messageReceived){
            try {
                outputBufferedWriter.write(messageReceived);
                outputBufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() { //ISTO È DE CADA SERVER WORKER / CLIENT
            setChangingInfo();
            while(clientSocket.isBound()) {
                String messagereceived = inputReceiver();
                if(messagereceived.equals(null + "\n")  || messagereceived.contains("/quit")){
                    break;
                }

                sendAll(messagereceived);
            }
        }

    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.init();
    }


}
