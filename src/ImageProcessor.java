import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageProcessor
{

    private BufferedImage image;

    public BufferedImage getImage() {return image;}



    public void readImage(String path) throws IOException
    {
        File file=new File(path);

        if(!file.exists())
        {
            throw new IOException("Nie odnaleziono pliku w " + path);
        }

        this.image= ImageIO.read(file);

        if(this.image==null)
        {
            throw new IOException("Nie można odczytać pliku z " + path);
        }

        System.out.println("Załadowano obraz z " + path);
    }


    public void saveImage(String outputPath) throws IOException
    {
        if(this.image==null)
        {
            throw new IllegalStateException("Brak obrazu");
        }

        String format=getFileExtension(outputPath);

        if(format==null || format.isEmpty())
        {
            throw new IllegalArgumentException("Plik musi mieć rozszerzenie typu .jpg");
        }

        File outputFile=new File(outputPath);

        ImageIO.write(this.image,format,outputFile);

        System.out.println("Plik zapisano w " + outputPath);

    }
    // Metoda prywatna, zwracająca rozszerzenie pliku jako String.
    // Przyjmuje jako parametr nazwę pliku (np. "dokument.txt").
    private String getFileExtension(String filename)
    {
        // Szukamy ostatniego wystąpienia kropki w nazwie pliku.
        // lastIndexOf(".") zwraca indeks ostatniej kropki (np. dla "plik.txt" zwróci 4).
        int dotIndex = filename.lastIndexOf(".");

        // Sprawdzamy dwa warunki:
        // 1. Czy kropka została znaleziona (dotIndex > 0), czyli nie znajduje się na początku (np. ".gitignore").
        // 2. Czy kropka nie jest ostatnim znakiem (dotIndex < filename.length() - 1),
        //    czyli za kropką znajduje się jeszcze jakieś rozszerzenie.
        if (dotIndex > 0 && dotIndex < filename.length() - 1)
        {
            // Zwracamy część tekstu po kropce, czyli rozszerzenie pliku.
            // substring(dotIndex + 1) zwraca tekst od znaku po kropce do końca (np. "txt").
            return filename.substring(dotIndex + 1);
        }

        // Jeśli warunki nie są spełnione (np. brak kropki lub kropka na końcu),
        // to nie ma rozszerzenia — zwracamy null.
        return null;
    }


    // Metoda publiczna, która zwiększa lub zmniejsza jasność obrazu o podaną wartość (brightnessValue).
    public void adjustBrightness(int brightnessValue)
    {
        // Sprawdzenie, czy obraz istnieje. Jeśli nie, rzucany jest wyjątek.
        if (this.image == null)
        {
            throw new IllegalStateException("Brak obrazu");
        }

        // Pobieramy szerokość obrazu w pikselach.
        int width = this.image.getWidth();

        // Pobieramy wysokość obrazu w pikselach.
        int height = this.image.getHeight();

        // Tworzymy tablicę do przechowywania składowych jednego piksela (np. R, G, B, A).
        // Liczba składowych jest pobierana dynamicznie z obrazu.
        int[] pixelComponets = new int[this.image.getRaster().getNumBands()];

        // Iterujemy po wszystkich wierszach obrazu (y to współrzędna pionowa).
        for (int y = 0; y < height; y++)
        {
            // Iterujemy po wszystkich kolumnach obrazu (x to współrzędna pozioma).
            for (int x = 0; x < width; x++)
            {
                // Pobieramy aktualny piksel na pozycji (x, y) i zapisujemy jego składowe do tablicy pixelComponets.
                // Np. dla RGB: pixelComponets[0] = R, [1] = G, [2] = B.
                this.image.getRaster().getPixel(x, y, pixelComponets);

                // Iterujemy po każdej składowej koloru danego piksela.
                for (int i = 0; i < pixelComponets.length; i++)
                {
                    // Dodajemy wartość brightnessValue do danej składowej.
                    // Math.min i Math.max gwarantują, że wynik będzie w zakresie 0-255 (zakres dopuszczalny dla koloru).
                    pixelComponets[i] = Math.max(0, Math.min(255, pixelComponets[i] + brightnessValue));
                }

                // Ustawiamy nową wartość piksela (ze zmodyfikowanymi składowymi) z powrotem w obrazie.
                this.image.getRaster().setPixel(x, y, pixelComponets);
            }
        }

        // Informacja na konsoli o zakończeniu operacji.
        System.out.println("Zwiększono jasność o : " + brightnessValue);
    }

    public void adjustBrightnessMultiThreaded(int brightnessIncrease) throws InterruptedException
    {
        if (this.image == null)
        {
            throw new IllegalStateException("Brak Obrazu"); // Checks if an image is loaded
        }

        // 1. Sprawdzamy ile mamy rdzeni
        int numCores = Runtime.getRuntime().availableProcessors();
        int width = this.image.getWidth();
        int height = this.image.getHeight();

        int numThreads = numCores;
        if (numThreads == 0) numThreads = 1; // Program wymaga przynajmniej jednego watku

        // 2. Zarządanie watkami
        Thread[] threads = new Thread[numThreads]; // Tablica przechowuje reference to 'workerów' wykonujących zadania na rdzeniach
        int rowsPerThread = height / numThreads;    // Ile rzędów będze przetwarzał jeden wątek
        int remainingRows = height % numThreads;    // Reszta rzędów dodawan do ostatniego watku

        // 3. Dzielenie zadań i tworzenie wątków
        for (int i = 0; i < numThreads; i++)
        {
            int startY = i * rowsPerThread; // obliczamy poczatkowy nr. rzędu dla watku
            int endY = startY + rowsPerThread - 1; // końcowy

            if (i == numThreads - 1)
            {
                // ostatni dostaje reszte
                endY += remainingRows;
            }
            endY = Math.min(endY, height - 1); // endY nie może być większy niż wysokośc obrazu

            // tworzymy nowy wątek i przekazujemy mu workera który wykonuje zadanie
            threads[i] = new Thread(new BrightnessWorker(this.image, brightnessIncrease, startY, endY, width));

            // Włączamy wątek
            threads[i].start();
        }

        // 4. Czekamy aż wszystkie watki wykonają swoje zadania
        for (int i = 0; i < numThreads; i++)
        {
            // thread join każe obecnemu wątkowi, czyli main czekać na zakończenie wykonania wątku threads[i]
            threads[i].join();
        }
    }

    public void adjustBrightnessThreadPool(int brightnessIncrease) throws InterruptedException
    {
        if (this.image == null)
        {
            throw new IllegalStateException("Brak Obrazu");
        }

        int width = this.image.getWidth();
        int height = this.image.getHeight();
        int numCores = Runtime.getRuntime().availableProcessors();
        // Sugerowany rozmiar puli: liczba rdzeni, lub 2x liczba rdzeni dla zadań z potencjalnymi blokadami I/O.
        // Dla CPU-bound zadań zwykle numCores jest dobre.
        int poolSize = numCores;
        if (poolSize == 0) poolSize = 1; // Upewnij się, że pula ma co najmniej 1 wątek

        // Tworzymy pulę wątków o stałym rozmiarze
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        System.out.println("Starting thread pool brightness adjustment with " + poolSize + " threads in pool.");


        // Dla każdego wiersza tworzymy zadanie i wysyłamy je do puli
        for (int y = 0; y < height; y++)
        {
            // Nowy RowBrightnessWorker, który zajmuje się tylko jednym wierszem
            executor.submit(new RowBrightnessWorker(this.image, brightnessIncrease, y, width));
        }

        // Zakończ działanie puli:
        // 1. Zakończ przyjmowanie nowych zadań.
        executor.shutdown();
        // 2. Czekaj na zakończenie wszystkich zadań w puli z timeoutem.
        // To jest kluczowe, aby mieć pewność, że wszystkie operacje na obrazie się zakończyły.
        // Dajemy sensowny timeout (np. 1 godzinę), aby program nie wieszał się w nieskończoność.
        if (!executor.awaitTermination(1, TimeUnit.HOURS))
        { // Max wait 1 hour
            System.err.println("Thread pool did not terminate in time!");
            // Możesz dodać executor.shutdownNow(); aby wymusić zakończenie, jeśli timeout się skończył
        }
    }

    // Worker, to on będzie rozjaśniał kawałek obrazu, na pojedynczym wątku
    private static class BrightnessWorker implements Runnable
    {
        private final BufferedImage image;
        private final int brightnessIncrease;
        private final int startY;
        private final int endY;
        private final int width;
        private final int[] pixelComponents;

        public BrightnessWorker(BufferedImage image, int brightnessIncrease, int startY, int endY, int width)
        {
            this.image = image;
            this.brightnessIncrease = brightnessIncrease;
            this.startY = startY;
            this.endY = endY;
            this.width = width;
            this.pixelComponents = new int[image.getRaster().getNumBands()];
        }

        @Override
        public void run()
        {
            for (int y = this.startY; y <= this.endY; y++)
            {
                for (int x = 0; x < this.width; x++)
                {
                    this.image.getRaster().getPixel(x, y, this.pixelComponents);

                    for (int i = 0; i < this.pixelComponents.length; i++)
                    {
                        this.pixelComponents[i] = Math.max(0, Math.min(255, this.pixelComponents[i] + this.brightnessIncrease));
                    }
                    this.image.getRaster().setPixel(x, y, this.pixelComponents);
                }
            }
        }
    }

    //Zad. 3

    private static class RowBrightnessWorker implements Runnable
    {
        private final BufferedImage image;
        private final int brightnessIncrease;
        private final int rowY; // Only one row to process
        private final int width;
        private final int[] pixelComponents;

        public RowBrightnessWorker(BufferedImage image, int brightnessIncrease, int rowY, int width)
        {
            this.image = image;
            this.brightnessIncrease = brightnessIncrease;
            this.rowY = rowY;
            this.width = width;
            this.pixelComponents = new int[image.getRaster().getNumBands()];
        }

        @Override
        public void run()
        {
            // Process only the assigned row (rowY)
            for (int x = 0; x < this.width; x++)
            {
                this.image.getRaster().getPixel(x, this.rowY, this.pixelComponents);

                for (int i = 0; i < this.pixelComponents.length; i++)
                {
                    this.pixelComponents[i] = Math.max(0, Math.min(255, this.pixelComponents[i] + this.brightnessIncrease));
                }
                this.image.getRaster().setPixel(x, this.rowY, this.pixelComponents);
            }
        }
    }


}
