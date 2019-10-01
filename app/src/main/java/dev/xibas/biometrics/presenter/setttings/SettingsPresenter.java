package dev.xibas.biometrics.presenter.setttings;

import dev.xibas.biometrics.model.Manager;

public interface SettingsPresenter extends Manager.ManagerListener {

    void onBiometricAuthToggleChecked(boolean checked);

    void onBiometricAuthActivationDialogGoToSecuritySettingsClicked();

    void onBiometricAuthActivationDialogCloseClicked();

}
