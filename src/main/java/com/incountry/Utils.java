package com.incountry;

public class Utils {
    private Utils() {
        throw new IllegalStateException("Utility class");
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
