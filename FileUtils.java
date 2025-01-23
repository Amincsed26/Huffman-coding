package Utils;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {
    public static Map<Bytes, Integer> collectStatistics(File input, int n) throws Exception {
        Map<Bytes, Integer> frequency = new HashMap<>();
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(input))) {
            byte[] buffer = new byte[(int) (Math.min(512 * 1024, input.length()) - (Math.min(512 * 1024, input.length()) % n))];
            int count;
            while ((count = bufferedInputStream.read(buffer)) != -1) {
                for (int i = 0; i < count; i += n)
                    frequency.merge(new Bytes(Arrays.copyOfRange(buffer, i, Math.min(count, i + n))), 1, Integer::sum);
            }
        }
        return frequency;
    }

    public static void writeMetaData(Map<Bytes, String> sequenceCodes, File input, File output) throws Exception {
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(output))) {
            dataOutputStream.writeLong(input.length());
            dataOutputStream.writeInt(sequenceCodes.size());
            for (Map.Entry<Bytes, String> sequence : sequenceCodes.entrySet()) {
                dataOutputStream.writeInt(sequence.getKey().bytes.length);
                dataOutputStream.write(sequence.getKey().bytes);
                dataOutputStream.writeInt(sequence.getValue().length());
                byte[] bytes = new byte[(sequence.getValue().length() + 7) / 8];
                for (int i = 0; i < sequence.getValue().length(); i++) {
                    if (sequence.getValue().charAt(i) == '1')
                        bytes[i / 8] |= (byte) (1 << 7 - (i % 8));
                }
                dataOutputStream.write(bytes);
            }
        }
    }

    public static void writeData(Map<Bytes, String> sequenceCodes, File input, File output, int n) throws Exception {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(input));
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(output, true))) {
            int bufferSize = (int) (Math.min(512 * 1024, input.length()) - (Math.min(512 * 1024, input.length()) % n));
            byte[] bufferA = new byte[bufferSize];
            byte[] bufferB = new byte[bufferSize];
            int count, outputIndex = 0, bitAccumulator = 0, accumulatedBits = 0;
            while ((count = bufferedInputStream.read(bufferA)) != -1) {
                for (int i = 0; i < count; i += n) {
                    String code = sequenceCodes.get(new Bytes(Arrays.copyOfRange(bufferA, i, Math.min(count, i + n))));
                    for (int j = 0; j < code.length(); j++) {
                        bitAccumulator = (bitAccumulator << 1) | (code.charAt(j) - '0');
                        accumulatedBits++;
                        if (accumulatedBits == 8) {
                            bufferB[outputIndex++] = (byte) bitAccumulator;
                            if (outputIndex == bufferSize) {
                                bufferedOutputStream.write(bufferB);
                                outputIndex = 0;
                            }
                            bitAccumulator = 0;
                            accumulatedBits = 0;
                        }
                    }
                }
            }
            if (accumulatedBits > 0 || outputIndex > 0) {
                if (accumulatedBits > 0) {
                    bitAccumulator <<= (8 - accumulatedBits);
                    bufferB[outputIndex++] = (byte) bitAccumulator;
                }
                if (outputIndex > 0) {
                    bufferedOutputStream.write(bufferB, 0, outputIndex);
                }
            }
        }
    }

    public static Map<String, Bytes> extractMap(DataInputStream dataInputStream) throws Exception {
        Map<String, Bytes> sequenceCodes = new HashMap<>();
        int numberOfEntries = dataInputStream.readInt();
        for (int i = 0; i < numberOfEntries; i++) {
            Bytes data = new Bytes(dataInputStream.readNBytes(dataInputStream.readInt()));
            int size = dataInputStream.readInt();
            byte[] codeBytes = dataInputStream.readNBytes((int) Math.ceil(size / 8.0));
            sequenceCodes.put(getCodeString(codeBytes, size), data);
        }
        return sequenceCodes;
    }

    private static String getCodeString(byte[] codeBytes, int size) {
        StringBuilder codeBuilder = new StringBuilder(size);
        for (int i = 0; i < codeBytes.length && size > 0; i++) {
            for (int j = 7; j >= 0 && size > 0; j--) {
                if ((codeBytes[i] & (1 << j)) != 0) codeBuilder.append('1');
                else codeBuilder.append('0');
                size--;
            }
        }
        return codeBuilder.toString();
    }

    public static void processData(DataInputStream dataInputStream, BufferedOutputStream bufferedOutputStream, long inputFileSize, Map<String, Bytes> sequenceCodes) throws Exception {
        int bufferSize = (int) Math.min(512 * 1024, inputFileSize);
        byte[] inputBuffer = new byte[bufferSize];
        byte[] outputBuffer = new byte[bufferSize];

        StringBuilder sequence = new StringBuilder();
        long remainingBytes = inputFileSize;
        int outputIndex = 0;
        int count;
        while ((count = dataInputStream.read(inputBuffer)) != -1) {
            for (int i = 0; i < count && remainingBytes > 0; i++) {
                byte currentByte = inputBuffer[i];
                for (int bitIndex = 7; bitIndex >= 0 && remainingBytes > 0; bitIndex--) {
                    sequence.append((currentByte & (1 << bitIndex)) != 0 ? '1' : '0');
                    if (sequenceCodes.containsKey(sequence.toString())) {
                        Bytes bytes = sequenceCodes.get(sequence.toString());
                        if (bytes != null) {
                            for (byte b : bytes.bytes) {
                                outputBuffer[outputIndex++] = b;
                                if (outputIndex == bufferSize) {
                                    bufferedOutputStream.write(outputBuffer);
                                    outputIndex = 0;
                                }
                            }
                            remainingBytes -= bytes.bytes.length;
                            sequence.setLength(0);
                        }
                    }
                }
            }
        }
        if (outputIndex > 0)
            bufferedOutputStream.write(outputBuffer, 0, outputIndex);
    }
}