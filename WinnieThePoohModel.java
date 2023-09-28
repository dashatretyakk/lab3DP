class HoneyPot {
    // Максимальний обсяг горщика для меду.
    private final int capacity;
    // Кількість меду у горщику.
    private int amount = 0;
    // Об'єкт для синхронізації.
    private final Object lock = new Object();

    public HoneyPot(int capacity) {
        this.capacity = capacity;
    }

    // Метод перевіряє, чи заповнений горщик медом.
    public boolean isFull() {
        return amount == capacity;
    }

    public void addHoney(int beeNumber) {
        synchronized (lock) {
            // Якщо горщик не повний, бджола додає мед.
            if (!isFull()) {
                amount++;
                System.out.println("Бджола " + beeNumber + " додала мед. Загалом: " + amount);
                // Якщо після додавання меду горщик стає повним, сповіщаємо про це (для Вінні-Пуха).
                if (isFull()) {
                    lock.notify();
                }
            }
        }
    }

    public void consumeHoney() {
        synchronized (lock) {
            // Якщо горщик не повний, Вінні-Пух чекає.
            while (!isFull()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Коли горщик заповнений, Вінні-Пух з'їдає весь мед.
            amount = 0;
            System.out.println("Вінні-Пух з'їв увесь мед!");
        }
    }
}

class Bee implements Runnable {
    private final HoneyPot pot;
    private final int number;

    public Bee(HoneyPot pot, int number) {
        this.pot = pot;
        this.number = number;
    }

    @Override
    public void run() {
        while (true) {
            // Бджола регулярно додає мед.
            pot.addHoney(number);
            try {
                // Імітація часу, необхідного для збору меду.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class Bear implements Runnable {
    private final HoneyPot pot;

    public Bear(HoneyPot pot) {
        this.pot = pot;
    }

    @Override
    public void run() {
        while (true) {
            // Вінні-Пух регулярно споживає мед.
            pot.consumeHoney();
            try {
                // Імітація часу на споживання меду.
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

public class WinnieThePoohModel {
    public static void main(String[] args) {
        // Створення горщика для меду з максимальним обсягом 10.
        HoneyPot pot = new HoneyPot(10);

        // Запуск потоку для Вінні-Пуха.
        Bear bear = new Bear(pot);
        Thread bearThread = new Thread(bear);
        bearThread.start();

        // Створення і запуск 5 потоків для бджіл.
        int n = 5;
        for (int i = 0; i < n; i++) {
            Bee bee = new Bee(pot, i + 1);
            Thread beeThread = new Thread(bee);
            beeThread.start();
        }
    }
}
