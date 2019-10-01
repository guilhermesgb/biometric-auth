package dev.xibas.biometrics.model.biometric.crypto;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import dev.xibas.biometrics.model.biometric.BiometricAuthManager;
import timber.log.Timber;

import static dev.xibas.biometrics.model.biometric.BiometricAuthManager.BiometricAuthError.UNABLE_TO_EXE_DECRYPTION;

public class DecryptPasswordHandler extends CryptoPasswordHandler {

    private final String username;

    public DecryptPasswordHandler(String username, Handler mainThread, SharedPreferences secureStorage,
                                  BiometricAuthManager.BiometricAuthCallback callback) {

        super(mainThread, secureStorage, callback);
        this.username = username;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public Cipher obtainCipher() throws Exception {

        byte[] IV = obtainInitVector(username);
        SecretKey biometricKey = obtainSecretKey();
        Cipher biometricCipher = Cipher.getInstance
                (KeyProperties.KEY_ALGORITHM_AES
                        + "/" + KeyProperties.BLOCK_MODE_CBC
                        + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        biometricCipher.init(Cipher.DECRYPT_MODE, biometricKey, new IvParameterSpec(IV));
        return biometricCipher;
    }

    public void wipeEncryptedData() {
        secureStorage.edit()
                .remove(username + SUFFIX_STORED_INIT_VECTOR)
                .remove(username + SUFFIX_STORED_PASSWORD)
                .apply();
    }

    @Override
    protected void performPasswordCryptoOperation(Cipher biometricCipher) {
        try {
            String encryptedPassword = secureStorage.getString
                    (username + SUFFIX_STORED_PASSWORD, "");

            String password = new String(biometricCipher.doFinal(Base64.decode(encryptedPassword, Base64.DEFAULT)));
            mainThread.post(() -> callback.onAuthSuccess(username, password));

        } catch (Exception exception) {
            Timber.e("Biometric Auth error: Secure password decryption failed: %s", exception.getMessage());
            mainThread.post(() -> callback.onAuthError(UNABLE_TO_EXE_DECRYPTION));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private byte[] obtainInitVector(String username) {

        return Base64.decode(secureStorage.getString(username
                + SUFFIX_STORED_INIT_VECTOR, ""), Base64.DEFAULT);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey obtainSecretKey() throws Exception {

        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(BIOMETRIC_AUTH_KEYSTORE_ALIAS, null);
    }

}
