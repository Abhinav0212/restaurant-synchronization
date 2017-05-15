import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

class SinglesOnly{
    RestaurantTables res;
    BurgerMachine burger;
    FriesMachine fry;
    SodaMachine soda;
    LinkedBlockingQueue<Order> sharedQueue;
    LinkedBlockingQueue<Integer> tableQueue;
    int dinersLeft;
    int cooksLeft;


    public static void main(String[] args){
        SinglesOnly obj = new SinglesOnly();
        obj.openRestaurant();
    }
    void init(int totTables, int totDiners, int totCooks){
        res = new RestaurantTables(totTables);
        burger = new BurgerMachine();
        fry = new FriesMachine();
        soda = new SodaMachine();
        sharedQueue = new LinkedBlockingQueue<Order>();
        tableQueue = new LinkedBlockingQueue<Integer>();
        dinersLeft = totDiners;
        cooksLeft = totCooks;
    }
    void openRestaurant(){
        Scanner in = new Scanner(System.in);
        int totDiners = in.nextInt();
        int totTables = in.nextInt();
        int totCooks = in.nextInt();
        init(totTables, totDiners, totCooks);

        // for (int i = 0; i < totTables; i++){
        //     try{
        //         tableQueue.put(i);
        //     }
        //     catch(InterruptedException e){
        //
        //     }
        // }
        Cook[] cook = new Cook[totCooks];
        for (int i = 0; i < totCooks; i++){
            cook[i] = new Cook(i);
            cook[i].start();
        }
        Diner[] diner = new Diner[totDiners];
        for (int i = 0; i < totDiners; i++){
            String[] input = in.next().split(",");
            diner[i]= new Diner(i+1, Integer.parseInt(input[0]), Integer.parseInt(input[1]), Integer.parseInt(input[2]), Integer.parseInt(input[3]));
            diner[i].start();
        }
    }

    class Order{
        private int inTime = 0;
        private int burgers = 0;
        private int fries = 0;
        private int sodas = 0;
        private int id = 0;

        Order(int id, int inTime, int burgers, int fries, int sodas){
            this.id = id;
            this.inTime = inTime;
            this.burgers = burgers;
            this.fries = fries;
            this.sodas = sodas;
        }
        int getBurger(){
            return burgers;
        }
        int getFries(){
            return fries;
        }
        int getId(){
            return id;
        }
        int getSodas(){
            return sodas;
        }
        int getTime(){
            return inTime;
        }
        void setTime(int n){
            inTime = n;
        }
    }

    class Cook extends Thread{
        private int id = 0;
        private int inTime = 0;
        private int burgers = 0;
        private int fries = 0;
        private int sodas = 0;
        Cook(int id){
            this.id = id+1;
        }
        public void run(){
            while(dinersLeft>cooksLeft){
                try {
                    Order ord = sharedQueue.take();
                    // System.out.println("Cook " + id + " has received the order.");
                    synchronized(ord){
                        inTime = ord.getTime();
                        res.addStatement(inTime,"Cook " + id + " has received the order for Diner " + ord.getId());
                        burgers = ord.getBurger();
                        fries = ord.getFries();
                        sodas = ord.getSodas();
                        if(burgers>0)
                            inTime = burger.useMachine(id, inTime, burgers);
                        if(fries>0)
                            inTime = fry.useMachine(id,inTime, fries);
                        if(sodas>0)
                            inTime = soda.useMachine(id,inTime, sodas);
                        ord.setTime(inTime);
                        ord.notify();
                        //System.out.println(" Cook "+ id + " checking");
                        // res.addStatement(inTime,"Cook "+ id + " checking");
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error " + e + " for Cook "+ id + " taking order.");
                }
            }
            // res.addStatement(inTime,"Cook "+ id + " leaving");
            cooksLeft--;
        }
    }

    class Diner extends Thread{
        private int inTime = 0;
        private int burgers = 0;
        private int fries = 0;
        private int sodas = 0;
        private int id = 0;

        Diner(int id, int inTime, int burgers, int fries, int sodas){
            this.id = id;
            this.inTime = inTime;
            this.burgers = burgers;
            this.fries = fries;
            this.sodas = sodas;
        }
        public void run(){
            inTime = res.enterRestaurant(id, inTime);
            Order ord = new Order(id, inTime, burgers, fries, sodas);
            // System.out.println("Diner " + id + " has decided the order.");
            // res.addStatement(inTime,"Diner " + id + " has decided the order.");
            synchronized (ord){
                try{
                    sharedQueue.put(ord);
                    // System.out.println("Diner " + id + " has placed the order.");
                    // res.addStatement(inTime,"Diner " + id + " has placed the order.");
                    ord.wait();
                    inTime = ord.getTime();
                    res.addStatement(inTime,"Diner "+ id + " starts eating");
                }
                catch(InterruptedException e){
                    System.out.println("Error " + e + " for Diner "+ id + " creating order.");
                }
            }
            res.exitRestaurant(inTime, id);
            dinersLeft--;
            if(dinersLeft==0){
                res.printMap(res.hm);
            }
        }
    }

