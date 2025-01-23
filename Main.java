import Algorithm.AlgorithmUtils;
import Utils.Bytes;
import Utils.FileUtils;
import java.io.*;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Invalid arguments");
            return;
        }
        if (args[0].equals("c")) {
            if (args.length < 3) {
                System.out.println("Invalid number of Args");
                return;
            }
            compress(new File(args[1]), Integer.parseInt(args[2]));
        }
        else if (args[0].equals("d")) decompress(new File(args[1]));
        else System.out.println("Invalid operation");
    }

    public static void compress(File input, int n) throws Exception{
        File output = new File(input.getParent() + "\\" + "21010310." + n + "." + input.getName() + ".hc");
        long start = System.currentTimeMillis();
        Map<Bytes, String> sequenceCodes = AlgorithmUtils.buildTreeCodes(FileUtils.collectStatistics(input, n));
        FileUtils.writeMetaData(sequenceCodes, input, output);
        FileUtils.writeData(sequenceCodes, input, output, n);
        System.out.println("Compression Time: " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("Compression Ratio: " + (double) output.length() / input.length());
    }

    public static void decompress(File input) throws Exception {
        File output = new File(input.getParent() + "\\" + "extracted." + input.getName().substring(0, Math.max(0, input.getName().length() - 3)));
        long start = System.currentTimeMillis();
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(input)); BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(output))) {
            FileUtils.processData(dataInputStream, bufferedOutputStream, dataInputStream.readLong(), FileUtils.extractMap(dataInputStream));
        }
        System.out.println("Decompression Time: " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("Decompression Ratio: " +  (double) output.length() / input.length());
    }
}