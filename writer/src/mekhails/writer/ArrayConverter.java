package mekhails.writer;

import java.nio.ByteBuffer;

class ArrayConverter
{
    static short[] byteToShort(byte[] src)
    {
        if (src == null || src.length % 2 != 0) return null;

        short[] res = new short[src.length/2];

        ByteBuffer byteBuffer = ByteBuffer.wrap(src);
        for (int i = 0; i < res.length; i++)
        {
            res[i] = byteBuffer.getShort(2*i);
        }
        return res;
    }

    static byte[] shortToByte(short[] src)
    {
        if (src == null) return null;

        ByteBuffer byteBuffer = ByteBuffer.allocate(2 * src.length);
        for (short shortCur : src) {
            byteBuffer.putShort(shortCur);
        }
        return byteBuffer.array();
    }


}
