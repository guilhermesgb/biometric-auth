package dev.xibas.biometrics.presenter.setttings;

import dev.xibas.biometrics.model.Manager;
import dev.xibas.biometrics.presenter.proto.Presenter;
import dev.xibas.biometrics.view.SettingsView;

public interface SettingsPresenter extends Presenter<SettingsView>, Manager.ManagerListener {

    void onBiometricAuthToggleChecked(boolean checked);

    void onBiometricAuthActivationDialogGoToSecuritySettingsClicked();

    void onBiometricAuthActivationDialogCloseClicked();

}
