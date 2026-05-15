package client;

import java.io.*;
import java.net.Socket;

public class ClinetSocket {
   public static void main(String[] args) throws Exception{
       Socket socket = new Socket("localhost", 8000);
       BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
       bufferedWriter.write("GET / HTTP/1.0\n\n");
       bufferedWriter.flush();
   }
}