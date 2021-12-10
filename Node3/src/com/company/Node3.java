package com.company;

import java.io.*;
import java.net.*;

class myData implements Serializable {
    public int[] minCost;
    public int[] export;
    public int Source;

    public myData() {
        Source = 3;
        minCost = new int[4];
        export = new int[4];
    }
}

public class Node3 {

    static final int NUMOFROUTERS = 4;
    private final int MAX_SIZE = 1024;
    static final String Address = "127.0.0.1";
    static final int Node0Port = 1000;
    static final int Node1Port = 1001;
    static final int Node2Port = 1002;
    static final int Node3Port = 1003;

    static final int MAX = 999999;

    int sendCount = 1;
    int ReceiveCount = 0;
    boolean isChanged = false;


    myData DataPack;

    DatagramSocket serverSocket;

    {
        try {
            serverSocket = new DatagramSocket(Node3Port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    byte[] sendData;
    byte[] receiveData;
    DatagramPacket sendPacket;
    DatagramPacket receivePacket;

    public void Init() throws Exception {
        DataPack = new myData();

        //到其他节点到最小代价
        DataPack.minCost[0] = MAX;
        DataPack.minCost[1] = 7;
        DataPack.minCost[2] = MAX;
        DataPack.minCost[3] = 0;

        //到其他节点最小代价路径的出口
        DataPack.export[0] = -1;
        DataPack.export[1] = 1;
        DataPack.export[2] = -1;
        DataPack.export[3] = 3;

        System.out.println("Node3节点启动，路由表如下：");
        System.out.println("******************************************");
        System.out.println("节点\t\t最短路径代价\t\t出口");
        for (int i = 0; i < NUMOFROUTERS; i++) {
            System.out.println(i + "\t\t" + DataPack.minCost[i] + "\t\t" + DataPack.export[i]);
        }
        System.out.println("******************************************");
        System.out.println();

        Send();
    }

    public void Update() throws Exception {
        Send();
        sendCount++;
    }

    private void Send() throws IOException, InterruptedException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
        oos.writeObject(DataPack);
        oos.flush();
        byte[] sendData = bos.toByteArray();

        InetAddress DestAddr = InetAddress.getByName(Address);
//        sendPacket = new DatagramPacket(sendData, sendData.length, DestAddr, Node0Port);
//        serverSocket.send(sendPacket);
//        Thread.sleep(1000);//毫秒
        sendPacket = new DatagramPacket(sendData, sendData.length, DestAddr, Node1Port);
        serverSocket.send(sendPacket);
//        Thread.sleep(1000);//毫秒
//        sendPacket = new DatagramPacket(sendData, sendData.length, DestAddr, Node2Port);
//        serverSocket.send(sendPacket);
    }

    class myThread implements Runnable {
        private Thread t;
        int flag = -1;

        myThread(int i) {
            flag = i;
        }

        @Override
        public void run() {

            if (flag == 0) {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        Send();
                        System.out.println("send");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ((flag == 1)) {
                try {
                    Thread.currentThread();
                    Thread.sleep(5000);//毫秒
                    Init();

                    while (true) {
                        isChanged = false;
                        receiveData = new byte[MAX_SIZE];
                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        ReceiveCount++;

                        byte[] datas = receivePacket.getData();
                        int len = receivePacket.getLength();

                        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(datas)));
                        Object temp = ois.readObject();

                        myData dataPack = (myData) temp;

                        int[] cost = new int[NUMOFROUTERS];
                        int[] export = new int[NUMOFROUTERS];
                        int tempSource = dataPack.Source;

                        for (int i = 0; i < NUMOFROUTERS; i++) {
                            cost[i] = dataPack.minCost[i];
                            export[i] = dataPack.export[i];
                        }

                        System.out.println("收到来自节点" + tempSource + "的数据包");
                        if (tempSource == 0) {
                            if (DataPack.minCost[0] + cost[1] < DataPack.minCost[1]) {//通过0到达1
                                DataPack.minCost[1] = DataPack.minCost[0] + cost[1];
                                DataPack.export[1] = 0;
                                System.out.println("更新Node3到Node1的代价和出口");
                                isChanged = true;
                            }
                            if (DataPack.minCost[0] + cost[2] < DataPack.minCost[2]) {//通过0到达2
                                DataPack.minCost[2] = DataPack.minCost[0] + cost[2];
                                DataPack.export[2] = 0;
                                System.out.println("更新Node3到Node2的代价和出口");
                                isChanged = true;
                            }
                        }

                        if (tempSource == 1) {
                            if (DataPack.minCost[1] + cost[0] < DataPack.minCost[0]) {//通过1到达0
                                DataPack.minCost[0] = DataPack.minCost[1] + cost[0];
                                DataPack.export[0] = 1;
                                System.out.println("更新Node3到Node0的代价和出口");
                                isChanged = true;
                            }
                            if (DataPack.minCost[1] + cost[2] < DataPack.minCost[2]) {//通过1到达2
                                DataPack.minCost[2] = DataPack.minCost[1] + cost[2];
                                DataPack.export[2] = 1;
                                System.out.println("更新Node3到Node2的代价和出口");
                                isChanged = true;
                            }
                        }
                        if (tempSource == 2) {
                            if (DataPack.minCost[2] + cost[0] < DataPack.minCost[0]) {//通过2到达0
                                DataPack.minCost[0] = DataPack.minCost[2] + cost[0];
                                DataPack.export[0] = 2;
                                System.out.println("更新Node3到Node0的代价和出口");
                                isChanged = true;
                            }
                            if (DataPack.minCost[2] + cost[1] < DataPack.minCost[1]) {//通过2到达1
                                DataPack.minCost[1] = DataPack.minCost[2] + cost[1];
                                DataPack.export[1] = 2;
                                System.out.println("更新Node3到Node1的代价和出口");
                                isChanged = true;
                            }
                        }

                        if (isChanged) {
                            Update();
                            System.out.println();
                            System.out.println("路由表更新完成，更新后的路由表如下：");
                            System.out.println("节点\t\t最短路径代价\t\t出口");
                            for (int i = 0; i < NUMOFROUTERS; i++) {
                                System.out.println(i + "\t\t" + DataPack.minCost[i] + "\t\t" + DataPack.export[i]);
                            }
                            System.out.println("****************************");
                            System.out.println("发送更新给其邻接点");

                        }
                        System.out.println();
                        System.out.println();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void start() {
            if (t == null) {
                t = new Thread(this);
                t.start();
            }
        }
    }

    public Node3() throws Exception {
        myThread t1 = new myThread(1);
        t1.start();
        myThread t2 = new myThread(1);
        t2.start();
    }

    public static void main(String args[]) throws Exception {
        Node3 Node3 = new Node3();
    }
}
