package com.projects.loto_fine.classes_metier;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Cryptage {
    public String getSha1(String donnees) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // On calcule le digest de donnees.
            byte[] messageDigest = md.digest(donnees.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);

            // On convertit le message digest en valeur hexadécimale.
            String hashText = no.toString(16);

            // On ajoute des 0 pour faire en sorte que le message digest fasse 32 bits.
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }

            return hashText;
        }
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPbkdf2(String donnees) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(donnees.toCharArray(), salt, 65536, 128);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return new String(hash, StandardCharsets.UTF_16);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
