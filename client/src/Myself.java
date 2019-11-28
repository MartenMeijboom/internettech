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

    public Key getPrivateKey(){
        try{
            return kp.getPrivate();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getPublicKey(){
        try {
            return pub.getEncoded();
        }catch (NullPointerException e){
            return null;
        }
    }

    public String getPrivateKeyString(){
        return Base64.getEncoder().encodeToString(pvt.getEncoded());

    }

    public String getPublicKeyString(){
        return Base64.getEncoder().encodeToString(pub.getEncoded());
    }


    public String encrypt(byte[] planiEncode) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, kp.getPublic());

        byte[] cipherText = encryptCipher.doFinal(planiEncode);

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public String decrypt(String cipherText) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, kp.getPrivate());

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    public byte[] EncryptSecretKey (SecretKey skey)
    {
        Cipher cipher = null;
        byte[] key = null;

        try
        {
            // initialize the cipher with the user's public key
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pub );
            key = cipher.doFinal(skey.getEncoded());
        }
        catch(Exception e )
        {
            System.out.println ( "exception encoding key: " + e.getMessage() );
            e.printStackTrace();
        }
        return key;
    }

    public SecretKey decryptAESKey(byte[] data )
    {
        SecretKey key = null;
        PrivateKey privKey = null;
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
