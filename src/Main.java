import java.io.IOException;

public class Main
{
    public static void main(String[] args)
    {
        // Tworzymy obiekt klasy ImageProcessor, który będzie przetwarzał obrazy.
        ImageProcessor processor = new ImageProcessor();

        // Ścieżki do plików wejściowych i wyjściowych.
        String inputImagePath = "resources/Pies.jpg";
        String outputImagePath = "resources/Pies_dwa.jpg";
        String outputImagePathBrighter = "resources/Pies_brighter.jpg";
        String outputImagePathDarker = "resources/Pies_darker.jpg";

        try
        {
            // Wczytujemy obraz z pliku.
            processor.readImage(inputImagePath);

            // Zapisujemy oryginalny obraz do nowego pliku.
            processor.saveImage(outputImagePath);

            // Zwiększamy jasność obrazu o 70 jednostek i zapisujemy wynik.
            processor.adjustBrightness(70);
            processor.saveImage(outputImagePathBrighter);

            // Zmniejszamy jasność obrazu o 70 jednostek (wracamy do oryginału lub ciemniejszy) i zapisujemy wynik.
            processor.adjustBrightness(-70);
            processor.saveImage(outputImagePathDarker);

        }
        // Obsługa błędów związanych z plikami wejścia/wyjścia.
        catch (IOException e)
        {
            System.err.println("An I/O error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        // Obsługa błędów związanych ze stanem (np. brak obrazu).
        catch (IllegalStateException e)
        {
            System.err.println("Processing error: " + e.getMessage());
        }
        // Obsługa błędów związanych z nieprawidłowymi argumentami.
        catch (IllegalArgumentException e)
        {
            System.err.println("Input error: " + e.getMessage());
        }

        // Pomiar czasu działania metod jedno- i wielowątkowych.
        long startTime;
        long endTime;
        long singleThreadTime = 0;
        long multiThreadTime = 0;

        // --- Jednowątkowe zwiększenie jasności obrazu ---
        try
        {
            System.out.println("\n--- Running Single-threaded Brightness Adjustment (Raster) ---");

            // Wczytanie obrazu ponownie.
            processor.readImage(inputImagePath);

            // Rozpoczęcie pomiaru czasu.
            startTime = System.currentTimeMillis();

            // Przetwarzanie obrazu w jednym wątku (jasność +70).
            processor.adjustBrightness(70);

            // Zakończenie pomiaru czasu.
            endTime = System.currentTimeMillis();

            singleThreadTime = endTime - startTime;
            System.out.println("Single-threaded brightness adjustment finished in " + singleThreadTime + " ms.");

            // Zapis przetworzonego obrazu.
            processor.saveImage("img_single_thread.jpg");
        }
        catch (IOException e)
        {
            System.err.println("I/O error during single-threaded process: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IllegalStateException e)
        {
            System.err.println("Error in single-threaded process: " + e.getMessage());
        }

        // --- Wielowątkowe zwiększenie jasności obrazu ---
        try
        {
            System.out.println("\n--- Running Multi-threaded Brightness Adjustment (Raster) ---");

            // Wczytanie obrazu ponownie, przed wielowątkowym przetwarzaniem.
            processor.readImage(inputImagePath);

            // Rozpoczęcie pomiaru czasu.
            startTime = System.currentTimeMillis();

            // Ustalenie liczby dostępnych wątków (rdzeni procesora).
            int numCores = Runtime.getRuntime().availableProcessors();
            int numThreads = numCores == 0 ? 1 : numCores;
            System.out.println("Using " + numThreads + " threads.");

            // Przetwarzanie obrazu z użyciem wielu wątków (jasność +70).
            processor.adjustBrightnessMultiThreaded(70);

            // Zakończenie pomiaru czasu.
            endTime = System.currentTimeMillis();

            multiThreadTime = endTime - startTime;
            System.out.println("Multi-threaded brightness adjustment finished in " + multiThreadTime + " ms.");

            // Zapis przetworzonego obrazu.
            processor.saveImage("img_multi_thread.jpg");
        }
        catch (IOException e)
        {
            System.err.println("I/O error during multi-threaded process: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IllegalStateException e)
        {
            System.err.println("Error in multi-threaded process: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            System.err.println("Multi-threaded process interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // poprawna reakcja na przerwanie wątku
        }
    }
}