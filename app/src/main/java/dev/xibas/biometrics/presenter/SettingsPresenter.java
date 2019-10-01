package dev.xibas.biometrics.presenter;

import android.content.Context;

import dev.xibas.biometrics.model.biometric.BiometricAuthManager;
import dev.xibas.biometrics.model.biometric.BiometricAuthManagerImpl;
import dev.xibas.biometrics.model.Manager;
import dev.xibas.biometrics.presenter.proto.AbstractPresenter;
import dev.xibas.biometrics.view.SettingsView;

import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;

public class SettingsPresenter extends AbstractPresenter<SettingsView> implements Manager.ManagerListener {

    private final BiometricAuthManager biometricAuthManager;

    public SettingsPresenter(Context context) {
        this.biometricAuthManager = BiometricAuthManagerImpl.getInstance(context);
    }

    @Override
    protected void onViewAttached(SettingsView view) {
        this.biometricAuthManager.registerListener(this);
    }

    @Override
    protected void onViewDetached(SettingsView view) {
        this.biometricAuthManager.unregisterListener(this);
    }

    @Override
    public void onManagerUpdate(Manager manager) {
        SettingsView view = getView();
        if (view == null) {
            return;
        }

        biometricAuthManager.getBiometricAuthStatus(view::updateBiometricAuthToggle);
    }

    public void onBiometricAuthToggleChecked(boolean checked) {
        biometricAuthManager.setBiometricAuthEnabled(checked, (enabled, supported) -> {
            SettingsView view = getView();
            if (view == null) {
                return;
            }

            view.updateBiometricAuthToggle(enabled, supported);

            if (!supported) {
                switch (biometricAuthManager.getBiometricAuthSupportFlag()) {
                    default:
                    case BIOMETRIC_ERROR_NO_HARDWARE:
                        view.showBiometricErrorNoHardware();
                        break;
                    case BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        view.showBiometricErrorHardwareUnavailable();
                        break;
                    case BIOMETRIC_ERROR_NONE_ENROLLED:
                        view.showBiometricAuthActivationDialog();
                        break;
                }
            }
        });
    }

    public void onBiometricAuthActivationDialogGoToSecuritySettingsClicked() {
        SettingsView view = getView();
        if (view == null) {
            return;
        }

        view.closeBiometricAuthActivationDialog();
        view.openSecuritySettings();
    }

    public void onBiometricAuthActivationDialogCloseClicked() {
        SettingsView view = getView();
        if (view == null) {
            return;
        }

        view.closeBiometricAuthActivationDialog();
    }

}
