package p2p;

import java.io.*;
import java.util.*;

public class FileManager {
    String fileName, chunkMap = "";
    int[] using = {0, 0, 0, 0, 0};
    int userNum, downloadCnt = 0, downloadThreadCnt = 0, UploadThreadCnt = 0, runningThreadCnt = 0;
    boolean isSeeder, isFirst, isLast;
    byte[][] fileChunks;

    public FileManager() {
        isSeeder = false;
        isFirst = true;
        isLast = true;
    }

    public void setInfo(String inputFileName, int userNum) {
        this.fileName = inputFileName;
        this.userNum = userNum;
    }

    public synchronized int GetDownloadThreadNum() {
        downloadThreadCnt++;
        return downloadThreadCnt;
    }

    public synchronized boolean GetIsLast() {
        if (isLast) {
            isLast = false;
            return true;
        }
        
        return false;
    }

    public synchronized void SetDownloadCnt() {
        downloadCnt++;
    }

    public synchronized void SetUsing(int index, int changeNum) {
        using[index] = changeNum;
    }

    public synchronized void SetFileChunkAndChunkMap(byte[] tempBuffer, int size, int index) {
        fileChunks[index] = new byte[size];
        fileChunks[index] = Arrays.copyOf(tempBuffer, size);

        if (index == chunkMap.length() - 1)
            chunkMap = chunkMap.substring(0, index) + "1";
        else
            chunkMap = chunkMap.substring(0, index) + "1" + chunkMap.substring(index + 1);
    }

    public synchronized void SetSize(String serverChunkMap) {
        if (isFirst) {
            isFirst = false;
            fileChunks = new byte[serverChunkMap.length()][];

            for (int i = 0; i < serverChunkMap.length(); i++)
                chunkMap += '0';
        }
    }

    public void ReleasePeer(int index) {
        if (0 < index && index < 5)
            SetUsing(index, 0);
    }

    public synchronized int SelectPeer() {
        int selectedNeighbor;
        while (true) {
            selectedNeighbor = (int) (Math.random() * 4);
            if (using[selectedNeighbor] == 0)
                break;
        }
        SetUsing(selectedNeighbor, 1);
        return selectedNeighbor;
    }

    public synchronized String GetChunkIdx(String serverChunkMap) {
        Random random = new Random();
        ArrayList<Integer> needs = new ArrayList<>();
        int r = -1;

        // If server has required chunk, put it in to needs.
        for (int j = 0; j < chunkMap.length(); j++) {
            if (chunkMap.charAt(j) == '0' && serverChunkMap.charAt(j) == '1')
                needs.add(j);
        }

        // Case : server has a required chunk. return chunk index.
        if (needs.size() != 0) {
            r = random.nextInt(needs.size());
            return Integer.toString(needs.get(r));
        }

        // Case : server doesn't have a required chunk. return -1.
        return Integer.toString(r);
    }

    public boolean isComplete() {
        int isComplete = 0;
        for (int i = 0; i < chunkMap.length(); i++) {
            if (chunkMap.charAt(i) == '0')
                isComplete++;
        }

        if (isComplete == 0 && chunkMap.length() != 0) {
            isSeeder = true;
            return true;
        }

        return false;
    }


    // ****************** APIs for upload ******************
    // Split file to 10KB and set chunk map
    public void split(File file) throws IOException {
        FileInputStream fi = new FileInputStream(file);
        BufferedInputStream bi = new BufferedInputStream(fi);

        int chunkNum = (int) file.length() / 10240 + 1;
        int chunkSize = 1024 * 10;
        int readCnt;

        fileChunks = new byte[chunkNum][];
        byte[] tempBuffer = new byte[chunkSize];

        for (int i = 0; i < fileChunks.length; i++) {
            readCnt = bi.read(tempBuffer, 0, chunkSize);
            fileChunks[i] = new byte[readCnt];
            fileChunks[i] = Arrays.copyOf(tempBuffer, readCnt);
            chunkMap += '1';
        }

        bi.close();
        fi.close();
    }

    public synchronized int GetDownloadCnt() {
        return downloadCnt;
    }

    public synchronized void ReleaseDownloadCnt() {
        downloadCnt = 0;
    }

    public synchronized int GetUploadThreadNum() {
        UploadThreadCnt++;
        return UploadThreadCnt;
    }

    public synchronized void SetRunningThreadCnt() {
        runningThreadCnt++;
    }

    public synchronized int GetRunningThreadCnt() {
        return --runningThreadCnt;
    }
}
