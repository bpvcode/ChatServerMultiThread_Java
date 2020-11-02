package org.academiadecodigo.anderdogs.ChatConcurrence;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Clients implements Runnable {

    //Properties
    private String hostname = getHost();
    private int portNumber = getPortNumber();
    private Socket clientSocket;
    private BufferedReader inputBufferedReader;
    private BufferedWriter outputBufferedWriter;
    private String name;

    //Constructor
    public Clients(){
        setName();
    }
    //Methods

    private static String getHost(){
        System.out.print("Hostname? ");
        Scanner reader = new Scanner(System.in);
        return reader.nextLine();
    }

    private static String getMessage(){
        //System.out.println("Type your message please: ");
        Scanner reader = new Scanner(System.in);
        return reader.nextLine();
    }

    private static int getPortNumber(){
        System.out.print("Port? ");
        Scanner reader = new Scanner(System.in);
        int number = Integer.parseInt(reader.nextLine());
        return number;
    }

    private void setClientSocket(){
        try {
            clientSocket = new Socket(hostname,portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setChangingInfo(){ //Deveria ser INPUTDATASTREAM e OUTPUTDATASTREAM para podermos enviar imagens???
        try {
            inputBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputBufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inputReceiver(){
        try {

                String messageReceived = inputBufferedReader.readLine();
                if(messageReceived == null){
                    System.err.print("Leaving Chat...");
                    System.exit(1);
                }else {
                    System.out.println(messageReceived);
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputSender(){
            String messageToSend =getName() + ": " + getMessage() + "\n";
            try {

                    outputBufferedWriter.write(messageToSend, 0, messageToSend.length());
                    outputBufferedWriter.flush();

                    if (messageToSend.contains("/quit")) {
                        this.inputBufferedReader.close();
                        this.outputBufferedWriter.close();
                        this.clientSocket.close();
                        System.out.println("All closed for: " + getName());
                        System.exit(1);
                    }

            } catch (IOException e) {
                e.printStackTrace();
            }

    }
    private String setName(){
        String [] names = {"Bruno","Alex","Vasco","João"};
        int random = (int) Math.floor(Math.random()*4);
        name = names[random];
        return name;
    }

    private String getName(){
        return name;
    }

    private void setAll(){
        setClientSocket();
        setChangingInfo();
    }

    private void init(){
        while (clientSocket.isBound()) {
            inputReceiver();
        }
    }

    @Override
    public void run() {
        while(clientSocket.isBound()) {
            outputSender();
        }
    }


    public static void main(String[] args) {
        Clients clients = new Clients();
        Thread thread = new Thread(clients);
        clients.setAll();
        thread.start();
        clients.init();
    }

}
