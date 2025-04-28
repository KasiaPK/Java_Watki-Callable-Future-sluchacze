import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ZadanieZWatkami {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Map<Integer, FutureTask<List<Integer>>> tasks = new ConcurrentHashMap<>();
    private static final AtomicInteger idGenerator = new AtomicInteger(1);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Dodaj zadanie (liczby pierwsze)");
            System.out.println("2. Pokaż status zadań");
            System.out.println("3. Pokaż wynik zadania");
            System.out.println("4. Anuluj zadanie");
            System.out.println("5. Wyjdź");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> addTask();
                case "2" -> showStatus();
                case "3" -> showResult();
                case "4" -> cancelTask();
                case "5" -> {
                    executor.shutdownNow();
                    return;
                }
                default -> System.out.println("Nieprawidłowa opcja.");
            }
        }
    }

    private static void addTask() {
        System.out.print("Podaj początek zakresu: ");
        int start = Integer.parseInt(scanner.nextLine());
        System.out.print("Podaj koniec zakresu: ");
        int end = Integer.parseInt(scanner.nextLine());

        int id = idGenerator.getAndIncrement();

        Callable<List<Integer>> callable = () -> {
            List<Integer> primes = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Zadanie #" + id + " zostało przerwane.");
                    return primes;
                }
                if (isPrime(i)) {
                    primes.add(i);
                }
                Thread.sleep(50); // spowolnienie dla testów anulowania
            }
            return primes;
        };

        FutureTask<List<Integer>> futureTask = new FutureTask<>(callable) {
            @Override
            protected void done() {
                System.out.println("Zadanie #" + id + " zakończone.");
            }
        };

        tasks.put(id, futureTask);
        executor.submit(futureTask);

        System.out.println("Dodano zadanie #" + id);
    }

    private static void showStatus() {
        tasks.forEach((id, future) -> {
            String status;
            if (future.isCancelled()) {
                status = "Anulowane";
            } else if (future.isDone()) {
                status = "Zakończone";
            } else {
                status = "W trakcie";
            }
            System.out.println("Zadanie #" + id + ": " + status);
        });
    }

    private static void showResult() {
        System.out.print("Podaj ID zadania: ");
        int id = Integer.parseInt(scanner.nextLine());
        FutureTask<List<Integer>> task = tasks.get(id);
        if (task == null) {
            System.out.println("Nie ma takiego zadania.");
            return;
        }
        if (!task.isDone()) {
            System.out.println("Zadanie jeszcze się nie zakończyło.");
            return;
        }
        try {
            List<Integer> result = task.get();
            System.out.println("Wynik zadania #" + id + ": " + result);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Błąd podczas pobierania wyniku: " + e.getMessage());
        }
    }

    private static void cancelTask() {
        System.out.print("Podaj ID zadania do anulowania: ");
        int id = Integer.parseInt(scanner.nextLine());
        FutureTask<List<Integer>> task = tasks.get(id);
        if (task == null) {
            System.out.println("Nie ma takiego zadania.");
            return;
        }
        if (task.cancel(true)) {
            System.out.println("Zadanie #" + id + " zostało anulowane.");
        } else {
            System.out.println("Nie udało się anulować zadania #" + id);
        }
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
