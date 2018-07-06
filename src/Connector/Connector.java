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
    
	
	private static final String SERVER = "127.0.0.1";
	//задаем порт
	private static final int PORT = 9001;
	private static final int PORT_UDP = 9002;

    public static void main(String[] args) throws IOException {

        System.out.println("Посредник запущен");

        //количество клиентов
        int clientNumber = 0;
        
        //сокет
        ServerSocket listener = new ServerSocket(PORT);
        
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
		
		Transmitter_UDP tUDP = new Transmitter_UDP(clientNumber, in, out);
                
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
                    tUDP.transmit(input);
                }
                //END

                
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } catch (ClassNotFoundException ex) {
		Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
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

private static class Transmitter_UDP {
    int clientNumber;
    BufferedReader in;
	    PrintWriter out;
    
    public Transmitter_UDP(int clientNumber, BufferedReader in, PrintWriter out)
    {
	this.clientNumber = clientNumber;
	this.in = in;
	this.out = out;
    }
    
    public void transmit(String str) throws SocketException, IOException, ClassNotFoundException{
	//буфер отправки
	byte[] sendData_UDP = new byte[1024];
	//буфер получения
	byte[] receiveData_UDP = new byte[1024];
	StringWithID swid = new StringWithID(clientNumber,str);
	
	DatagramSocket clientSocket = new DatagramSocket();
	
	
	sendData_UDP = StringWithID.serialize(swid);
	
	DatagramPacket sendPacket = new DatagramPacket(sendData_UDP, sendData_UDP.length, InetAddress.getByName(SERVER), PORT_UDP);
	clientSocket.send(sendPacket);
	
	System.out.print("Подготовка к отправке через UDP:" + swid.toString() + "; Клиента#" + swid.toInt());
	
	DatagramPacket receivePacket = new DatagramPacket(receiveData_UDP, receiveData_UDP.length);
	clientSocket.receive(receivePacket);
	
	swid = StringWithID.deserialize(receivePacket.getData());
	if (swid.toInt()==this.clientNumber)
	{
	String result = StringWithID.deserialize(receivePacket.getData()).toString();
	this.out.println(result);
	clientSocket.close();
	}
    }
    
}
}