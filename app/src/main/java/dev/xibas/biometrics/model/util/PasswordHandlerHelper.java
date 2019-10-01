package dev.xibas.biometrics.model.util;

import android.content.SharedPreferences;
import android.os.Handler;

import dev.xibas.biometrics.model.BiometricAuthManager;
import dev.xibas.biometrics.model.crypto.DecryptPasswordHandler;
import dev.xibas.biometrics.model.crypto.EncryptPasswordHandler;

public class PasswordHandlerHelper {

    private final Handler mainThread;
    private final SharedPreferences secureStorage;

    public PasswordHandlerHelper(Handler mainThread, SharedPreferences secureStorage) {

        this.mainThread = mainThread;
        this.secureStorage = secureStorage;
    }

    public EncryptPasswordHandler obtainEncryptHandler(String username, String password,
                                                       BiometricAuthManager.BiometricAuthCallback callback) {

        return new EncryptPasswordHandler(username, password, mainThread, secureStorage, callback);
    }

    public DecryptPasswordHandler obtainDecryptHandler(String username,
                                                       BiometricAuthManager.BiometricAuthCallback callback) {

        return new DecryptPasswordHandler(username, mainThread, secureStorage, callback);
    }

}
