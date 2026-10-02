package ca.mcgill.ecse420.a1;

public class DeadlockExample {

    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) {

        Thread thread1 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("Thread 1 acquired LOCK_A");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println("Thread 1 waiting for LOCK_B");

                synchronized (LOCK_B) {
                    System.out.println("Thread 1 acquired LOCK_B");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (LOCK_B) {
                System.out.println("Thread 2 acquired LOCK_B");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println("Thread 2 waiting for LOCK_A");

                synchronized (LOCK_A) {
                    System.out.println("Thread 2 acquired LOCK_A");
                }
            }
        });
    
        thread1.start();
        thread2.start();
    }
}