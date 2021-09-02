package p2p;

import java.io.*;
import java.net.*;

public class Download extends Thread {
    FileManager fileManager;
    String[] ipArr;
    int[] portArr;
    boolean isFirst;
    int selectedNeighbor = -1, selectedNeighborRightBefore = -1;

    // Constructor
    public Download (FileManager fileManager, String[] ipArr, int[] portArr) {
        this.fileManager = fileManager;
        this.ipArr = ipArr;
        this.portArr = portArr;
        isFirst = true;
        this.setName("Download Thread-" + fileManager.GetDownloadThreadNum());
    }

    public void run() {
        String messageFromServer, serverChunkMap = "", inFromUser;
        String filePath = System.getProperty("user.dir") + "/test/user_" + fileManager.userNum + "/";
        Socket clientSocket;
        boolean keepConnect = false;
        long start = 0;
        int numOfDownload;

        while (true) {
            try {
                if (fileManager.isComplete()) {
                    fileManager.isSeeder = true;
                    FileOutputStream fw = new FileOutputStream(filePath + fileManager.fileName);
                    for (int i = 0; i < fileManager.chunkMap.length(); i++)
                        fw.write(fileManager.fileChunks[i]);
                    fw.close();

                    if (fileManager.GetIsLast())
                        System.out.println("[" + this.getName() + "]" + "\n-> Download complete");
                    break;
                }

                do {
                    fileManager.ReleasePeer(selectedNeighbor);
                    selectedNeighbor = fileManager.SelectPeer();
                } while (selectedNeighbor == selectedNeighborRightBefore);
                selectedNeighborRightBefore = selectedNeighbor;
                numOfDownload = 0;

                clientSocket = new Socket(ipArr[selectedNeighbor], portArr[selectedNeighbor]);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

                while (numOfDownload < 3) {
                    if (isFirst) {
                        outToServer.writeUTF(fileManager.fileName);
                        messageFromServer = inFromServer.readUTF();

                        if (messageFromServer.equals("No file") || messageFromServer.equals("")) {
                            System.out.println("[" + this.getName() + "]" + " No file in server");
                            sleep(100);
                            break;
                        }
                        else {
                            serverChunkMap = messageFromServer;

                            if (fileManager.isFirst)
                                fileManager.SetSize(serverChunkMap);

                            isFirst = false;
                        }
                    }

                    else {
                        inFromUser = fileManager.GetChunkIdx(serverChunkMap);

                        // Case : server doesn't have required chunk
                        if (inFromUser.equals("-1")) {
                            System.out.println("[" + this.getName() + "]" + " Server doesn't have required chunk.");
                            sleep(100);
                            if (!keepConnect) {
                                start = System.currentTimeMillis();
                                keepConnect = true;
                            }
                            else {
                                if ((System.currentTimeMillis() - start) / 1000 >= 10) {
                                    System.out.println("[" + this.getName() + "]" + " Timeout. Find new peer.");
                                    sleep(100);
                                    keepConnect = false;
                                    break;
                                }
                                else {
                                    outToServer.writeUTF(fileManager.fileName);
                                    sleep(1000);
                                    serverChunkMap = inFromServer.readUTF();
                                }
                            }
                        }

                        // Case : server has required chunk
                        else {
                            outToServer.writeUTF(inFromUser);

                            byte[] tempBuffer = new byte[10240];
                            int size = inFromServer.read(tempBuffer);

                            fileManager.SetFileChunkAndChunkMap(tempBuffer, size, Integer.parseInt(inFromUser));
                            numOfDownload++;
                            keepConnect = false;
                            fileManager.SetDownloadCnt();

                            int chunkCnt = 0;
                            for (int i = 0; i < fileManager.chunkMap.length(); i++) {
                                if (fileManager.chunkMap.charAt(i) == '1') chunkCnt += 1;
                            }
                            System.out.println("[" + this.getName() + "]" + " Download : " + (chunkCnt / serverChunkMap.length() * 100));
                            System.out.println("[" + this.getName() + "]" + " Download " + inFromUser + "th file chunk");
                        }
                    }
                }

                fileManager.ReleasePeer(selectedNeighbor);
                isFirst = true;
                clientSocket.close();
            }
            catch (FileNotFoundException e) {
                System.out.println("[" + this.getName() + "]" + " FileNotFoundException");
            }
            catch (IOException e) {
                fileManager.ReleasePeer(selectedNeighbor);
            }
            catch (InterruptedException e) {
                System.out.println("[" + this.getName() + "]" + " InterruptedException");
            }
        }

        System.out.println("[" + this.getName() + "]" + " This thread's work is done. Kill this thread.");
    }
}
