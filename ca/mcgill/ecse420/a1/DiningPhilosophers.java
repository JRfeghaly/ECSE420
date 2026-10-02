package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
//question 3.2
public class DiningPhilosophers {

    public static void main(String[] args) {

        int numberOfPhilosophers = 5;

        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new ReentrantLock(true);
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);

        for (int i = 0; i < numberOfPhilosophers; i++) {
            int leftChopstick = i;
            int rightChopstick = (i + 1) % numberOfPhilosophers;
            philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick, chopsticks);
            executor.execute(philosophers[i]);
        }

        executor.shutdown();
    }

    public static class Philosopher implements Runnable {

        private final int id;
        private final int leftChopstick;
        private final int rightChopstick;
        private final ReentrantLock[] chopsticks;

        private int mealsEaten = 0;

        public Philosopher(int id, int leftChopstick, int rightChopstick, ReentrantLock[] chopsticks) {
            this.id = id;
            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
            this.chopsticks = chopsticks;
        }

        @Override
        public void run() {

            while (true) {

                think();

                int firstChopstick = Math.min(leftChopstick, rightChopstick);
                int secondChopstick = Math.max(leftChopstick, rightChopstick);

                chopsticks[firstChopstick].lock();

                try {
                    System.out.println("Philosopher " + id + " picked up chopstick " + firstChopstick);
                    chopsticks[secondChopstick].lock();

                    try {
                        System.out.println( "Philosopher " + id + " picked up chopstick " + secondChopstick);
                        eat();
                    } finally {
                        chopsticks[secondChopstick].unlock();
                    }

                } finally {
                    chopsticks[firstChopstick].unlock();
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

            mealsEaten++;

            System.out.println("Philosopher " + id + " is eating. Meals eaten: " + mealsEaten);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}