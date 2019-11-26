import javax.crypto.Cipher;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Myself {

    private String name;
    private String pathString;
    private Key pub, pvt;

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

            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

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

    public PrivateKey getPrivateKey(){
        try{
            Path path = Paths.get(pathString + "my.key");
            byte[] bytes = Files.readAllBytes(path);

            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);

            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePrivate(ks);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public PublicKey getPublicKey(){
        try{
            Path path = Paths.get(pathString + "my.pub");
            byte[] bytes = Files.readAllBytes(path);

            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getPrivateKeyString(){
        PrivateKey key = getPrivateKey();
        Base64.Encoder encoder = Base64.getEncoder();

        return encoder.encodeToString(key.getEncoded());
    }

    public String getPublicKeyString(){
        PublicKey key = getPublicKey();
        Base64.Encoder encoder = Base64.getEncoder();

        return encoder.encodeToString(key.getEncoded());
    }

    public byte[] encrypt(byte[] plaintext) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        return cipher.doFinal(ciphertext);
    }

}
