/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Connector;

import java.io.*;
import java.net.*;

/**
 *
 * @author C
 */
public class Connector {
    //задаем порт

    private static final int PORT = 9001;

    public static void main(String[] args) throws IOException {

        System.out.println("Посредник запущен");

        //количество клиентов
        int clientNumber = 0;
        
        //сокет
        ServerSocket listener = new ServerSocket(PORT);
        
        try {
            while (true) {
                //обработка по кругу
                new Transmitter(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }
    
private static class Transmitter extends Thread {
        private Socket socket;
        private int clientNumber;

        public Transmitter(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("Новое подключение: Клиент# " + clientNumber + " на " + socket);
        }

        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                
                //Отправка номера клиента в out
                out.println(clientNumber);
                // Приветственные сообщения
                out.println("Добро пожаловать Клиент#" + clientNumber + ".");
                out.println("----------------------->\n");

                //берем записи от клиента и обрабатываем дальше
                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    out.println(input.toUpperCase());
                }
                //END

                
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Ошибка: " + e);
                }
                log("Клиент# " + clientNumber + " закрыл подключение");
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }
}