package dev.xibas.biometrics.presenter.login;

import dev.xibas.biometrics.model.Manager;

public interface LoginPresenter extends Manager.ManagerListener {

    void onSubmitLoginClicked(String username, String password);

    void onUseBiometricAuthClicked();

    void onDoNotUseBiometricAuthClicked();

    void onDisableBiometricAuthClicked();

}
