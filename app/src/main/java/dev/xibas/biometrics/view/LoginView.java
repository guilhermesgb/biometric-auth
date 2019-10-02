package dev.xibas.biometrics.view;

public interface LoginView {

    void fillUsername(String username);

    void showBiometricAuthDialog();

    void closeBiometricAuthDialog();

    void showBiometricAuthEncryptionFailure();

    void showBiometricAuthEncryptionError();

    void showBiometricAuthDecryptionFailure();

    void showBiometricAuthDecryptionError();

    void showLoginFailed();

    void openPrivateArea();

}
