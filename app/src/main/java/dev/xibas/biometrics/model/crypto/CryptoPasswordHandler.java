package dev.xibas.biometrics.model.crypto;

import android.content.SharedPreferences;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import javax.crypto.Cipher;

import dev.xibas.biometrics.model.BiometricAuthManager;
import timber.log.Timber;

import static androidx.biometric.BiometricConstants.ERROR_NEGATIVE_BUTTON;
import static androidx.biometric.BiometricConstants.ERROR_USER_CANCELED;
import static dev.xibas.biometrics.model.BiometricAuthManager.BiometricAuthError.BIOMETRIC_AUTH_EXE_ERROR;
import static dev.xibas.biometrics.model.BiometricAuthManager.BiometricAuthError.UNABLE_TO_FIND_CRYPTO_DATA;

public abstract class CryptoPasswordHandler extends BiometricPrompt.AuthenticationCallback {

    public static final String SUFFIX_STORED_PASSWORD = ".pwd";

    static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    static final String BIOMETRIC_AUTH_KEYSTORE_ALIAS = "_biometric_auth_keystore_alias_";
    static final String SUFFIX_STORED_INIT_VECTOR = ".iv";
    static final int KEY_SIZE = 256;

    final Handler mainThread;
    final SharedPreferences secureStorage;
    final BiometricAuthManager.BiometricAuthCallback callback;

    CryptoPasswordHandler(Handler mainThread, SharedPreferences secureStorage,
                          BiometricAuthManager.BiometricAuthCallback callback) {

        this.mainThread = mainThread;
        this.secureStorage = secureStorage;
        this.callback = callback;
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);

        if (errorCode == ERROR_NEGATIVE_BUTTON || errorCode == ERROR_USER_CANCELED) {
            mainThread.post(callback::onAuthCancel);

        } else {
            Timber.e("Biometric Auth error: " + errString + " (" + errorCode + ")");
            mainThread.post(() -> callback.onAuthError(BIOMETRIC_AUTH_EXE_ERROR));
        }
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull final BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        BiometricPrompt.CryptoObject biometricCrypto = result.getCryptoObject();
        if (biometricCrypto == null) {
            Timber.e("Biometric Auth error: Missing crypto object");
            mainThread.post(() -> callback.onAuthError(UNABLE_TO_FIND_CRYPTO_DATA));
            return;
        }

        Cipher biometricCipher = biometricCrypto.getCipher();
        if (biometricCipher == null) {
            Timber.e("Biometric Auth error: Missing crypto cipher");
            mainThread.post(() -> callback.onAuthError(UNABLE_TO_FIND_CRYPTO_DATA));
            return;
        }

        performPasswordCryptoOperation(biometricCipher);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();

        mainThread.post(callback::onAuthFailure);
    }

    public abstract Cipher obtainCipher() throws Exception;

    protected abstract void performPasswordCryptoOperation(Cipher biometricCipher);

}
