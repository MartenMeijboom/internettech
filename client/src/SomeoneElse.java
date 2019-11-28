import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SomeoneElse {

    private String name;
    private String pathString;
    private SecretKey sessionKey;
    private PublicKey pub;

    public SomeoneElse(String name){
        this.name = name;
        pub = null;
        sessionKey = null;
    }

    public String getName(){
        return name;
    }

    public void generateSessionKey(){
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            sessionKey = keyGen.generateKey();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public SecretKey getSessionKey(){
        return sessionKey;
    }

    public Key getPublicKey(){
        return pub;
    }

    public void setPublicKey(byte[] bytes){
        try {
            pub = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setSessionKey(SecretKey key){
        this.sessionKey = key;
    }

    public byte[] encryptWithPub(byte[] plaintext) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pub);
        return cipher.doFinal(plaintext);
    }

    public byte[] encryptWithSession(byte[] plaintext) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sessionKey);
        return cipher.doFinal(ciphertext);
    }

    public String getSessionKeyString(){
        return Base64.getEncoder().encodeToString(getSessionKey().getEncoded());
    }

    public String getPublicKeyString(){
        Key key = getPublicKey();
        Base64.Encoder encoder = Base64.getEncoder();

        return encoder.encodeToString(key.getEncoded());
    }
}
