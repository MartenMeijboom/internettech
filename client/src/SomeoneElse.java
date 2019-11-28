import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SomeoneElse {

    private String name;
    private String pathString;
    private SecretKey sessionKey;
    private PublicKey pub;

    public SomeoneElse(String name){
        this.name = name;
        pub = null;
        sessionKey = null;
        Security.setProperty("crypto.policy", "unlimited");
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

    public byte[] getPublicKey(){
        try{
            return pub.getEncoded();
        }catch (NullPointerException e){
            return null;
        }

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


    public String encrypt(String strToEncrypt)
    {
        try
        {
            System.out.println("E: " + Base64.getEncoder().encodeToString(sessionKey.getEncoded()));
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public String decrypt(String strToDecrypt)
    {
        try
        {
            System.out.println("D: " + Base64.getEncoder().encodeToString(sessionKey.getEncoded()));
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sessionKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
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
}
