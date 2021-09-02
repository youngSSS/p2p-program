package p2p;

import java.io.*;
import java.net.*;

public class Upload extends Thread{
    FileManager fileManager;
    Socket connectionSocket;
    long start;

    public Upload (FileManager fileManager, Socket connectionSocket) {
        this.fileManager = fileManager;
        this.connectionSocket = connectionSocket;
        this.setName("Upload Thread-" + fileManager.GetUploadThreadNum());
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                String messageFromClient;
                int chunkIdx;

                fileManager.SetRunningThreadCnt();

                DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                // Receive file name from client
                messageFromClient = inFromClient.readUTF();
                sleep(1000);

                if (messageFromClient.equals(fileManager.fileName)) {
                    outToClient.writeUTF(fileManager.chunkMap);
                }
                else {
                    System.out.println("[" + this.getName() + "]" + messageFromClient + " doesn't exist.");
                    outToClient.writeUTF("No file");
                }

                sleep(100);

                while (true) {
                    messageFromClient = inFromClient.readUTF();

                    while ((messageFromClient).equals(fileManager.fileName)) {
                        sleep(1000);
                        start = System.currentTimeMillis();
                        while (true) {
                            if (((System.currentTimeMillis() - start) / 1000.0 > 5) || (fileManager.GetDownloadCnt() >= 3)) {
                                outToClient.writeUTF(fileManager.chunkMap);
                                sleep(1000);
                                fileManager.ReleaseDownloadCnt();
                                break;
                            }
                        }
                        messageFromClient = inFromClient.readUTF();
                    }

                    System.out.println("[" + this.getName() + "]" + " Receive message from client : " + messageFromClient);
                    sleep(100);
                    chunkIdx = Integer.parseInt(messageFromClient);
                    outToClient.write(fileManager.fileChunks[chunkIdx]);
                    System.out.println("[" + this.getName() + "]" + " Send " + chunkIdx + "th file chunk");
                    sleep(100);

                }

            }
        }
        catch (IOException e) {
            this.interrupt();
            System.out.println("[" + this.getName() + "]" + " This thread's work is done. Kill this thread.");
            System.out.println("Number of running upload thread : " + fileManager.GetRunningThreadCnt());
        }
        catch (InterruptedException e) {
            System.out.println("[" + this.getName() + "]" + " Sleep error");
        }
    }
}
