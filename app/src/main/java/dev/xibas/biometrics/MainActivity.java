package dev.xibas.biometrics;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.xibas.biometrics.presenter.login.LoginPresenter;
import dev.xibas.biometrics.presenter.login.LoginPresenterImpl;
import dev.xibas.biometrics.presenter.setttings.SettingsPresenter;
import dev.xibas.biometrics.presenter.setttings.SettingsPresenterImpl;
import dev.xibas.biometrics.view.LoginView;
import dev.xibas.biometrics.view.SettingsView;

public class MainActivity extends AppCompatActivity implements SettingsView, LoginView {

    @BindView(R.id.switch_biometric)
    Switch switchBiometric;

    @BindView(R.id.input_username)
    EditText inputUsername;

    @BindView(R.id.input_password)
    EditText inputPassword;

    @BindView(R.id.button_login)
    Button buttonLogin;

    private AlertDialog biometricAuthDialog;

    private SettingsPresenter settingsPresenter;
    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        settingsPresenter = new SettingsPresenterImpl(getApplicationContext());
        loginPresenter = new LoginPresenterImpl(getApplicationContext(), this,
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.prompt_biometric_auth_title))
                        .setDescription(getString(R.string.prompt_biometric_auth_new_user_message))
                        .setNegativeButtonText(getString(R.string.dialog_button_cancel))
                        .build(),
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.prompt_biometric_auth_title))
                        .setDescription(getString(R.string.prompt_biometric_auth_recurring_message))
                        .setNegativeButtonText(getString(R.string.dialog_button_cancel))
                        .build());

        switchBiometric.setOnClickListener(view -> {
            boolean enabled = switchBiometric.isChecked();

            settingsPresenter.onBiometricAuthToggleChecked(enabled);
        });

        buttonLogin.setOnClickListener(view -> {
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();

            loginPresenter.onSubmitLoginClicked(username, password);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        settingsPresenter.attachView(this);
        loginPresenter.attachView(this);
    }

    @Override
    public void onStop() {
        settingsPresenter.detachView(this);
        loginPresenter.detachView(this);
        super.onStop();
    }

    @Override
    public void updateBiometricAuthToggle(boolean enabled, boolean supported) {
        switchBiometric.setChecked(enabled);
        switchBiometric.setAlpha(supported ? 1.0f : 0.3f);
    }

    @Override
    public void showBiometricErrorNoHardware() {
        this.closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_biometric_auth_error_title)
                .setMessage(R.string.dialog_biometric_auth_no_hardware)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(DialogInterface::dismiss);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void showBiometricErrorHardwareUnavailable() {
        this.closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_biometric_auth_error_title)
                .setMessage(R.string.dialog_biometric_auth_hardware_unavailable)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(DialogInterface::dismiss);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void showBiometricAuthActivationDialog() {
        this.closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.prompt_biometric_auth_title)
                .setMessage(R.string.dialog_biometric_auth_user_not_enrolled)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) ->
                        settingsPresenter.onBiometricAuthActivationDialogGoToSecuritySettingsClicked())

                .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) ->
                        settingsPresenter.onBiometricAuthActivationDialogCloseClicked())

                .setOnCancelListener((dialog) ->
                        settingsPresenter.onBiometricAuthActivationDialogCloseClicked());

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void openSecuritySettings() {
        final String intentAction;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            intentAction = Settings.ACTION_FINGERPRINT_ENROLL;
        } else {
            intentAction = Settings.ACTION_SECURITY_SETTINGS;
        }

        startActivity(new Intent(intentAction));
    }

    @Override
    public void fillUsername(String username) {
        inputUsername.setText(username);
    }

    @Override
    public void showBiometricAuthDialog() {
        closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_biometric_auth_enable_title)
                .setMessage(R.string.dialog_biometric_auth_enable_message)

                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) ->
                        loginPresenter.onUseBiometricAuthClicked())

                .setNegativeButton(R.string.dialog_button_disable, (dialog, which) ->
                        loginPresenter.onDisableBiometricAuthClicked())

                .setNeutralButton(R.string.dialog_button_cancel, (dialog, which) ->
                        loginPresenter.onDoNotUseBiometricAuthClicked())

                .setOnCancelListener((dialog) ->
                        loginPresenter.onDoNotUseBiometricAuthClicked());

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void showBiometricAuthEncryptionFailure() {
        closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_biometric_auth_error_title)
                .setMessage(R.string.dialog_biometric_auth_encryption_failure)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(DialogInterface::dismiss);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void showBiometricAuthEncryptionError() {
        closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_biometric_auth_error_title)
                .setMessage(R.string.dialog_biometric_auth_encryption_error)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(DialogInterface::dismiss);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void showBiometricAuthDecryptionFailure() {
        closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_biometric_auth_error_title)
                .setMessage(R.string.dialog_biometric_auth_decryption_failure)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(DialogInterface::dismiss);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void showBiometricAuthDecryptionError() {
        closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_biometric_auth_error_title)
                .setMessage(R.string.dialog_biometric_auth_decryption_error)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(DialogInterface::dismiss);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void closeBiometricAuthDialog() {
        if (biometricAuthDialog != null) {
            biometricAuthDialog.hide();
            biometricAuthDialog = null;
        }
    }

    @Override
    public void showLoginFailed() {
        closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_login_failure_title)
                .setMessage(R.string.dialog_login_failure_message)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setCancelable(false);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

    @Override
    public void openPrivateArea() {
        closeBiometricAuthDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_login_success_title)
                .setMessage(R.string.dialog_login_success_message)
                .setPositiveButton(R.string.dialog_button_confirm, (dialog, which) -> dialog.dismiss())
                .setCancelable(false);

        biometricAuthDialog = builder.create();
        biometricAuthDialog.show();
    }

}
