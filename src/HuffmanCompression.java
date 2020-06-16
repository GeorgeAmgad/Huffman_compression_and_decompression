import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class HuffmanCompression {

    public static final int BITS_PER_WORD = 8;
    public static final int COMPRESS_FILE = 0;
    public static final int COMPRESS_FOLDER = 1;

    public void compress(File input, String output, int compressionType) {

        int nOfFiles = compressionType == COMPRESS_FILE ? 1 : Objects.requireNonNull(input.listFiles()).length;
        BitOutputStream out = new BitOutputStream(output);
        BitInputStream in;
        Instant start = Instant.now();

        out.write(nOfFiles);  // add number of files

        out.write(0);

        for (int i = 0; i < nOfFiles; i++) {

            if (compressionType == COMPRESS_FOLDER) {
                 in = new BitInputStream(Objects.requireNonNull(input.listFiles())[i]);

            } else {
                 in = new BitInputStream(input.toString());
            }

            Map<Integer, Integer> map = new HashMap<>();

            List<Node> nodeList = new ArrayList<>();


            double sumOfInputBytes = 0;

            int nOfOutedBits = 0;

            int nextCharacter = in.readBits(BITS_PER_WORD);
            while (nextCharacter != -1) {
                sumOfInputBytes++;
                if (map.containsKey(nextCharacter)) {
                    map.replace(nextCharacter, map.get(nextCharacter) + 1);
                } else {
                    map.put(nextCharacter, 1);
                }
                nextCharacter = in.readBits(BITS_PER_WORD);
            }

            map.forEach((key, value) -> nodeList.add(new Node(new HuffByte(key), value)));

            Node root = buildHuffmanTree(nodeList);

            Map<Integer, String> codes = DFS(root);

            int sumOfEncodedBits = 0;
            System.out.println("\t\tCompression Table");
            System.out.println("Byte\t\tCode\t\tNewCode");
            codes.forEach((key, value) -> System.out.println(Integer.toHexString(key.byteValue()) + "\t\t" +
                    Integer.toBinaryString(key.byteValue()) + "   \t\t" + value));

            System.out.println();

            String currFileName = compressionType == COMPRESS_FOLDER ?
                    Objects.requireNonNull(input.listFiles())[i].getName() : input.getName();

            for (char c : currFileName.toCharArray()) {  // add name of file
                out.write(c);
            }

            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                sumOfEncodedBits += entry.getValue() * codes.get(entry.getKey()).length();
            }

            out.write(0);
            for (char c : Integer.toString(sumOfEncodedBits).toCharArray()) {   // add number of bits to decode
                out.write(c);
            }
            out.write(0);
            out.write(codes.size());        // add number of codes in huff table
            out.write(0);
            for (Map.Entry<Integer, String> entry : codes.entrySet()) {
                out.write(entry.getKey());
                out.write(0);
                for (char c : entry.getValue().toCharArray()) {
                    out.write(c);
                }
                out.write(0);
            }

            in.reset();

            nextCharacter = in.read();
            while (nextCharacter != -1) {
                assert codes.containsKey(nextCharacter);
                String code = codes.get(nextCharacter);
                for (char c : code.toCharArray()) {
                    out.writeBits(1, c == '0' ? 0 : 1);  //where magic happens
                    nOfOutedBits++;
                }
                nextCharacter = in.read();
            }
            if (compressionType == COMPRESS_FILE) {
                Instant finish = Instant.now();

                long timeElapsed = Duration.between(start, finish).toMillis();


                System.out.println(" >> compression ratio = " + sumOfInputBytes / (nOfOutedBits / 8));
                System.out.println(" >> Time of compression = " + timeElapsed + " milliseconds");
                System.out.println();
                out.flush();
                in.reset();
            }

            out.write(0);
        }

    }

    public void decompress(String input, String outputDir) {

        int nOfFiles;
        BitInputStream in = new BitInputStream(input);

        StringBuilder outputFile;
        System.out.println(" >> decompressing...");
        Instant start = Instant.now();
        nOfFiles = in.read();

        int dummy;

        BitOutputStream out = null;

        for (int i = 0; i < nOfFiles; i++) {
            outputFile = new StringBuilder();
            dummy = in.read();
            assert dummy == 0;
            int nameChar = in.read();
            while (nameChar != 0) {
                outputFile.append((char) nameChar);
                nameChar = in.read();
            }
            out = new BitOutputStream(outputDir + "\\" + outputFile.toString());


            StringBuilder numString = new StringBuilder();
            int numChar = in.read();
            while (numChar != 0) {
                numString.append((char) numChar);
                numChar = in.read();
            }

            int nOfBits = Integer.parseInt(numString.toString());

            int nOfChars = in.read();
            if (nOfChars == 0) {
                nOfChars = 256;
            }

            dummy = in.read();
            assert dummy == 0;

            StringBuilder code = new StringBuilder();
            Map<String, Integer> codes = new HashMap<>();
            for (int j = 0; j < nOfChars; j++) {
                int character = in.read();
                dummy = in.read();
                assert dummy == 0;
                int current = in.read();
                while (current != 0) {
                    code.append((char) current);
                    current = in.read();
                }
                codes.put(code.toString(), character);
                code = new StringBuilder();
            }

            int current;
            StringBuilder currentCode = new StringBuilder();

            while (nOfBits != 0) {
                current = in.readBits(1);
                currentCode.append(current == 1 ? '1' : '0');
                if (codes.containsKey(currentCode.toString())) {
                    out.write(codes.get(currentCode.toString()));
                    currentCode = new StringBuilder();
                }
                nOfBits--;
            }
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        System.out.println(" >> done!");
        System.out.println(" >> Time of decompression = " + timeElapsed + " millisecond");
        System.out.println();

        Objects.requireNonNull(out).close();
        in.reset();
    }

    static Node buildHuffmanTree(List<Node> nodeList) {
        while (nodeList.size() > 1) {
            nodeList.sort(Node::compareTo);
            Node left = nodeList.remove(0);
            Node right = nodeList.remove(0);
            Node merge = new Node(left.getWeight() + right.getWeight());
            merge.setLeft(left);
            merge.setRight(right);
            nodeList.add(merge);
        }
        return nodeList.get(0);
    }

    static Map<Integer, String> DFS(Node root) {
        Map<Integer, String> returned = new HashMap<>();
        Stack<Node> s = new Stack<>();
        s.add(root);
        while (!s.isEmpty()) {
            Node x = s.pop();
            StringBuilder prevCode = x.getCode();
            if (x.right != null) {
                x.right.code.append(prevCode);
                x.right.code.append(0);
                s.add(x.right);
                if (x.right.getB() != null) {
                    returned.put(x.right.getB().b, x.right.code.toString());
                }

            }
            if (x.left != null) {
                x.left.code.append(prevCode);
                x.left.code.append(1);
                s.add(x.left);
                if (x.left.getB() != null) {
                    returned.put(x.left.getB().b, x.left.code.toString());
                }
            }
        }
        return returned;
    }

}
