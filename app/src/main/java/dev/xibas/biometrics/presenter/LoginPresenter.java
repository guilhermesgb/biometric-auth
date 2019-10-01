package dev.xibas.biometrics.presenter;

import android.content.Context;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import dev.xibas.biometrics.model.BiometricAuthManager;
import dev.xibas.biometrics.model.BiometricAuthManagerImpl;
import dev.xibas.biometrics.model.Manager;
import dev.xibas.biometrics.model.util.BiometricAuthCompletionCallback;
import dev.xibas.biometrics.presenter.proto.AbstractPresenter;
import dev.xibas.biometrics.view.LoginView;

import static dev.xibas.biometrics.model.BiometricAuthManager.BiometricAuthError.BIOMETRIC_AUTH_DATA_EXPIRED;

public class LoginPresenter extends AbstractPresenter<LoginView> implements Manager.ManagerListener {

    private final FragmentActivity fragmentActivity;
    private final BiometricPrompt.PromptInfo biometricPromptInfoNewUserFlow;
    private final BiometricPrompt.PromptInfo biometricPromptInfoRecurringFlow;

    private final BiometricAuthManager biometricAuthManager;
    private BiometricAuthManager.BiometricAuthStatusListener biometricAuthListener;

    private boolean biometricAuthAttemptDisabled = false;

    private String username;
    private String password;

    public LoginPresenter(Context context, FragmentActivity fragmentActivity,
                         BiometricPrompt.PromptInfo biometricPromptInfoNewUserFlow,
                         BiometricPrompt.PromptInfo biometricPromptInfoRecurringFlow) {

        this.biometricAuthManager = BiometricAuthManagerImpl.getInstance(context);

        this.fragmentActivity = fragmentActivity;
        this.biometricPromptInfoNewUserFlow = biometricPromptInfoNewUserFlow;
        this.biometricPromptInfoRecurringFlow = biometricPromptInfoRecurringFlow;

        this.biometricAuthListener = new RecurringPlayerAuthListener();
        this.biometricAuthManager.registerListener(this);
    }

    @Override
    protected void onViewAttached(LoginView view) {
        this.biometricAuthManager.registerListener(this);
    }

    @Override
    protected void onViewDetached(LoginView view) {
        this.biometricAuthManager.unregisterListener(this);
    }

    @Override
    public void onManagerUpdate(Manager manager) {
        biometricAuthManager.getBiometricAuthStatus(biometricAuthListener);
    }

    private void onLoginSuccessful() {
        if (biometricAuthAttemptDisabled) {
            LoginView view = getView();
            if (view != null) {
                return;
            }

            routeToPrivateArea(view);

        } else {
            biometricAuthListener = new NewPlayerAuthListener();
            biometricAuthManager.registerListener(this);
        }
    }

    public void onUseBiometricAuthClicked() {
        final LoginView view = getView();
        if (view != null) {
            view.closeBiometricAuthDialog();
        }

        biometricAuthManager.encryptUserPasswordWithBiometricAuth
                (username, password, fragmentActivity, biometricPromptInfoNewUserFlow,
                        new BiometricAuthCompletionCallback() {
                            @Override
                            public void onAuthCompletion() {
                                unregisterBiometricManagerAndRouteToLobby();
                            }
                        });
    }

    public void onDoNotUseBiometricAuthClicked() {
        LoginView view = getView();
        if (view != null) {
            view.closeBiometricAuthDialog();
        }

        unregisterBiometricManagerAndRouteToLobby();
    }

    public void onDisableBiometricAuthClicked() {
        final LoginView view = getView();
        if (view != null) {
            view.closeBiometricAuthDialog();
        }

        biometricAuthManager.setBiometricAuthEnabled(false, (enabled, supported) ->
                unregisterBiometricManagerAndRouteToLobby());
    }

    private void unregisterBiometricManagerAndRouteToLobby() {
        biometricAuthManager.unregisterListener(LoginPresenter.this);
        biometricAuthAttemptDisabled = true;

        LoginView view = getView();
        if (view != null) {
            routeToPrivateArea(view);
        }
    }

    private void routeToPrivateArea(LoginView view) {

    }

    class NewPlayerAuthListener implements BiometricAuthManager.BiometricAuthStatusListener {
        @Override
        public void onBiometricAuthStatusUpdated(boolean enabled, boolean supported) {
            if (enabled && supported) {
                LoginView view = getView();
                if (view != null) {
                    view.showBiometricAuthDialog();
                }

            } else {
                biometricAuthManager.unregisterListener(LoginPresenter.this);
                biometricAuthAttemptDisabled = true;

                LoginView view = getView();
                if (view != null) {
                    routeToPrivateArea(view);
                }
            }
        }
    }

    class RecurringPlayerAuthListener implements BiometricAuthManager.BiometricAuthStatusListener {
        @Override
        public void onBiometricAuthStatusUpdated(boolean enabled, boolean supported) {
            if (enabled && supported && !biometricAuthAttemptDisabled && biometricAuthManager
                    .userHasBiometricAuthEncryptedPassword(username)) {

                biometricAuthManager.decryptUserPasswordWithBiometricAuth
                        (username, fragmentActivity, biometricPromptInfoRecurringFlow,
                                new BiometricAuthManager.BiometricAuthCallback() {
                                    @Override
                                    public void onAuthSuccess(String username, String password) {
                                        biometricAuthManager.unregisterListener(LoginPresenter.this);
                                        biometricAuthAttemptDisabled = true;

                                        //SKIP LOGIN HERE USING (username, password)
                                    }

                                    @Override
                                    public void onAuthFailure() {
                                        biometricAuthManager.unregisterListener(LoginPresenter.this);
                                        biometricAuthAttemptDisabled = true;

                                        LoginView view = getView();
                                        if (view != null) {
                                            view.showBiometricAuthFailure();
                                        }
                                    }

                                    @Override
                                    public void onAuthError(BiometricAuthManager.BiometricAuthError error) {
                                        biometricAuthManager.unregisterListener(LoginPresenter.this);

                                        if (error != BIOMETRIC_AUTH_DATA_EXPIRED) {
                                            biometricAuthAttemptDisabled = true;

                                            LoginView view = getView();
                                            if (view != null) {
                                                view.showBiometricAuthError();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onAuthCancel() {
                                        biometricAuthManager.unregisterListener(LoginPresenter.this);
                                        biometricAuthAttemptDisabled = true;
                                    }
                                });

            } else {
                biometricAuthManager.unregisterListener(LoginPresenter.this);
            }
        }
    }

}
