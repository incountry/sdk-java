package com.incountry;
import com.incountry.key_accessor.SecretKeyAccessor;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.Provider;
import java.security.Security;


public class TestCognizant {
    public static void main(String[] args) throws Exception{
        try {
            // This is to allow Java 7 to work with the newer algorithm for encryption
            Provider bcp = null;
            try {
                ClassLoader cl = TestCognizant.class.getClassLoader();
                URL url =  cl.getResource("org/bouncycastle/jce/provider/BouncyCastleProvider.class");
                if ("jar".equals(url.getProtocol())) {
                    url = new URL(url.getPath().substring(0, url.getPath().indexOf('!')));
                    cl = new URLClassLoader(new URL[]{url}, null);
                    Class cls = cl.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");

                    // This uses a deprecated method to work with Java 7
                    // There might be a cleaner way to get this to work
                    bcp = (Provider) cls.newInstance();
                    Security.addProvider(bcp);
                }
            } catch (Exception ignored) {
                System.out.println("Could not load Bouncy Castle for some odd reason.");
            }

            String environmentID = "689f7634-b6e7-474e-bef3-a1bb5f935007";
            String apiKey = "mxqkoc.ed536a176fc84deaa0ef1b5a1d44cdff";
            String endpoint = "https://ruc1.api.incountry.io";
            String secretKey = "I-qaEXivUuLHEt-YExsr";
            SecretKeyAccessor saa = new SecretKeyAccessor(secretKey);
            Storage store = new Storage(environmentID, apiKey, endpoint, saa != null, saa);
            Record dd = store.read("ru", "yeehaw");
            if (dd != null) System.out.println(dd.getBody());
            else System.out.println("not found");
        }
        catch (Exception exc) {
            System.out.println(exc.toString());
        }
    }
}