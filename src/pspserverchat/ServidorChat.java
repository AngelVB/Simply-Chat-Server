package pspserverchat;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import usuario.Usuario;

/**
 * Clase ServidorChat.
 * @author Angel Valera
 */
public class ServidorChat {

   // public static ArrayList<Socket> conex = new ArrayList<>();
    
    //Array de usuarios. De él sacaremos la info a la hora de mandar los datos de un usuario a un cliente.
    public static ArrayList<Usuario> users = new ArrayList<>();
    public static int conexiones=0;
    
    public static void main(String[] args) throws IOException {
        try {
            final int PUERTO = 6000;
            //Creamos un socket de servidor
            ServerSocket server = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado...");
            
            while (true)
            {
               //Aceptamos la conexión por parte del cliente.
                Socket s = server.accept();
              
                //Nos preparamos para recibir el objeto usuario
                InputStream fe = s.getInputStream();
                ObjectInputStream inObjeto = new ObjectInputStream(fe);
                
                //Creamos un nuevo usuario
                Usuario u = new Usuario();
                
                //Leemos el objeto
                u = (Usuario) inObjeto.readObject();
                
                //Lo almacenamos en el array
                users.add(u);
               
                System.out.println(u.getNick()+" conectado al servicio de chat");
                
                conexiones = conexiones+1;
                System.out.println("NÚMERO DE CONEXIONES ACTUALES: " + conexiones);	
                
                //Creamos un nuevo hilo al que le pasamos el socket y el usuario.
                HiloServidor chat = new HiloServidor(s,u);
                Thread t = new Thread(chat);
                
                //Iniciamos el hilo.
                t.start();
              
            }
           
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error");
        }
    }

}
