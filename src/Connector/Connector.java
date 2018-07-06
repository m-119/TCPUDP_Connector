/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Connector;

import StringWithID.StringWithID;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author C
 */
public class Connector {

	//Сервер и порт
    private static final String SERVER = "127.0.0.1";
	
    //задаем порт
    private static final int PORT_TCP = 9001;
    private static final int PORT_UDP = 9002;

    public static void main(String[] args) throws IOException {

        System.out.println("Посредник запущен");

        //количество клиентов
        int clientNumber = 0;
        
        //сокет
        ServerSocket listener = new ServerSocket(PORT_TCP);
        
        try {
            while (true) {
                //обработка по кругу
                new Transmitter_TCP(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }
    
private static class Transmitter_TCP extends Thread {
        private Socket socket;
        private int clientNumber;

        public Transmitter_TCP(Socket socket, int clientNumber) {
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
		
		String input;
		Transmitter_UDP tudp = new Transmitter_UDP();
		
                //берем записи от клиента и обрабатываем дальше
                while (true) {
                    input = in.readLine();
                    if (input == null || input.equals(".")) {
                        break;
                    }
		    //log(clientNumber + ": " + input);
                    out.println(Transmitter_UDP.toUDP(clientNumber, input));
                }
                //END

                
            } catch (IOException e) {
                log("Ошибка Клиент# " + clientNumber + ": " + e);
            } catch (ClassNotFoundException ex) {
		Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
	    } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Ошибка(не удалось закрыть ): " + e);
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


private static class Transmitter_UDP
{
    public Transmitter_UDP(){};
    public static String toUDP (int client, String str) throws IOException, ClassNotFoundException
	    {
		    	//буфер отправки
	byte[] sendData_UDP = new byte[1024];
	//буфер получения
	byte[] receiveData_UDP = new byte[1024];
	
	StringWithID swid;
	
	//
	BufferedReader dataForSend = new BufferedReader(new InputStreamReader(System.in));
	swid = new StringWithID(client,dataForSend);
	
	DatagramSocket clientSocket = new DatagramSocket();
	
	
	sendData_UDP = StringWithID.serialize(swid);
	
	DatagramPacket sendPacket = new DatagramPacket(sendData_UDP, sendData_UDP.length, InetAddress.getByName(SERVER), PORT_UDP);
	System.out.print("Подготовка к отправке через UDP:" + swid.toString() + "; Клиента#" + swid.toInt());
	clientSocket.send(sendPacket);
	
	DatagramPacket receivePacket = new DatagramPacket(receiveData_UDP, receiveData_UDP.length);
	
	
	while (StringWithID.deserialize(receivePacket.getData()).toInt() == client)
	{
	clientSocket.receive(receivePacket);
	
	String result = StringWithID.deserialize(receivePacket.getData()).toString();
	System.out.println("Полученный ответ: '" + result + "' для Клиента#" + StringWithID.deserialize(receivePacket.getData()).toInt());
	clientSocket.close();
	
	return result;
	
	
	}
	
	return "Досрочный выход из процедуры toUDP";
	}

	private Transmitter_UDP() {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    
}

}