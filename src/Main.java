import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Store {
    private List<Integer> stockList = new ArrayList<>();
    private final int capacity = 12;
    private boolean producersFinished = false;
    private boolean consumersFinished = false;
    private int producedCount = 0;

    public synchronized void produce() {
        Random random = new Random();
        int randomNumber1, randomNumber2;

        while (producedCount < 50) {
            while (stockList.size() + 2 <= capacity && producedCount < 50) {
                randomNumber1 = random.nextInt(100) * 2;
                randomNumber2 = random.nextInt(100) * 2;

                stockList.add(randomNumber1);
                stockList.add(randomNumber2);

                producedCount += 2;
                System.out.println("Producatorul a generat: " + randomNumber1 + ", " + randomNumber2 + ". Total generat: " + producedCount);
                displayStock();
            }

            if (producedCount == 50) {
                randomNumber1 = random.nextInt(100) * 2;
                stockList.add(randomNumber1);
                producedCount++;
                System.out.println("Producatorul a generat: " + randomNumber1 + ". Total generat: " + producedCount);
                displayStock();
            }

            notifyAll();

            if (producedCount == 51) {
                break;
            }

            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        producersFinished = true;
        notifyAll();
    }

    public synchronized void consume() {
        while (!producersFinished || stockList.size() > 0) {
            while (stockList.size() >= 2 || (producedCount == 51 && stockList.size() > 0)) {
                int consumedNumber1 = stockList.remove(0);
                int consumedNumber2 = stockList.size() > 0 ? stockList.remove(0) : -1;

                System.out.println("Consumatorul a consumat: " + consumedNumber1 + (consumedNumber2 != -1 ? ", " + consumedNumber2 : ""));
                displayStock();
            }

            notifyAll();

            if (!producersFinished && stockList.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        consumersFinished = true;
        notifyAll();
    }

    public synchronized List<Integer> getStockList() {
        return new ArrayList<>(stockList);
    }

    public synchronized boolean areProducersFinished() {
        return producersFinished;
    }

    public synchronized boolean areConsumersFinished() {
        return consumersFinished;
    }

    public synchronized int getProducedCount() {
        return producedCount;
    }

    private void displayStock() {
        int currentSize = stockList.size();
        System.out.printf("Stocul: %d din %d%n", currentSize, capacity);
    }

    public synchronized void setProducersFinished() {
        producersFinished = true;
        notifyAll();
    }

    public synchronized void setConsumersFinished() {
        consumersFinished = true;
        notifyAll();
    }
}

class Producer extends Thread {
    private final Store store;

    public Producer(Store store) {
        this.store = store;
    }

    @Override
    public void run() {
        store.produce();
    }
}

class Consumer extends Thread {
    private final Store store;

    public Consumer(Store store) {
        this.store = store;
    }

    @Override
    public void run() {
        store.consume();
    }
}

public class Main {
    public static void main(String[] args) {
        Store store = new Store();

        Producer producer1 = new Producer(store);
        Producer producer2 = new Producer(store);

        Consumer consumer1 = new Consumer(store);
        Consumer consumer2 = new Consumer(store);
        Consumer consumer3 = new Consumer(store);
        Consumer consumer4 = new Consumer(store);
        Consumer consumer5 = new Consumer(store);

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        consumer3.start();
        consumer4.start();
        consumer5.start();

        try {
            producer1.join();
            producer2.join();
            consumer1.join();
            consumer2.join();
            consumer3.join();
            consumer4.join();
            consumer5.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        if (store.getProducedCount() == 50) {
            System.out.println("Producatorul a generat: " + (new Random().nextInt(100) * 2) + ". Total generat: " + (store.getProducedCount() + 1));
            System.out.println("Stocul final:");
            System.out.println(store.getStockList());
        }

        System.out.println("Toți producătorii și consumatorii au terminat. Programul se încheie.");
    }
}
