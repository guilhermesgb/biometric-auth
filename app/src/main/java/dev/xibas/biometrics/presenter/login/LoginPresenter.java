package dev.xibas.biometrics.presenter.login;

import dev.xibas.biometrics.model.Manager;
import dev.xibas.biometrics.presenter.proto.Presenter;
import dev.xibas.biometrics.view.LoginView;

public interface LoginPresenter extends Presenter<LoginView>, Manager.ManagerListener {

    void onSubmitLoginClicked(String username, String password);

    void onUseBiometricAuthClicked();

    void onDoNotUseBiometricAuthClicked();

    void onDisableBiometricAuthClicked();

}
