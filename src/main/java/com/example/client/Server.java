package com.example.client;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server extends Thread {

    private static ServerSocket server; // серверсокет
    private static BufferedReader in; // поток чтения из сокета
    private static BufferedWriter out; // поток записи в сокет
    private static String status;

    @Override
    public void run() {

        try {
            InetAddress host = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
//        Ниже приведён пример создания серверного сокета для 5000 порта.

        try {
            try {
                server = new ServerSocket(8082); // серверсокет прослушивает порт 4004
                System.out.println("Сервер запущен!"); // хорошо бы серверу
                //   объявить о своем запуске
                //сокет для общения
                // accept() будет ждать пока
                //кто-нибудь не захочет подключиться
                try (Socket clientSocket = server.accept()) { // установив связь и воссоздав сокет для общения с клиентом можно перейти
                    // к созданию потоков ввода/вывода.
                    // теперь мы можем принимать сообщения
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    // и отправлять
                    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    while (true) {
                        status = in.readLine(); // ждём пока клиент что-нибудь нам напишет

                        System.out.println(status);
                        // не долго думая отвечает клиенту
                        out.write("Привет, это Сервер! Подтверждаю, вы написали : " + status + "\n");
                        out.flush(); // выталкиваем все из буфера
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally { // в любом случае сокет будет закрыт
                    // потоки тоже хорошо бы закрыть
                    in.close();
                    out.close();
                }
            } finally {
                System.out.println("Сервер закрыт!");
                server.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static String getStatus() {
        return status;
    }
}
