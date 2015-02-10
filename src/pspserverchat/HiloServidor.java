package pspserverchat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import usuario.Usuario;

/**
 * Clase HiloServidor
 * @author Angel Valera
 */
public class HiloServidor extends Thread {

    Socket scli = null;
    DataInputStream entrada = null;
    DataOutputStream salida = null;
    Usuario u, uinfo;
    public static ArrayList<HiloServidor> clientesActivos = new ArrayList();
    public static OutputStream fs = null;

    /**
     * Constructor HiloServidor
     * @param s
     * @param us 
     */
    public HiloServidor(Socket s, Usuario us) {
        
        scli = s;
        //Añadimos el hilo al array de hilos.
        clientesActivos.add(this);
        u = us;
        uinfo = null;
    }

    @Override
    public void run() {

        System.out.println("Esperando Mensajes");
        try
        {
            
            entrada = new DataInputStream(scli.getInputStream());
            salida = new DataOutputStream(scli.getOutputStream());
            enviaMsg("Entra en el chat " + u.getNick());
            String mencli = "";

            while (true)
            {
                if (!scli.isClosed()) {
                    mencli = entrada.readUTF();
                    System.out.println("mensaje recibido " + mencli);

                    //Comprobamos si el mensaje recibido es la cadena de desconexión
                    if (mencli.equals("*")) {
                        enviaMsg("Abandona el chat " + u.getNick());

                        HiloServidor user = this;
                        //Eliminamos nuestro hilo del array
                        clientesActivos.remove(user);
                        //Cerramos el socket
                        user.scli.close();
                        //Interrumpimos el hilo.
                        user.interrupt();
                    } else if (mencli.length() > 5) {
                        
                        //Pasamos el mensaje a minúsculas para que no importe cómo hayan escrito "info" y el nick del usuario"
                        String msgtemp = mencli.toLowerCase();
                        //Si la cadena recibida empieza por info, enviamos los datos del usuario.
                        if (msgtemp.substring(0, 5).equals("info ")) {
                            //Extraemos el nick del mensaje recibido
                            String Infouser = msgtemp.substring(5);
                            System.out.println("INFO USUARIO:" + Infouser);
                            boolean enviado = false;
                            
                            //Recorremos el Array de usuarios del server para ver si el usuario existe
                            for (Usuario u2 : ServidorChat.users) {
                                String nicktemp = u2.getNick().toLowerCase();
                               
                                //Si el usuario existe, llamamos a la función EnviaUsuario.
                                if (nicktemp.equals(Infouser)) {
                                    enviaUsuario(u2);
                                    enviado = true;
                                }
                            }
                            //Si no existe, avisamos al cliente que ha solicitado la información.
                            if (!enviado) {
                                this.salida.writeUTF("¥¬¥Ningún usuario activo con el nick: " + Infouser);
                            }
                        } else {
                            //Si el mensaje no es ni * ni "Info", enviamos el mensaje a todos los clientes.
                            enviaMsg(mencli);
                        }
                    } else {
                        //Si el mensaje no es ni * ni "Info", enviamos el mensaje a todos los clientes.    
                        enviaMsg(mencli);

                    }
                    mencli = "";
                }
            }
        } catch (IOException e) {
        }
    }
    /**
     * Método para enviar los datos de usuario al cliente.
     * @param user2 
     */
    public void enviaUsuario(Usuario user2) {

        System.out.println("ENVIANDO DATOS DE " + user2.getNick() + " A " + u.getNick());
        try {
            //Enviamos una cadena de texto al cliente para que se prepare para recibir el objeto.
            this.salida.writeUTF("[info]");
            fs = scli.getOutputStream();
            ObjectOutputStream outObjeto = new ObjectOutputStream(fs);
            //Enviamos el objeto usuario.
            outObjeto.writeObject(user2);

        } catch (IOException e) {
        }
    }
    
    /**
     * Método para enviar el mensaje a todos los clientes.
     * @param mencli2 
     */
    public void enviaMsg(String mencli2) {
        HiloServidor user = null;
        
        //Recorremos el array de hilos.
        for (HiloServidor clientesActivo : clientesActivos) {
            System.out.println("MENSAJE DEVUELTO:" + mencli2);
            try {
                user = clientesActivo;
                
                //Si el usuario es el mismo al que envía el mensaje, añadimos una cadena inicial para que el cliente identifique el mensaje
                //como propio
                if (user.equals(this)) {
                    user.salida.writeUTF("¥¬¥" + mencli2);
                } else {
                    //Si el mensaje es de entrada o de desconexión, lo enviamos sin más.
                    if (mencli2.equals("Abandona el chat " + u.getNick()) || mencli2.equals("Entra en el chat " + u.getNick())) {
                        user.salida.writeUTF(mencli2);
                    //Si el mensaje es "normal", le añadimos el nick del usuario al principio.
                    } else {
                        user.salida.writeUTF(u.getNick() + "> " + mencli2);
                    }
                }
            } catch (IOException e) {
            }
        }
    }
}
