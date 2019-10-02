package dev.xibas.biometrics.presenter.login;

import android.content.Context;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import dev.xibas.biometrics.model.Manager;
import dev.xibas.biometrics.model.biometric.BiometricAuthManager;
import dev.xibas.biometrics.model.biometric.BiometricAuthManagerImpl;
import dev.xibas.biometrics.model.login.MockLoginManager;
import dev.xibas.biometrics.model.login.MockLoginManagerImpl;
import dev.xibas.biometrics.presenter.AbstractPresenter;
import dev.xibas.biometrics.view.LoginView;

import static dev.xibas.biometrics.model.biometric.BiometricAuthManager.BiometricAuthError.BIOMETRIC_AUTH_DATA_EXPIRED;

public class LoginPresenterImpl extends AbstractPresenter<LoginView> implements LoginPresenter {

    private final FragmentActivity fragmentActivity;
    private final BiometricPrompt.PromptInfo biometricPromptInfoNewUserFlow;
    private final BiometricPrompt.PromptInfo biometricPromptInfoRecurringFlow;

    private final MockLoginManager mockLoginManager;
    private final BiometricAuthManager biometricAuthManager;
    private BiometricAuthManager.BiometricAuthStatusListener biometricAuthListener;

    private boolean biometricAuthAttemptDisabled = false;

    private String username;
    private String password;

    public LoginPresenterImpl(Context context, FragmentActivity fragmentActivity,
                              BiometricPrompt.PromptInfo biometricPromptInfoNewUserFlow,
                              BiometricPrompt.PromptInfo biometricPromptInfoRecurringFlow) {

        this.mockLoginManager = MockLoginManagerImpl.getInstance(context);
        this.biometricAuthManager = BiometricAuthManagerImpl.getInstance(context);

        this.fragmentActivity = fragmentActivity;
        this.biometricPromptInfoNewUserFlow = biometricPromptInfoNewUserFlow;
        this.biometricPromptInfoRecurringFlow = biometricPromptInfoRecurringFlow;
    }

    @Override
    protected void onViewAttached(LoginView view) {
        this.mockLoginManager.registerListener(this);
    }

    @Override
    protected void onViewDetached(LoginView view) {
        this.mockLoginManager.unregisterListener(this);
    }

    @Override
    public void onManagerUpdate(Manager manager) {
        if (manager == mockLoginManager) {
            username = mockLoginManager.getLastKnownUsername();

            LoginView view = getView();
            if (view != null) {
                view.fillUsername(username);
            }

            this.biometricAuthListener = new RecurringPlayerAuthListener();
            this.biometricAuthManager.registerListener(this);

        } else if (manager == biometricAuthManager) {
            biometricAuthManager.getBiometricAuthStatus(biometricAuthListener);
        }
    }

    @Override
    public void onSubmitLoginClicked(String username, String password) {
        this.username = username;
        this.password = password;

        submitToMockLoginManager(username, password);
    }

    private void submitToMockLoginManager(String username, String password) {
        mockLoginManager.submitLogin(username, password, new MockLoginManager.LoginCallback() {
            @Override
            public void loginOperationSuccess() {
                onLoginSuccessful();
            }

            @Override
            public void loginOperationFailure() {

                LoginView view = getView();
                if (view != null) {
                    view.showLoginFailed();
                }
            }
        });
    }

    private void onLoginSuccessful() {
        if (biometricAuthAttemptDisabled) {
            routeToPrivateArea();

        } else {
            biometricAuthListener = new NewPlayerAuthListener();
            biometricAuthManager.registerListener(this);
        }
    }

    @Override
    public void onUseBiometricAuthClicked() {
        final LoginView view = getView();
        if (view != null) {
            view.closeBiometricAuthDialog();
        }

        biometricAuthManager.encryptUserPasswordWithBiometricAuth
                (username, password, fragmentActivity, biometricPromptInfoNewUserFlow,
                        new BiometricAuthManager.BiometricAuthCallback() {
                            @Override
                            public void onAuthSuccess(String username, String password) {
                                unregisterBiometricManagerAndRouteToLobby();
                            }

                            @Override
                            public void onAuthFailure() {
                                LoginView view = getView();
                                if (view != null) {
                                    view.showBiometricAuthEncryptionFailure();
                                }
                            }

                            @Override
                            public void onAuthError(BiometricAuthManager.BiometricAuthError error) {
                                LoginView view = getView();
                                if (view != null) {
                                    view.showBiometricAuthEncryptionError();
                                }
                            }

                            @Override
                            public void onAuthCancel() {
                                unregisterBiometricManagerAndRouteToLobby();
                            }
                        });
    }

    @Override
    public void onDoNotUseBiometricAuthClicked() {
        LoginView view = getView();
        if (view != null) {
            view.closeBiometricAuthDialog();
        }

        unregisterBiometricManagerAndRouteToLobby();
    }

    @Override
    public void onDisableBiometricAuthClicked() {
        final LoginView view = getView();
        if (view != null) {
            view.closeBiometricAuthDialog();
        }

        biometricAuthManager.setBiometricAuthEnabled(false, (enabled, supported) ->
                unregisterBiometricManagerAndRouteToLobby());
    }

    private void unregisterBiometricManagerAndRouteToLobby() {
        biometricAuthManager.unregisterListener(LoginPresenterImpl.this);
        biometricAuthAttemptDisabled = true;

        routeToPrivateArea();
    }

    private void routeToPrivateArea() {
        LoginView view = getView();
        if (view != null) {
            view.openPrivateArea();
        }
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
                biometricAuthManager.unregisterListener(LoginPresenterImpl.this);
                biometricAuthAttemptDisabled = true;

                LoginView view = getView();
                if (view != null) {
                    routeToPrivateArea();
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
                                        biometricAuthManager.unregisterListener(LoginPresenterImpl.this);
                                        biometricAuthAttemptDisabled = true;

                                        submitToMockLoginManager(username, password);
                                    }

                                    @Override
                                    public void onAuthFailure() {
                                        biometricAuthManager.unregisterListener(LoginPresenterImpl.this);
                                        biometricAuthAttemptDisabled = true;

                                        LoginView view = getView();
                                        if (view != null) {
                                            view.showBiometricAuthDecryptionFailure();
                                        }
                                    }

                                    @Override
                                    public void onAuthError(BiometricAuthManager.BiometricAuthError error) {
                                        biometricAuthManager.unregisterListener(LoginPresenterImpl.this);

                                        if (error != BIOMETRIC_AUTH_DATA_EXPIRED) {
                                            biometricAuthAttemptDisabled = true;

                                            LoginView view = getView();
                                            if (view != null) {
                                                view.showBiometricAuthDecryptionError();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onAuthCancel() {
                                        biometricAuthManager.unregisterListener(LoginPresenterImpl.this);
                                        biometricAuthAttemptDisabled = true;
                                    }
                                });

            } else {
                biometricAuthManager.unregisterListener(LoginPresenterImpl.this);
            }
        }
    }

}
