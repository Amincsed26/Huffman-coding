package Utils;

public class Node {
    public Bytes bytes;
    public int frequency;
    public Node leftNode;
    public Node rightNode;

    public Node(Bytes bytes, int frequency) {
        this.bytes = bytes;
        this.frequency = frequency;
    }

    public Node(int frequency, Node leftNode, Node rightNode) {
        this.frequency = frequency;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }
}