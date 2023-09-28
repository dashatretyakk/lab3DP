import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BarberShop {

    private final Semaphore semaphore = new Semaphore(1, true);
    private final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);

    public void haircut() {
        try {
            while (true) {
                Integer customerId = queue.poll();
                if (customerId == null) {
                    System.out.println("Перукар чекає відвідувача.");
                    // Якщо в черзі немає клієнта, перукар "спить", використовуючи семафор.
                    semaphore.acquire();
                } else {
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
            if (!queue.offer(customerId, 2, TimeUnit.SECONDS)) {
                System.out.println("Відвідувач " + customerId + " покинув перукарню через брак місця.");
                return;
            }
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
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(5000, 15000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
