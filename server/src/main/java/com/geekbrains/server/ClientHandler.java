package com.geekbrains.server;

import log.Log4j;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientHandler {
    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String login;
    private String historyFile;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        // /auth login1 pass1 for example
                        if (msg.startsWith("/auth ")) {
                            String[] tokens = msg.split("\\s");
                            String nick = ConnectWithDB.getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                            login = tokens[1];
                            if (nick != null && !server.isNickBusy(nick)) {
                                sendMsg("/authok " + nick);
                                nickname = nick;
                                server.subscribe(this);
                                Log4j.log.info("Клиент " + nickname + " подключился на сервер!");
                                validateHistoryFile(login);
                                break;
                            }
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        if(msg.startsWith("/")) {
                            if (msg.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if(msg.startsWith("/w ")) {
                                String[] tokens = msg.split("\\s", 3);
                                server.privateMsg(this, tokens[1], tokens[2]);
                            }
                        } else {
                            server.broadcastMsg(nickname + ": " + msg);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Клиент с логином " + getLogin() + " завершил свою работу");;
                    Log4j.log.info("Клиент " + nickname + " отключился от сервера");
                } finally {
                    ClientHandler.this.disconnect();
                    ConnectWithDB.disconnect();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            if (msg.startsWith("/")){
                return;
            } else {
                writeBufHistory(msg + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log4j.log.error("Не удалось отправить сообщение!!!");
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void validateHistoryFile(String login){
        historyFile = "C:\\Users\\Ivan Green\\Desktop\\Geek Brains\\brains-chat\\userHistory\\" + login + "_localHistory.txt";
        if ((new File(historyFile)).exists()){
            System.out.println("Файл истории для пользователя " + login + " существует.");
            readHistory();
        } else{
            File file = new File("C:\\Users\\Ivan Green\\Desktop\\Geek Brains\\brains-chat\\userHistory\\" + login + "_localHistory.txt");
            System.out.println("Для пользователя " + login + " создан файл истории: " + file.getName());
        }

    }

    public void writeBufHistory(String msg){
        try {
            FileWriter first = new FileWriter(historyFile,true);
            BufferedWriter writer = new BufferedWriter(first);
            writer.write(msg);
            writer.close();
            System.out.println("История записана в файл!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readHistory(){

        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(historyFile));
            String history;
            while((history = reader.readLine()) != null) server.historyMessage(server.getProfile(login), history);
            Log4j.log.info("История для пользователя записана в файл.");
            System.out.println("История отправлена!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin(){
        return login;
    }
}
