package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//question 3.1
public class DiningPhilosophersDeadlock {

    public static void main(String[] args) {

        int numberOfPhilosophers = 5;

        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        Object[] chopsticks = new Object[numberOfPhilosophers];

        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new Object();
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);

        for (int i = 0; i < numberOfPhilosophers; i++) {

            Object leftChopstick = chopsticks[i];
            Object rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];

            philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick);

            executor.execute(philosophers[i]);
        }

        executor.shutdown();
    }

    public static class Philosopher implements Runnable {

        private final int id;
        private final Object leftChopstick;
        private final Object rightChopstick;

        public Philosopher(int id, Object leftChopstick, Object rightChopstick) {
            this.id = id;
            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
        }

        @Override
        public void run() {

            while (true) {

                think();

                synchronized (leftChopstick) {

                    System.out.println("Philosopher " + id + " picked up left chopstick.");

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    synchronized (rightChopstick) {

                        System.out.println("Philosopher " + id + " picked up right chopstick.");

                        eat();
                    }
                }
            }
        }

        private void think() {
            System.out.println("Philosopher " + id + " is thinking.");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void eat() {
            System.out.println("Philosopher " + id + " is eating.");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}