import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BarberShop {
    // Семафор використовується для синхронізації між перукарем і клієнтами.
    // Початкова кількість дозволів встановлена як 1.
    private final Semaphore semaphore = new Semaphore(1, true);
    // Черга для клієнтів, що чекають. Максимальна її довжина - 5.
    private final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);

    public void haircut() {
        try {
            while (true) {
                // Перукар намагається отримати клієнта з черги.
                Integer customerId = queue.poll();
                if (customerId == null) {
                    System.out.println("Перукар чекає відвідувача.");
                    // Якщо в черзі немає клієнта, перукар "спить", використовуючи семафор.
                    semaphore.acquire();
                } else {
                    // Якщо клієнт присутній, перукар розпочинає стрижку.
                    System.out.println("Перукар стриже відвідувача " + customerId + ".");
                    Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1000));
                    System.out.println("Відвідувач " + customerId + " закінчив стрижку.");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enterShop(int customerId) {
        try {
            System.out.println("Відвідувач " + customerId + " прийшов.");
            // Клієнт намагається зайняти місце в черзі.
            if (!queue.offer(customerId, 2, TimeUnit.SECONDS)) {
                System.out.println("Відвідувач " + customerId + " покинув перукарню через брак місця.");
                return;
            }
            // Якщо перукар спить (немає доступних дозволів), клієнт розбудить його.
            if (semaphore.availablePermits() == 0) {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BarberShop shop = new BarberShop();
        Thread barberThread = new Thread(new Barber(shop));
        barberThread.start();

        for (int i = 1; i <= 10; i++) {
            Thread customerThread = new Thread(new Customer(shop, i));
            customerThread.start();
            // Моделювання приходу клієнтів у різний час.
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Barber implements Runnable {
    private final BarberShop shop;

    public Barber(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        shop.haircut();
    }
}

class Customer implements Runnable {
    private final BarberShop shop;
    private final int id;

    public Customer(BarberShop shop, int id) {
        this.shop = shop;
        this.id = id;
    }

    @Override
    public void run() {
        while (true) {
            shop.enterShop(id);
            // Моделювання поведінки клієнта після стрижки: він чекає та знову відвідує перукарню після певного проміжку часу.
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(5000, 15000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
