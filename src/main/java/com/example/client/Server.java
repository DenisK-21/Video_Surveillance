package com.example.client;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server extends Thread {

    private static Socket clientSocket; //сокет для общения
    private static ServerSocket server; // серверсокет
    private static BufferedReader in; // поток чтения из сокета
    private static BufferedWriter out; // поток записи в сокет
    private static int status_cam_1;

    private static int status_cam_2;

    public Server(){
        status_cam_1 = 0;
        status_cam_2 = 0;
    }
    @Override
    public void run() {

        try {
            try  {
                server = new ServerSocket(8082); // серверсокет прослушивает порт 4004
                System.out.println("Сервер запущен!"); // хорошо бы серверу
                int status;
                while (true) {
                    //   объявить о своем запуске
                    clientSocket = server.accept(); // accept() будет ждать пока
                    //кто-нибудь не захочет подключиться
                    try { // установив связь и воссоздав сокет для общения с клиентом можно перейти
                        // к созданию потоков ввода/вывода.
                        // теперь мы можем принимать сообщения
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        // и отправлять
                        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                        status = Integer.parseInt(in.readLine()); // ждём пока клиент что-нибудь нам напишет
                        treatment(status);
                        System.out.println(status);
                        // не долго думая отвечает клиенту
                        out.write("Привет, это Сервер! Подтверждаю, вы написали : " + status + "\n");
                        out.flush(); // выталкиваем все из буфера

                    } finally { // в любом случае сокет будет закрыт
                        clientSocket.close();
                        // потоки тоже хорошо бы закрыть
                        in.close();
                        out.close();
                    }
                }
            } finally {
                System.out.println("Сервер закрыт!");
                server.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    public void treatment(int status){
        if(status / 10 == 1){
            status_cam_1 = status;
            return;
        }
        if (status / 10 == 2){
            status_cam_2 = status;
            return;
        }
        if (status / 10 == 3){
            status_cam_1 = status;
            status_cam_2 = status;
        }
    }

    public static int getStatus(int id) {
        switch (id) {
            case 1 -> {
                return status_cam_1;
            }
            case 2 -> {
                return status_cam_2;
            }
        }
        return 0;
    }
}
