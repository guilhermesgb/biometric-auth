package dev.xibas.biometrics.view;

public interface SettingsView {

    void updateBiometricAuthToggle(boolean enabled, boolean supported);

    void showBiometricErrorNoHardware();

    void showBiometricErrorHardwareUnavailable();

    void showBiometricAuthActivationDialog();

    void closeBiometricAuthDialog();

    void openSecuritySettings();

}
