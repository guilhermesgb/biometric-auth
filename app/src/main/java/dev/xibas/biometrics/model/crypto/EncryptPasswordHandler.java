package dev.xibas.biometrics.model.crypto;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import dev.xibas.biometrics.model.BiometricAuthManager;
import timber.log.Timber;

import static dev.xibas.biometrics.model.BiometricAuthManager.BiometricAuthError.UNABLE_TO_EXE_ENCRYPTION;
import static dev.xibas.biometrics.model.BiometricAuthManager.BiometricAuthError.UNABLE_TO_STORE_SECURELY;

public class EncryptPasswordHandler extends CryptoPasswordHandler {

    private final String username;
    private final String password;

    public EncryptPasswordHandler(String username, String password, Handler mainThread, SharedPreferences secureStorage,
                                  BiometricAuthManager.BiometricAuthCallback callback) {

        super(mainThread, secureStorage, callback);
        this.username = username;
        this.password = password;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private KeyGenParameterSpec createBiometricKeyGenParameterSpec() {
        return new KeyGenParameterSpec.Builder(BIOMETRIC_AUTH_KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setKeySize(KEY_SIZE)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public Cipher obtainCipher() throws Exception {

        SecretKey biometricKey = obtainSecretKey();
        Cipher biometricCipher = Cipher.getInstance
                (KeyProperties.KEY_ALGORITHM_AES
                        + "/" + KeyProperties.BLOCK_MODE_CBC
                        + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        biometricCipher.init(Cipher.ENCRYPT_MODE, biometricKey);
        return biometricCipher;
    }

    @Override
    public void performPasswordCryptoOperation(Cipher biometricCipher) {
        try {
            String encryptedPassword = Base64.encodeToString(biometricCipher
                    .doFinal(password.getBytes()), Base64.DEFAULT);

            SharedPreferences.Editor editor = secureStorage.edit();
            editor.putString(username + SUFFIX_STORED_INIT_VECTOR, Base64
                    .encodeToString(biometricCipher.getIV(), Base64.DEFAULT));
            editor.putString(username + SUFFIX_STORED_PASSWORD, encryptedPassword);

            if (editor.commit()) {
                mainThread.post(() -> callback.onAuthSuccess(username, password));

            } else {
                Timber.e("Biometric Auth error: Secure password storage failed");
                mainThread.post(() -> callback.onAuthError(UNABLE_TO_STORE_SECURELY));
            }

        } catch (Exception exception) {
            Timber.e("Biometric Auth error: Secure password encryption failed: %s", exception.getMessage());
            mainThread.post(() -> callback.onAuthError(UNABLE_TO_EXE_ENCRYPTION));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey obtainSecretKey() throws Exception {

        KeyGenerator keyGenerator = KeyGenerator.getInstance
                (KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        keyGenerator.init(createBiometricKeyGenParameterSpec());
        return keyGenerator.generateKey();
    }

}
