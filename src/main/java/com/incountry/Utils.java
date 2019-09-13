package com.incountry;

public class Utils {
    public static String bytesToHex(byte[] bytesArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytesArray) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex)
    {
        int n = hex.length();
        byte[] ba = new byte[n/2];
        int i = 0;
        int j = 0;
        while (i<n)
        {
            String s = hex.substring(i, i+2);
            ba[j++] = (byte)Integer.parseInt(s, 16);
            i += 2;
        }
        return ba;
    }
}
