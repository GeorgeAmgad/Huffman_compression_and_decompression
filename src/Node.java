import org.jetbrains.annotations.NotNull;
@SuppressWarnings("rawtypes")
public class Node implements Comparable {

    private HuffByte b;
    int weight;
    Node left;
    Node right;
    StringBuilder code;

    public Node(HuffByte b, int weight) {
        this.b = b;
        this.weight = weight;
        this.left = null;
        this.right = null;
        this.code = new StringBuilder();
    }

    public Node(int weight) {
        b = null;
        this.weight = weight;
        this.left = null;
        this.right = null;
        this.code = new StringBuilder();
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public int getWeight() {
        return weight;
    }

    public HuffByte getB() {
        return b;
    }

    public StringBuilder getCode() {
        return code;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        return (Integer.compare(this.getWeight(), ((Node) o).getWeight()));
    }
}
