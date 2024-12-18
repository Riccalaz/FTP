package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.net.ServerSocket;

public class server {
		
	static class Clienth implements Runnable {

		Socket link;
		
		public Clienth(Socket s) {
			this.link = s;
		}
		
		public void cleanup() {
			try {
				link.close();
			} catch (IOException e) {
			}
		}
		
		@Override
		public void run() {
			BufferedReader reader = null; 
			PrintWriter writer = null;
			String message = null;
			
			String nome = "";
			String[] p=Path.of("").toAbsolutePath().toString().split("\\\\");
			String path=Path.of("").toAbsolutePath().toString();
			Vector<String> pathv=new Vector<String>();
			
			for(int i=0;i<p.length;i++)
			pathv.add(p[i]);
			
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
	            
	            if(message.split(" ").length>1)
	            {
	            	
	            	for(int i=1;i<message.split(" ").length;i++) {
	            		if(i==1)
	            		nome=message.split(" ")[i];
	            		else
	            		nome=nome+" "+message.split(" ")[i];
	            	}
	            }
	            
	            switch(message.split(" ")[0]){
	            
	            case "h":
	            	writer.println("---MANUALE---");
	            	String man="[pwd] stampa il percorso in cui ti trovi/"+
	            	"[ls] stampa il contenuto della directory corrente/"+
	            	"[get *filename*] scarica un file tra quelli disponibili sul server nella directory -scaricati-/"+
	            	"[put *filename*] invia un file al server se presente nella directory -caricabili- che verra scaricato nella directory di lavoro/"+
	            	"[rm *filename*] rimuove un file dal server nella directory di lavoro/"+
	            	"[mkdir *directoryname*] crea una cartella nella directory di lavoro/"+
	            	"[rmdir *directoryname*] rimuove una cartella nella directory di lavoro (se è vuota)/"+
	            	"[cd *directoryname or path*] entra nella directory o nel path inserito/"+
	            	"[cd] esce dalla directory di lavoro/"+
	            	"[close] chiude la connessione con il server";
	            	writer.println(man);
	            	break;
	            	
	            case "get":
	            	if(message.split(" ").length>1) {
	            		File f = new File(path+"\\" + nome);
		                
		                if (f.exists()) {
		                    writer.println("s");
		                    
		                        byte[] buffer = new byte[2048];
					        	 int len = (int) f.length();
					        	 writer.println(len);
					        	 FileInputStream fi = null;
								try {
									fi = new FileInputStream(f);
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								 int bletti = 0;

								 while (bletti != -1) {//invio i byte in modo bufferizzato fino a che non finiscono
								    try {
										bletti = fi.read(buffer);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}  
								    if (bletti == -1) break;
								    try {
										link.getOutputStream().write(buffer, 0, bletti); // Scrivo il blocco letto sul file
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								    try {
										link.getOutputStream().flush();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								  }
		                    
		                } else {
		                    writer.println("n"); 
		                }
	            	}else {
	            		
	            		writer.println("nome file mancante");
	            		
	            	}
	                
	                break;
	                
	            case "pwd":
	            	writer.println(path);
	            	break;
	            	
	            case "ls":
	            	writer.println("contenuto:");
	            	File fl= new File(path);
	            	String[] str=fl.list();
	            	for(int i=0;i<str.length;i++) {
	            		if(new File(path+"\\"+str[i]).isDirectory()) {
	            			str[i]=str[i]+"/";
	            		}
	            	}
	            	String lista=String.join("-", str);
	            	writer.println(lista);
	            	break;
	            	
	            case "cd":
	            	if(message.split(" ").length>1) {
	            		String newp=nome;
	            		if(!newp.split("\\\\")[0].contains(":")) {
	            		File fi= new File(path+"\\"+newp);
		            	if(fi.isDirectory()) {
		            		path=path+"\\"+newp;
		            		if(newp.split("\\\\").length>1) {
		            			for(int i=0;i<newp.split("\\\\").length;i++)
		            				pathv.add(newp.split("\\\\")[i]);
		            		}else {
		            			pathv.add(newp);
		            		}
		            		writer.println(path);
		            	}else {
		            		writer.println("cartella inesistente");
		            	}
	            		}else {
	            			
	            			File fi= new File(newp);
	            			
	            			if(fi.isDirectory()) {
			            		path=newp;
			            		pathv.removeAllElements();
			            		p=path.split("\\\\");
			            		for(int i=0;i<p.length;i++)
			            			pathv.add(p[i]);
			            		writer.println(path);
			            	}else {
			            		writer.println("cartella inesistente");
			            	}
	            			
	            		}
	            	}else {
	            		
		            	if(pathv.size()>1) {
		            		pathv.removeLast();
		            		path="C:\\";
		            		for(int i=1;i<pathv.size();i++)
			            		path=path+"\\"+pathv.get(i);
			            	writer.println(path);
		            	}else {
		            		writer.println("non puoi andare più indietro");
		            	}
	            	}
	            	
	            	break;
	            case "mkdir":
	            	File drpath= new File(path+"\\"+nome);
	            	if (drpath.mkdir()) { //uso mkdir per creare la dir
	            		writer.println("Directory creata");
	            		} else {
	            		writer.println("impossibile creare la directory");
	            		}
	            	break;
	            	
	            case "rmdir":
	            	File dirpath= new File(path+"\\"+nome);
	            	if (dirpath.delete()) {//uso delete per eliminare la directory
	            		writer.println("Directory rimossa");
	            		} else {
	            		writer.println("impossibile rimuovere la directory");
	            		}
	            	break;
	            
	            case "put":
	            	if(message.split(" ").length>1) {
	            		writer.println("file in caricamento");
		            	String m = null;
						try {
							m = reader.readLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
		            	if("s".equalsIgnoreCase(m)) { //se il client invia s
		            		int len=0;
							try {
								len = Integer.parseInt(reader.readLine());
							} catch (NumberFormatException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							File file = new File(path+"\\"+nome);
							 if(!file.exists()) {
								 try {
									file.createNewFile();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}//creo un file nella cartella scaritcati
							 }
							 
							 byte[] buffer = new byte[2048];  
						     FileOutputStream fin = null;
							try {
								fin = new FileOutputStream(file);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						     int lenb = 0;
						     int bletti = 0;

						        while (lenb < len) {//finche i byte letti sono minori della lunghezza
						        	if(buffer.length<len-bletti) {//se i byte di grandezza del buffer sono maggiori di quello che deve essere ancora letto
						        		try {
											bletti = link.getInputStream().read(buffer, 0,buffer.length);//leggo la lunghezza di byte del buffer
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
						        		
						        	}else {
						        		try {
											bletti = link.getInputStream().read(buffer, 0,len-bletti);//leggo i byte rimanenti
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
						        	}
						             
						            if (bletti == -1) break;

						            try {
										fin.write(buffer, 0, bletti);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}  // Scrivi il blocco letto sul file
						            lenb += bletti;
						        }
						       try {
								fin.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						       if(len==lenb)
							   					
						    	   writer.println("s");
						       
						}
	            	}else {
	            		writer.println("nome file mancante");
	            	}
	            	
	            	
	            	break;
	            case "rm":
	            	File rmf= new File(path+"\\"+nome);
	            	if(rmf.isFile()) { //se il file esiste
	            		if(rmf.delete()) { //cerco di rimuovere il file
	            			writer.println("file rimosso correttamente");
	            		}else {
	            			writer.println("file non rimuovibile");
	            		}
	            	}else {
	            		writer.println("file inesistente");
	            	}
	            	break;
	            	default:
	            	writer.println("comando errato"); //se il comando inserito non è giusto invia al server comando errqato
	            }
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
        int port = 32940;

        try {
        	ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server avviato. In attesa");
        	
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("client connesso");
                Thread h = new Thread(new Clienth(client));
                h.start();
            }
        
        } catch (Exception e) {
            System.err.println("Errore server: " + e.getMessage());
            e.printStackTrace();
        }
    }
	}