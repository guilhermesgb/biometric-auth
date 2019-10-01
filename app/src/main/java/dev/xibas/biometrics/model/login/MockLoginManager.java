package dev.xibas.biometrics.model.login;

import dev.xibas.biometrics.model.Manager;

public interface  MockLoginManager extends Manager  {

    String getLastKnownUsername();

    void submitLogin(String username, String password, LoginCallback callback);

    interface LoginCallback {

        void loginOperationSuccess();

        void loginOperationFailure();

    }

}
