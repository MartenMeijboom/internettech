import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Myself {

    private String name;
    private String pathString;
    private Key pub, pvt;
    private KeyPair kp;

    public Myself(){
        Security.setProperty("crypto.policy", "unlimited");
        pathString = "./keys/";
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void generateKeys(){
        try{
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

            kpg.initialize(1024);
            kp = kpg.generateKeyPair();

            pub = kp.getPublic();
            pvt = kp.getPrivate();

            FileOutputStream writer = new FileOutputStream(pathString + "my.key");
            writer.write(pvt.getEncoded());
            writer.close();

            writer = new FileOutputStream(pathString + "my.pub");
            writer.write(pub.getEncoded());
            writer.close();

            //System.out.println("Private key format: " + pvt.getFormat());
            //System.out.println("Public key format: " + pub.getFormat());

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public byte[] getPublicKey(){
        try {
            return pub.getEncoded();
        }catch (NullPointerException e){
            return null;
        }
    }


    public String getPublicKeyString(){
        return Base64.getEncoder().encodeToString(pub.getEncoded());
    }


    public SecretKey decryptAESKey(byte[] data )
    {
        SecretKey key = null;
        Cipher cipher = null;

        try
        {
            // initialize the cipher...
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pvt );

            // generate the aes key!
            key = new SecretKeySpec( cipher.doFinal(data), "AES" );
        }
        catch(Exception e)
        {
            System.out.println ( "exception decrypting the aes key: "
                    + e.getMessage() );
            return null;
        }

        return key;
    }
}
