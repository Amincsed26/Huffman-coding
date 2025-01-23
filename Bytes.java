package Utils;
import java.util.Arrays;

public class Bytes {
    public byte[] bytes;

    public Bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bytes other = (Bytes) obj;
        return Arrays.equals(this.bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }
}