package dev.xibas.biometrics.view;

public interface LoginView {

    void showBiometricAuthDialog();

    void closeBiometricAuthDialog();

    void showBiometricAuthFailure();

    void showBiometricAuthError();

    void showLoginFailed();
}
