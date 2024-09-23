import java.nio.*;

public class LittleEndian {
    int readInt(byte[] buf, int start) { 
        byte temp[] = new byte[4];
        for (int i=0;i<4;i++)
            temp[i] = buf[start+i];
        return ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN ).getInt(); 
    }  
    float readFloat(byte[] buf, int start) {
        byte temp[] = new byte[4];
        for (int i=0;i<4;i++)
            temp[i] = buf[start+i];
        return ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN ).getFloat();
    }
}