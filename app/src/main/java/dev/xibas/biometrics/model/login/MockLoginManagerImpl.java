package dev.xibas.biometrics.model.login;

import android.content.Context;
import android.content.SharedPreferences;

import dev.xibas.biometrics.model.AbstractManager;

public class MockLoginManagerImpl extends AbstractManager implements MockLoginManager {

    private static final String KEY_LAST_KNOWN_USERNAME = "_last_known_username_";

    private static MockLoginManager instance;

    private SharedPreferences usernameStorage;

    public static MockLoginManager getInstance(Context context) {
        if (instance == null) {
            instance = new MockLoginManagerImpl(context);
        }
        return instance;
    }

    private MockLoginManagerImpl(Context context) {
        this.usernameStorage = context.getSharedPreferences
                ("username_shared_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public String getLastKnownUsername() {
        return usernameStorage.getString(KEY_LAST_KNOWN_USERNAME, "");
    }

    @Override
    public void submitLogin(String username, String password, LoginCallback callback) {
        if ("chewbacca".equals(username) && "thewookie".equals(password)) {
            usernameStorage
                    .edit()
                    .putString(KEY_LAST_KNOWN_USERNAME, "chewbacca")
                    .apply();

            callback.loginOperationSuccess();

        } else {
            callback.loginOperationFailure();
        }
    }

}
