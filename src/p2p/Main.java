package p2p;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner S = new Scanner(System.in);
        String fileName, confPath = System.getProperty("user.dir") + "/", testPath = System.getProperty("user.dir") + "/test/";
        String[] ipArr = new String[5];
        int[] portArr = new int[5];
        int userNum;
        
        System.out.println(" ___________________________________________________________________\n"
                         + "|                           < How to use >                          |\n"
                         + "|-------------------------------------------------------------------|\n"
                         + "| > Before the start, set the configuration file (change local IP)  |\n"
                         + "|-------------------------------------------------------------------|\n"
                         + "| > Case 1 : Seeder                                                 |\n"
                         + "| > Use 0 for user ID                                               |\n"
                         + "| > Enter the file name in the folder where the current code exists |\n"
                         + "|-------------------------------------------------------------------|\n"
                         + "| > Case 2 : Leecher                                                |\n"
                         + "| > Use 1, 2, 3 or 4 for user ID                                    |\n"
                         + "| > Enter the file name that you want to download                   |\n"
                         + "| > Warning!  Do not use duplicate number for user number           |\n"
                         + "|___________________________________________________________________|\n"
                         + "\n> Local IP - " + InetAddress.getLocalHost().getHostAddress() + "\n");
        System.out.print("User ID : ");
        userNum = S.nextInt();
        System.out.print("File name : ");
        fileName = S.next();

        // Get user information
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(confPath + "configuration.txt")));
        String text = br.readLine();
        for (int i = 0; i < 4; i++)
            text += " " + br.readLine();
        br.close();

        StringTokenizer token = new StringTokenizer(text, " ", false);
        for (int i = 0; i < 5; i++) {
            ipArr[i] = token.nextToken();
            portArr[i] = Integer.parseInt(token.nextToken());
        }

        FileManager fileManager = new FileManager();

        // Case : Seeder
        if (userNum == 0) {
            File file = new File(testPath + fileName);

            fileManager.setInfo(fileName, userNum);
            fileManager.split(file);

            fileManager.isSeeder = true;
        }

        // Case : Leecher
        else {
            fileManager.setInfo(fileName, userNum);

            int tempPort = portArr[userNum];
            String tempIp = ipArr[userNum];

            portArr[userNum] = portArr[0];
            ipArr[userNum] = ipArr[0];

            portArr[0] = tempPort;
            ipArr[0] = tempIp;
        }

        System.out.println();

        if (!fileManager.isSeeder) {
            Download download_1 = new Download(fileManager, ipArr, portArr);
            Download download_2 = new Download(fileManager, ipArr, portArr);
            Download download_3 = new Download(fileManager, ipArr, portArr);
            download_1.start();
            download_2.start();
            download_3.start();
        }

        ServerSocket welcomeSocket = new ServerSocket(portArr[0]);
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            Upload upload = new Upload(fileManager, connectionSocket);
            upload.start();
        }
    }
}
