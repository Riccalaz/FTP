package server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.ServerSocket;

public class server {
	
	
	public static final int CLIENT_DELAY = 200;

	public static void attendi(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}
	
	
	static class NClients {
		int i = 0;
		
		synchronized void inc() {
			i++;
		}
		
		synchronized void dec() {
			i--;
		}
		
		synchronized int get() {
			return i;
		}
	}
	
	static class ClientHandler implements Runnable {

		Socket link;
		NClients counter;
		
		public ClientHandler(Socket s, NClients v) {
			this.link = s;
			this.counter = v;
		}
		
		public void cleanup() {
			try {
				link.close();
			} catch (IOException e) {
			}
			counter.dec();
		}
		
		@Override
		public void run() {
			BufferedReader reader = null; 
			PrintWriter writer = null;
			String message = null;
			try {
				reader = new BufferedReader(new InputStreamReader(link.getInputStream()));
				writer = new PrintWriter(link.getOutputStream(), true);
			}catch(IOException ex) {
				ex.printStackTrace();
				System.out.println("Client died too early, cleaning up the mess!");
				cleanup();
				return;
			}
			
			while("".equalsIgnoreCase(message) == false) {
	            try {
					message = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
	            
	            switch(message){
	            case "h":
	            	writer.println("manuale");
	            	break;
	            case "get":
	            	writer.println("scaricamitutto");
	            	 try {
						Files.copy(Paths.get("ciao.txt"), link.getOutputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	break;
	            }
	            // Legge il messaggio dal client
			}
			
        	try {
				reader.close();
	        	writer.close();
			} catch (IOException e) {
			}
        	cleanup();
        	
		}
	}
	
    public static void main(String[] args) {
        int port = 12345; // Porta su cui il server ascolta
        NClients clients = new NClients();

        try {
        	ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server avviato. In attesa di connessioni...");
        	
            while (true) {
                // Accetta e aspetta la connessione di un client una nuova connessione dal client
                Socket client = serverSocket.accept();
                System.out.println("Nuovo client connesso.");
                clients.inc();
                Thread handler = new Thread(new ClientHandler(client,clients));
                handler.start();
            }
        
        } catch (Exception e) {
            System.err.println("Errore del server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

