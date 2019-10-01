package dev.xibas.biometrics.model;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

public interface BiometricAuthManager extends Manager {

    /*
        Biometric authentication ACTIVATION-related classes and methods
     */

    interface BiometricAuthStatusListener {
        void onBiometricAuthStatusUpdated(boolean enabled, boolean supported);
    }

    int getBiometricAuthSupportFlag();

    void getBiometricAuthStatus(BiometricAuthStatusListener listener);

    void setBiometricAuthEnabled(boolean enabled, BiometricAuthStatusListener listener);

    // End of ACTIVATION-related classes and methods

    /*
        Biometric authentication EXECUTION-related classes and methods
     */

    interface BiometricAuthCallback {
        void onAuthSuccess(String username, String password);

        void onAuthFailure();

        void onAuthError(BiometricAuthError error);

        void onAuthCancel();
    }

    enum BiometricAuthError {
        UNABLE_TO_INIT_ENCRYPTION,
        UNABLE_TO_INIT_DECRYPTION,
        BIOMETRIC_AUTH_DATA_EXPIRED,
        BIOMETRIC_AUTH_EXE_ERROR,
        UNABLE_TO_FIND_CRYPTO_DATA,
        UNABLE_TO_STORE_SECURELY,
        UNABLE_TO_EXE_ENCRYPTION,
        UNABLE_TO_EXE_DECRYPTION,
    }

    boolean userHasBiometricAuthEncryptedPassword(String username);

    /**
     * Requests Biometric authentication. Upon success, perform encryption of the user's password.
     * @param username the username of the user, whose password shall be securely stored.
     * @param password the password of the user, to be securely stored in encrypted form.
     * @param fragmentActivity the context from which Biometric authentication will be prompted
     *                         - I know it sounds fishy to have it here but the BiometricPrompt API
     *                         requires a FragmentActivity or Fragment when building the prompt instance.
     *                         There's no way around it, but in any case I think all of this flow makes
     *                         sense encapsulated in one of our managers, so I'm passing it as part of
     *                         the requested crypto operation's parameters.
     * @param promptInfo an object containing the customised info to be shown by the BiometricPrompt System UI
     * @param callback a callback to be fired when Biometric authentication
     *                 + secure encryption of user's password completes.
     */
    void encryptUserPasswordWithBiometricAuth(String username, String password,
                                              FragmentActivity fragmentActivity,
                                              BiometricPrompt.PromptInfo promptInfo,
                                              BiometricAuthCallback callback);

    /**
     * Requests Biometric authentication. Upon success, perform decryption of the user's password.
     * @param username the username of the user, whose password shall be securely retrieved.
     * @param fragmentActivity the context from which Biometric authentication will be prompted
     *                         - I know it sounds fishy to have it here but the BiometricPrompt API
     *                         requires a FragmentActivity or Fragment when building the prompt instance.
     *                         There's no way around it, but in any case I think all of this flow makes
     *                         sense encapsulated in one of our managers, so I'm passing it as part of
     *                         the requested crypto operation's parameters.
     * @param promptInfo an object containing the customised info to be shown by the BiometricPrompt System UI
     * @param callback a callback to be fired when Biometric authentication
     *                 + secure decryption of user's password completes.
     */
    void decryptUserPasswordWithBiometricAuth(String username, FragmentActivity fragmentActivity,
                                              BiometricPrompt.PromptInfo promptInfo,
                                              BiometricAuthCallback callback);

    void wipeEncryptedUserPasswordWithBiometricAuth(String username, BiometricAuthCallback callback);

    // End of EXECUTION-related classes and methods

}
