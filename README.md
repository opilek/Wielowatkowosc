# Notatka: Podstawy wielowątkowości w Javie — praktyczne przykłady
## 1. Co to jest wielowątkowość?
Wielowątkowość to uruchamianie wielu ścieżek wykonywania (wątków) w jednym procesie.

Pozwala na równoległe przetwarzanie, co przyspiesza zadania (zwłaszcza na wielordzeniowych procesorach).

W Javie podstawą jest klasa Thread i interfejs Runnable.

## 2. Tworzenie i uruchamianie wątków
### Sposób 1: Dziedziczenie po Thread
```java 

class MyThread extends Thread {
    public void run() {
        System.out.println("Wątek działa");
    }
}

MyThread t = new MyThread();
t.start();  // Uruchamia wątek i wywołuje metodę run() równolegle
```
### Sposób 2: Implementacja Runnable

```java 

class MyRunnable implements Runnable {
    public void run() {
        System.out.println("Wątek działa");
    }
}

Thread t = new Thread(new MyRunnable());
t.start();
```
## 3. Synchronizacja (gdy wątki korzystają z tych samych danych)
Aby zapobiec problemom z współbieżnym dostępem (np. wyścigi), stosujemy słowo kluczowe synchronized:
```java 

public synchronized void increment() {
    counter++;
}
Synchronizacja blokuje dostęp innym wątkom do tej metody/sekcji kodu aż do jej zakończenia.

4. Pula wątków (ExecutorService)
Zarządzanie wieloma wątkami ułatwia pula wątków, która tworzy i ponownie wykorzystuje ograniczoną liczbę wątków.


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

ExecutorService executor = Executors.newFixedThreadPool(4);

executor.submit(() -> {
    System.out.println("Zadanie wykonane przez wątek z puli");
});

executor.shutdown();  // Po zakończeniu pracy zamyka pulę
```
## 5. Podział zadania na wątki — przykład wzorca
Zadanie: przetwarzanie dużej tablicy/liczby elementów
Dzielimy dane na części, każda część jest obsługiwana przez osobny wątek,

Po zakończeniu wątków łączymy wyniki.
```java 


int cores = Runtime.getRuntime().availableProcessors();
int length = data.length;
Thread[] threads = new Thread[cores];

for (int i = 0; i < cores; i++) {
    int start = i * length / cores;
    int end = (i + 1) * length / cores;
    threads[i] = new Thread(() -> {
        for (int j = start; j < end; j++) {
            // Przetwarzaj element data[j]
        }
    });
    threads[i].start();
}

for (Thread t : threads) {
    t.join();  // Czekaj na zakończenie wszystkich wątków
}
```
## 6. Przykład wielowątkowego przetwarzania obrazu (bufor obrazu)

```java

for (int y = startY; y < endY; y++) {
    for (int x = 0; x < width; x++) {
        // Przetwarzanie piksela obrazu
    }
}
Wątek przetwarza konkretny zakres wierszy obrazu.
```

## 7. Mierzenie czasu wykonania
```java
long start = System.currentTimeMillis();

// kod do zmierzenia

long end = System.currentTimeMillis();
System.out.println("Czas wykonania: " + (end - start) + " ms");

```
## 8. Obsługa wyników i komunikacja między wątkami
Unikaj modyfikowania współdzielonych zmiennych bez synchronizacji,

Można użyć tablicy wyników — każdy wątek zapisuje swoje wyniki w innym fragmencie,

Po zakończeniu wątków dane są scalane w głównym wątku.

## 9. Zalety i ograniczenia
Zalety:
Skrócenie czasu wykonywania zadań,

Wykorzystanie wielu rdzeni CPU,

Lepsza responsywność aplikacji.

Ograniczenia:
Koszt tworzenia i przełączania wątków,

Ryzyko błędów związanych z synchronizacją (np. deadlocki),

Trudniejszy debugging.

## 10. Podsumowanie
Wielowątkowość to podstawowy sposób na poprawę wydajności w Javie,

Zawsze dziel zadanie na niezależne fragmenty (np. wiersze obrazu, segmenty tablicy),

Korzystaj z ExecutorService do zarządzania pulą wątków,

Synchronizuj współdzielone zasoby,

Testuj i mierz czas działania.

## Uniwersalny szablon kodu wielowątkowego

```java

public class MultiThreadExample {

    private int[] data;

    public void process() throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[cores];
        int length = data.length;

        for (int i = 0; i < cores; i++) {
            int start = i * length / cores;
            int end = (i + 1) * length / cores;
            threads[i] = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    // Przetwarzaj element data[j]
                }
            });
            threads[i].start();
        }
```

        for (Thread t : threads) {
            t.join();
        }
    }
}
