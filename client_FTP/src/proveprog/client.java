package proveprog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class client {
		
		public static void attendi(long ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
			}
		}

		public static void main(String[] args) {
			String serverAddress = "127.0.0.1";
			int port = 32940; 
			Scanner myObj = new Scanner(System.in);
			
			Socket link = null;
			int i = 0;
			
			while( link == null && i < 3) {
				try {
					link = new Socket(serverAddress, port);
				}catch(IOException ex) {
					ex.printStackTrace();
					attendi((long) (1000*Math.pow(2, i)));
				}
				i++;
			}
			
			if( link == null ) {
				System.out.println("Impossibile collegarsi al server");
				return;
			}
			
			try {

					// prendo l'output stream per inviare dati
					OutputStream out = link.getOutputStream();

					// prendo l'output stream per ricevere dati
					InputStream in = link.getInputStream();

					// Usa un PrintWriter per mandare righe al server
					PrintWriter writer = new PrintWriter(out, true);
					// Usa un BufferReader per leggere righe di testo al server
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					
					//path della cartella in cui finiscono i file scaricati
					String paths=Path.of("").toAbsolutePath().toString()+"\\scaricati\\";
					//path della cartella in cui i file da caricare sono presenti
					String pathc=Path.of("").toAbsolutePath().toString()+"\\caricabili\\";
					boolean conn=true;
					
					
					while(conn) {
						System.out.print("FTP>>");
						String v=myObj.nextLine();
						writer.println(v);	//invio del comando al server
						String mess=reader.readLine();//lettura risposta dal server
						String[] s = v.split("\\s+"); //divisione del comando dall'argomento
						
						switch(s[0]) {//switch in cui viene secondo il comando inserito il client esegue alcune azioni
						case "get":
							if(s.length>1) {
								if("s".equalsIgnoreCase(mess)) {//se riceve dal server s significa che il file è prensete
									int len=Integer.parseInt(reader.readLine());
									File file = new File(paths+s[1]);
									 if(!file.exists()) {
										 file.createNewFile();//creo un file nella cartella scaritcati
									 }
									 
									 byte[] buffer = new byte[2048];  
								     FileOutputStream fin = new FileOutputStream(file);
								     int lenb = 0;
								     int bletti = 0;

								        while (lenb < len) { //finche i byte letti sono minori della lunghezza
								        	if(buffer.length<len-bletti) { //se i byte di grandezza del buffer sono maggiori di quello che deve essere ancora letto
								        		bletti = in.read(buffer, 0,buffer.length);//leggo la lunghezza di byte del buffer
								        		
								        	}else {
								        		bletti = in.read(buffer, 0,len-bletti);//leggo i byte rimanenti
								        	}
								             
								            if (bletti == -1) break;

								            fin.write(buffer, 0, bletti);  // Scrivi il blocco letto sul file
								            lenb += bletti;
								        }
								       fin.close();
								       if(len==lenb)
									   System.out.println("file scaricato");
										
									
								}else if("n".equalsIgnoreCase(mess)){
									System.out.println("file inesistente o non scaricabile");
								}
							}else {
								
								System.out.println(mess);
								
							}
							
							break;
						case "put":
							if(s.length>1) {

								 File f = new File(pathc+v.split(" ")[1]); 
							        if (f.exists()) { //controllo che il file che voglio inserire esita
							        	System.out.println(mess);
							        	 writer.println("s");
							        	  
							        	 byte[] buffer = new byte[2048];
							        	 int len = (int) f.length();
							        	 writer.println(len);
							        	 FileInputStream fi = new FileInputStream(f);
										 int bletti = 0;

										 while (bletti != -1) { //invio i byte in modo bufferizzato fino a che non finiscono
										    bletti = fi.read(buffer);  
										    if (bletti == -1) break;
										    out.write(buffer, 0, bletti);  // Scrivo il blocco letto sul file
										    out.flush();
										  }
										  fi.close();
							        	  String risposta=reader.readLine();
							        	  if(risposta.equalsIgnoreCase("s")) {
							        		  System.out.println("caricato con successo");
							        	  }
							        	  else {
							        		  System.out.println("file non caricato");
							        	  }
							          } else {
							              writer.println("n");
							              System.out.println(mess);
							              System.out.println("file inesistente");
							          }
							}else{
								System.out.println(mess);
							}
							
							break;
						case "ls":
							System.out.println(mess);
							String st=reader.readLine();//ricevo la lista di file e directory
							String[] ls =st.split("-");//suddivido la lista di file e directory
							for(int j=0;j<ls.length;j++)
							System.out.println(ls[j]);
							break;
						case "h":
							System.out.println(mess);
							String man=reader.readLine();
							String[] manuale =man.split("/");//suddivido il manuale
							for(int j=0;j<manuale.length;j++)
							System.out.println(manuale[j]);
							break;
						case "close":
							writer.println("");//invio al server il messaggio per chiudere la connessione
							System.out.println("connessione con il server chiusa");
							reader.close();
							writer.close();
							link.close();
							conn=false;//chiudo la connessione
							break;
						default:
							System.out.println(mess);	
						}
						
					}
				
						
					
			} catch (Exception e) {
				// Gestisce eventuali errori
				System.err.println("Errore: " + e.getMessage());
				e.printStackTrace();
			}
		}

	}
