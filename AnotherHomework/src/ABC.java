public class ABC {

    private final Object obj = new Object();
    private volatile char currentLetter = 'A';

    public static void main(String[] args) {

        ABC abc = new ABC();


        Thread thread1 = new Thread(() -> abc.printLetterA());

        Thread thread2 = new Thread(() -> abc.printLetterB());

        Thread thread3 = new Thread(() -> abc.printLetterC());

        thread1.start();
        thread2.start();
        thread3.start();
    }


    public void printLetterA() {
        synchronized (obj) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != 'A') {
                        obj.wait();
                    }
                    System.out.print("A");
                    currentLetter = 'B';
                    obj.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void printLetterB() {
        synchronized (obj) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != 'B') {
                        obj.wait();
                    }
                    System.out.print("B");
                    currentLetter = 'C';
                    obj.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
    }

    public void printLetterC() {
        synchronized (obj) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != 'C') {
                        obj.wait();
                    }
                    System.out.print("C" + " - " + (i + 1) + " вывод");
                    System.out.print("\n");
                    currentLetter = 'A';
                    obj.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}