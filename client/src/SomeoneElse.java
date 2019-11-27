import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class SomeoneElse {

    private String name;
    private String pathString;
    private Key pub, sessionKey;

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
            sessionKey = keyGen.generateKey();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Key getSessionKey(){
        return sessionKey;
    }

    public Key getPublicKey(){
        return pub;
    }

    public void setPublicKey(Key pub){
        this.pub = pub;
    }

    public void setSessionKey(Key key){
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
        Key key = getSessionKey();
        Base64.Encoder encoder = Base64.getEncoder();

        return encoder.encodeToString(key.getEncoded());
    }

    public String getPublicKeyString(){
        Key key = getPublicKey();
        Base64.Encoder encoder = Base64.getEncoder();

        return encoder.encodeToString(key.getEncoded());
    }
}
