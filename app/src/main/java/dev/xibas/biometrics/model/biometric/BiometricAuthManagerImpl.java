package dev.xibas.biometrics.model.biometric;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.security.keystore.KeyPermanentlyInvalidatedException;

import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.security.UnrecoverableKeyException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dev.xibas.biometrics.model.biometric.crypto.CryptoPasswordHandler;
import dev.xibas.biometrics.model.biometric.crypto.DecryptPasswordHandler;
import dev.xibas.biometrics.model.biometric.crypto.EncryptPasswordHandler;
import dev.xibas.biometrics.model.biometric.util.PasswordHandlerHelper;
import dev.xibas.biometrics.model.proto.AbstractManager;
import timber.log.Timber;

import static android.Manifest.permission.USE_FINGERPRINT;
import static androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
import static androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
import static dev.xibas.biometrics.model.biometric.BiometricAuthManager.BiometricAuthError.BIOMETRIC_AUTH_DATA_EXPIRED;
import static dev.xibas.biometrics.model.biometric.BiometricAuthManager.BiometricAuthError.UNABLE_TO_INIT_DECRYPTION;
import static dev.xibas.biometrics.model.biometric.BiometricAuthManager.BiometricAuthError.UNABLE_TO_INIT_ENCRYPTION;
import static dev.xibas.biometrics.model.biometric.crypto.CryptoPasswordHandler.SUFFIX_STORED_PASSWORD;

public class BiometricAuthManagerImpl extends AbstractManager implements BiometricAuthManager {

    private static final String BIOMETRIC_AUTH_ENABLED = "_biometric_auth_enabled_";

    private static BiometricAuthManager instance;

    private final Context context;

    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private final Executor backgroundThread = Executors.newSingleThreadExecutor();

    private SharedPreferences secureStorage;
    private PasswordHandlerHelper passwordHandlerHelper;

    private boolean biometricAuthEnabled;
    private int biometricAuthSupportFlag;

    public static BiometricAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new BiometricAuthManagerImpl(context);
        }
        return instance;
    }

    private BiometricAuthManagerImpl(Context context) {
        this.context = context;

        if (checkSecureStorageSupport()) {
            passwordHandlerHelper = new PasswordHandlerHelper(mainThread, secureStorage);

            biometricAuthEnabled = secureStorage.getBoolean(BIOMETRIC_AUTH_ENABLED, true);
        }
    }

    @Override
    public int getBiometricAuthSupportFlag() {

        return biometricAuthSupportFlag;
    }

    @Override
    public void getBiometricAuthStatus(BiometricAuthStatusListener listener) {
        boolean biometricAuthSupported = checkBiometricAuthSupport();
        listener.onBiometricAuthStatusUpdated(biometricAuthSupported
                && biometricAuthEnabled, biometricAuthSupported);
    }

    @Override
    public void setBiometricAuthEnabled(boolean enabled, BiometricAuthStatusListener listener) {
        if (checkBiometricAuthSupport()) {
            biometricAuthEnabled = enabled;

            secureStorage
                    .edit()
                    .putBoolean(BIOMETRIC_AUTH_ENABLED,
                            biometricAuthEnabled)
                    .apply();

            listener.onBiometricAuthStatusUpdated
                    (biometricAuthEnabled, true);

        } else {
            listener.onBiometricAuthStatusUpdated
                    (false, false);
        }

        notifyUpdate();
    }

    private boolean checkBiometricAuthSupport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            biometricAuthSupportFlag = BiometricManager
                    .from(context).canAuthenticate();

            return biometricAuthSupportFlag == BIOMETRIC_SUCCESS
                    && checkPermissionForFingerprintUseGranted(context)
                    && checkSecureStorageSupport();
        }
        biometricAuthSupportFlag = BIOMETRIC_ERROR_NO_HARDWARE;
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissionForFingerprintUseGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkSecureStorageSupport() {
        return secureStorage != null || createSecureStorage();
    }

    private boolean createSecureStorage() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            secureStorage = EncryptedSharedPreferences.create
                    ("secret_shared_prefs", masterKeyAlias, context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            return true;

        } catch (Exception exception) {
            Timber.e("Device doesn't support secure password storage. Biometric authentication is deemed unavailable.");
            return false;
        }
    }

    @Override
    public boolean userHasBiometricAuthEncryptedPassword(String username) {
        return secureStorage.contains(username + SUFFIX_STORED_PASSWORD);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void encryptUserPasswordWithBiometricAuth(String username, String password,
                                                     FragmentActivity fragmentActivity,
                                                     BiometricPrompt.PromptInfo promptInfo,
                                                     BiometricAuthCallback callback) {
        EncryptPasswordHandler encryptPasswordHandler = passwordHandlerHelper
                .obtainEncryptHandler(username, password, callback);

        try {
            obtainBiometricPrompt(fragmentActivity, encryptPasswordHandler)
                    .authenticate(promptInfo, obtainCryptoObject(encryptPasswordHandler));

        } catch (Exception exception) {
            Timber.e("Biometric Auth error: Biometric prompt init failure: %s", exception.getMessage());
            mainThread.post(() -> callback.onAuthError(UNABLE_TO_INIT_ENCRYPTION));
        }
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void decryptUserPasswordWithBiometricAuth(String username, FragmentActivity fragmentActivity,
                                                     BiometricPrompt.PromptInfo promptInfo,
                                                     BiometricAuthCallback callback) {
        DecryptPasswordHandler decryptPasswordHandler = passwordHandlerHelper
                .obtainDecryptHandler(username, callback);

        try {
            obtainBiometricPrompt(fragmentActivity, decryptPasswordHandler)
                    .authenticate(promptInfo, obtainCryptoObject(decryptPasswordHandler));

        } catch (UnrecoverableKeyException | KeyPermanentlyInvalidatedException exception) {
            Timber.e("Biometric Auth error: %s", exception.getMessage());
            mainThread.post(() -> callback.onAuthError(BIOMETRIC_AUTH_DATA_EXPIRED));

            decryptPasswordHandler.wipeEncryptedData();

        } catch (Exception exception) {
            Timber.e("Biometric Auth error: Biometric prompt init failure: %s", exception.getMessage());
            mainThread.post(() -> callback.onAuthError(UNABLE_TO_INIT_DECRYPTION));
        }
    }

    private BiometricPrompt obtainBiometricPrompt(FragmentActivity fragmentActivity,
                                                  BiometricPrompt.AuthenticationCallback callback) {

        return new BiometricPrompt(fragmentActivity, backgroundThread, callback);
    }

    private BiometricPrompt.CryptoObject obtainCryptoObject(CryptoPasswordHandler passwordHandler) throws Exception {
        return new BiometricPrompt.CryptoObject(passwordHandler.obtainCipher());
    }

    @Override
    public void wipeEncryptedUserPasswordWithBiometricAuth(String username, BiometricAuthCallback callback) {
        DecryptPasswordHandler decryptPasswordHandler = passwordHandlerHelper
                .obtainDecryptHandler(username, callback);

        decryptPasswordHandler.wipeEncryptedData();
        callback.onAuthCancel();
    }

}
