package com.geekbrains.server;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ClientHandler {
    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String login;
    private String historyFile;



    public String getNickname() {
        return nickname;
    }

    public String getLogin(){
        return login;
    }

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
                        // /auth login1 pass1
                        if (msg.startsWith("/auth ")) {
                            String[] tokens = msg.split("\\s");
                            String nick = ConnectWithDB.getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                            login = tokens[1];
                            if (nick != null && !server.isNickBusy(nick)) {
                                sendMsg("/authok " + nick);
                                nickname = nick;
                                server.subscribe(this);
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
                    e.printStackTrace();
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
            if (msg.startsWith("/")){ //чтобы не записывать команды
                return;
            } else {
//                byte[] buf = msg.getBytes(); //способ через перевод в байты
//                writeHistory(buf);
                writeBufHistory(msg + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public void validateHistoryFile(String login){ //определяем надо ли создавать файл
        historyFile = "C:\\Users\\Ivan Green\\Desktop\\Geek Brains\\brains-chat\\userHistory\\" + login + "_localHistory.txt";
        if ((new File(historyFile)).exists()){
            System.out.println("Файл истории для пользователя " + login + " существует.");
            readHistory(); // сюда надо допилить метод по чтению последнних 100 сообщений, потому что файл уже существует.
        } else{
            File file = new File("C:\\Users\\Ivan Green\\Desktop\\Geek Brains\\brains-chat\\userHistory\\" + login + "_localHistory.txt");
            System.out.println("Для пользователя " + login + " создан файл истории: " + file.getName());
        }

    }

//    public void writeHistory(byte[] bw) { //с переводом в байты
//        try (FileOutputStream out1 = new FileOutputStream(historyFile, true)) {
//            out1.write(bw);
//            System.out.println("История записана в файл!");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void writeBufHistory(String msg){ //сам переводит в байты
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

        /**
         * реализованно все, кроме отправки только последних 100 строк истории.
         */
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(historyFile));
            String history;
            while((history = reader.readLine()) != null) server.historyMessage(server.getProfile(login), history);
            System.out.println("История отправлена!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public void readHistory(){ //пытался реализовать чтение последних 10 строк, но сил не хватило
//        int n_lines = 10; //читает почему-то снизу через одну строчку
//        int counter = 0; //в скором времени допишу до конца эту гадость =)
//        try {
//            ReversedLinesFileReader object = new ReversedLinesFileReader(new File(historyFile));
//            while (!object.readLine().isEmpty() && counter < n_lines)
//            {
//                server.historyMessage(server.getProfile(login),object.readLine());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
