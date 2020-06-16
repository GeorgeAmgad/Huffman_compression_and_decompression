import java.io.File;
import java.util.Objects;
import java.util.Scanner;
import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static java.lang.System.exit;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        HuffmanCompression huffman = new HuffmanCompression();

        do {
            System.out.println("\t\tHuffman compression and decompression");
            System.out.println("\tchoose operation:");
            System.out.println("\t 1. Compress.");
            System.out.println("\t 2. decompress.");
            System.out.println("\t 3. exit.");
            System.out.print(" >> ");
            Scanner sc = new Scanner(System.in);
            char c = sc.next().charAt(0);
            switch (c) {
                case '1':
                    System.out.println(" 1. Compress file");
                    System.out.println(" 2. Compress folder");
                    System.out.print(" >> ");

                    c = sc.next().charAt(0);
                    switch (c) {
                        case '1':
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Select file to compress");
                            fileChooser.setInitialDirectory(new File("C:\\Users\\georg\\Desktop\\Huffman compression & decompression"));
                            File selectedFile = fileChooser.showOpenDialog(stage);
                            huffman.compress(selectedFile, "output.bin", HuffmanCompression.COMPRESS_FILE);
                            break;
                        case '2':
                            DirectoryChooser dirChooser = new DirectoryChooser();
                            dirChooser.setTitle("Select folder to compress");
                            dirChooser.setInitialDirectory(new File("C:\\Users\\georg\\Desktop\\Huffman compression & decompression"));
                            File selectedFolder = dirChooser.showDialog(stage);
                            System.out.println(Objects.requireNonNull(selectedFolder.listFiles())[0].toString());
                            huffman.compress(selectedFolder,"output.bin", HuffmanCompression.COMPRESS_FOLDER);
                            break;
                        default:
                            System.out.println("\nunrecognized input!");
                    }
                    break;
                case '2':
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Select directory of decompression");
                    dirChooser.setInitialDirectory(new File("C:\\Users\\georg\\Desktop\\Huffman compression & decompression"));
                    File file = dirChooser.showDialog(stage);
                    if (file != null) {
                        huffman.decompress("output.bin", file.getAbsolutePath());
                    }
                    break;
                case '3':
                    exit(1);
                default:
                    System.out.println("\nunrecognized input!\n");
            }
        } while (true);
    }
}

