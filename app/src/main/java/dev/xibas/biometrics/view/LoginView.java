package dev.xibas.biometrics.view;

public interface LoginView {

    void fillUsername(String username);

    void showBiometricAuthDialog();

    void closeBiometricAuthDialog();

    void showBiometricAuthFailure();

    void showBiometricAuthError();

    void showLoginFailed();

    void openPrivateArea();

}
