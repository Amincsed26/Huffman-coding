package Algorithm;
import Utils.Bytes;
import Utils.Node;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class AlgorithmUtils {
    public static Map<Bytes, String> buildTreeCodes(Map<Bytes, Integer> frequency){
        Map<Bytes, String> sequenceCodes = new HashMap<>();
        buildCodes(sequenceCodes, AlgorithmUtils.buildTree(frequency), "");
        return sequenceCodes;
    }

    public static Node buildTree(Map<Bytes, Integer> frequency) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.frequency));
        for (Map.Entry<Bytes, Integer> entry : frequency.entrySet())
            priorityQueue.add(new Node(entry.getKey(), entry.getValue()));

        while (priorityQueue.size() > 1) {
            Node leftNode = priorityQueue.poll();
            Node rightNode = priorityQueue.poll();
            assert rightNode != null;
            priorityQueue.add(new Node(leftNode.frequency + rightNode.frequency, leftNode, rightNode));
        }
        return priorityQueue.poll();
    }

    public static void buildCodes(Map<Bytes, String> sequenceCodes, Node node, String code) {
        if(node == null) return;
        if (node.leftNode == null && node.rightNode == null) sequenceCodes.put(node.bytes, code);
        buildCodes(sequenceCodes, node.leftNode, code + "0");
        buildCodes(sequenceCodes, node.rightNode, code + "1");
    }
}