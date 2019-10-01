package dev.xibas.biometrics.model.login;

import dev.xibas.biometrics.model.proto.AbstractManager;

public class MockLoginManagerImpl extends AbstractManager implements MockLoginManager {

    private String lastKnownUsername = "";

    @Override
    public String getLastKnownUsername() {
        return lastKnownUsername;
    }

    @Override
    public void submitLogin(String username, String password, LoginCallback callback) {
        if ("chewbacca".equals(username) && "thewookie".equals(password)) {
            lastKnownUsername = "chewbacca";
            callback.loginOperationSuccess();

        } else {
            callback.loginOperationFailure();
        }
    }

}