    class BurgerMachine{
        private int time = 0;
        synchronized int useMachine(int id, int inTime, int num){
            if(inTime>time){
                // System.out.println("Cook " + id + " is using the BurgerMachine at " + inTime);
                res.addStatement(inTime,"Cook " + id + " is using the BurgerMachine.");
                time = inTime + 5*num;
            }
            else{
                // System.out.println("Cook " + id + " is using the BurgerMachine at " + time);
                res.addStatement(time,"Cook " + id + " is using the BurgerMachine.");
                time = time + 5*num;
            }
            return time;
        }
    }

    class FriesMachine{
        private int time = 0;
        synchronized int useMachine(int id, int inTime, int num){
            if(inTime>time){
                // System.out.println("Cook " + id + " is using the FriesMachine at " + inTime);
                res.addStatement(inTime,"Cook " + id + " is using the FriesMachine.");
                time = inTime + 3*num;
            }
            else{
                // System.out.println("Cook " + id + " is using the FriesMachine at " + time);
                res.addStatement(time,"Cook " + id + " is using the FriesMachine.");
                time = time + 3*num;
            }
            return time;
        }
    }

    class SodaMachine{
        private int time = 0;
        synchronized int useMachine(int id, int inTime, int num){
            if(inTime>time){
                // System.out.println("Cook " + id + " is using the SodaMachine at " + inTime);
                res.addStatement(inTime,"Cook " + id + " is using the SodaMachine.");
                time = inTime + 1*num;
            }
            else{
                // System.out.println("Cook " + id + " is using the SodaMachine at " + time);
                res.addStatement(time,"Cook " + id + " is using the SodaMachine.");
                time = time + 1*num;
            }
            return time;
        }
    }

    class RestaurantTables{
        private int maxTables = 0;
        private int availTables = 0;
        private int time = 0;
        HashMap<Integer,String> hm;
        RestaurantTables(int maxTables){
            this.maxTables = maxTables;
            this.availTables = maxTables;
            hm = new HashMap<Integer,String>();
        }
        synchronized int enterRestaurant(int id, int inTime){
            // System.out.println("Diner " + id + " arrives.");
            res.addStatement(inTime,"Diner " + id + " arrives.");
            while(availTables == 0){
                try{
                    wait();
                }
                catch(InterruptedException e){
                    System.out.println("Error " + e + " for Diner "+ id + " getting table.");
                }
            }
            availTables--;
            // System.out.println("Diner " + id + " is seated on table " + (availTables+1) + ".");
            if(time > inTime){
                res.addStatement(time,"Diner " + id + " is seated on table " + (availTables+1) + ".");
                return time;
            }
            else{
                res.addStatement(inTime,"Diner " + id + " is seated on table " + (availTables+1) + ".");
                return inTime;
            }
        }
        synchronized void exitRestaurant(int inTime, int id){
            time = inTime+30;
            availTables++;
            notify();
            //System.out.println("Diner " + id + " has left." + dinersLeft);
            res.addStatement(time,"Diner " + id + " has left.");
        }
        void addStatement(int time,String statement) {
            if(hm.containsKey(time)){
                String s = hm.get(time);
                s = s + "\n" + statement;
                hm.put(time,s);
            }
            else
            hm.put(time,statement);
        }

        void printMap(HashMap<Integer,String> hm) {
            Map<Integer, String> treeMap = new TreeMap<Integer, String>(hm);

            Set s = treeMap.entrySet();
            Iterator it = s.iterator();
            while ( it.hasNext() ) {
               Map.Entry entry = (Map.Entry) it.next();
               int key = (Integer) entry.getKey();
               String value = (String) entry.getValue();
               String[] values = value.split("\n");
               for (String v : values){
                   int hour = key/60;
                   int min = key%60;
                   if(min < 10){
                       System.out.println("0" + hour + ":0" + min + " - " + v);
                   }
                   else{
                       System.out.println("0" + hour + ":" + min + " - " + v);
                   }
               }
            }//while
            System.out.println("========================");
        }
    }
}
