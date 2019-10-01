package dev.xibas.biometrics.model.util;

import dev.xibas.biometrics.model.BiometricAuthManager;

public abstract class BiometricAuthCompletionCallback implements BiometricAuthManager.BiometricAuthCallback {
    public abstract void onAuthCompletion();

    @Override
    public void onAuthSuccess(String username, String password) {
        onAuthCompletion();
    }

    @Override
    public void onAuthFailure() {
        onAuthCompletion();
    }

    @Override
    public void onAuthError(BiometricAuthManager.BiometricAuthError error) {
        onAuthCompletion();
    }

    @Override
    public void onAuthCancel() {
        onAuthCompletion();
    }
}
