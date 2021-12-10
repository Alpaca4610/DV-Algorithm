package com.company;

import java.io.*;
import java.net.*;

class myData implements Serializable {
    public int[] minCost;
    public int[] export;
    public int Source;

    public myData() {
        Source = 0;
        minCost = new int[4];
        export = new int[4];
    }
}

public class Node0 implements Serializable {

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
            serverSocket = new DatagramSocket(Node0Port);
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
        DataPack.minCost[0] = 0;
        DataPack.minCost[1] = 3;
        DataPack.minCost[2] = 2;
        DataPack.minCost[3] = MAX;

        //到其他节点最小代价路径的出口
        DataPack.export[0] = 0;
        DataPack.export[1] = 1;
        DataPack.export[2] = 2;
        DataPack.export[3] = -1;

        System.out.println("Node0节点启动，路由表如下：");
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
        sendPacket = new DatagramPacket(sendData, sendData.length, DestAddr, Node1Port);
        serverSocket.send(sendPacket);
        Thread.sleep(1000);//毫秒
        sendPacket = new DatagramPacket(sendData, sendData.length, DestAddr, Node2Port);
        serverSocket.send(sendPacket);
        Thread.sleep(1000);//毫秒
//        sendPacket = new DatagramPacket(sendData, sendData.length, DestAddr, Node3Port);
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

                        int[] cost = new int[4];
                        int[] export = new int[4];
                        int tempSource = dataPack.Source;

                        for (int i = 0; i < 4; i++) {
                            cost[i] = dataPack.minCost[i];
                            export[i] = dataPack.export[i];
                        }

                        System.out.println("收到来自节点" + tempSource + "的数据包");
                        if (tempSource == 1) {
                            if (DataPack.minCost[1] + cost[2] < DataPack.minCost[2]) {//通过1到达2
                                DataPack.minCost[2] = DataPack.minCost[1] + cost[2];
                                DataPack.export[2] = 1;
                                System.out.println("更新Node0到Node2的代价和出口");
                                isChanged = true;
                            }
                            if (DataPack.minCost[1] + cost[3] < DataPack.minCost[3]) {//通过1到达3
                                DataPack.minCost[3] = DataPack.minCost[1] + cost[3];
                                DataPack.export[3] = 1;
                                System.out.println("更新Node0到Node3的代价和出口");
                                isChanged = true;
                            }
                        }
                        if (tempSource == 2) {
                            if (DataPack.minCost[2] + cost[1] < DataPack.minCost[1]) {//通过2到达1
                                DataPack.minCost[1] = DataPack.minCost[2] + cost[1];
                                DataPack.export[1] = 2;
                                System.out.println("更新Node0到Node1的代价和出口");
                                isChanged = true;
                            }
                            if (DataPack.minCost[2] + cost[3] < DataPack.minCost[3]) {//通过2到达3
                                DataPack.minCost[3] = DataPack.minCost[2] + cost[3];
                                DataPack.export[3] = 2;
                                System.out.println("更新Node0到Node3的代价和出口");
                                isChanged = true;
                            }
                        }
                        if (tempSource == 3) {
                            if (DataPack.minCost[3] + cost[1] < DataPack.minCost[1]) {//通过3到达1
                                DataPack.minCost[1] = DataPack.minCost[3] + cost[1];
                                DataPack.export[1] = 3;
                                System.out.println("更新Node0到Node1的代价和出口");
                                isChanged = true;
                            }
                            if (DataPack.minCost[3] + cost[2] < DataPack.minCost[2]) {//通过3到达2
                                DataPack.minCost[2] = DataPack.minCost[3] + cost[2];
                                DataPack.export[2] = 3;
                                System.out.println("更新Node0到Node2的代价和出口");
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

    public Node0() throws Exception {
        myThread t1 = new myThread(0);
        t1.start();
        myThread t2 = new myThread(1);
        t2.start();
    }

    public static void main(String args[]) throws Exception {
        Node0 n0 = new Node0();
    }

}
