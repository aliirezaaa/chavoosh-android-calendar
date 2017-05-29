package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.FinishSignInActivityEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.ui.dialogs.LoadingDialog;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.events.PlayerSignedInEvent;


public class SignInAsGustActivity extends BaseActivity {

    private LoadingDialog dialog;
    private boolean isNewPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_as_gust);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isNewPlayer = playerPersistenceService.get() == null;
        signUpAsGuest();

    }

    private void closeLoadingDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void signUpAsGuest() {
        createLoadingDialog();
        createPlayer();
        eventBus.post(new PlayerSignedInEvent("GUEST", true));
        Toast.makeText(this, R.string.using_as_guest, Toast.LENGTH_SHORT).show();
        onFinish();
    }

    private void createPlayer() {
        createPlayer(null, null);
    }

    private void createLoadingDialog() {
        dialog = LoadingDialog.show(this, getString(R.string.sign_in_loading_dialog_title), getString(R.string.sign_in_loading_dialog_message));
    }

    private void createPlayer(String playerId, AuthProvider authProvider) {
        Pet pet = new Pet(Constants.DEFAULT_PET_NAME, Constants.DEFAULT_PET_AVATAR, Constants.DEFAULT_PET_BACKGROUND_IMAGE, Constants.DEFAULT_PET_HP);
        Player player = new Player(String.valueOf(Constants.DEFAULT_PLAYER_XP),
                Constants.DEFAULT_AVATAR_LEVEL,
                Constants.DEFAULT_PLAYER_COINS,
                Constants.DEFAULT_PLAYER_PICTURE,
                DateFormat.is24HourFormat(this), pet);
        if (authProvider != null) {
            player.setCurrentAuthProvider(authProvider);
            player.getAuthProviders().add(authProvider);
        }

        playerPersistenceService.save(player, playerId);
        eventBus.post(new PlayerCreatedEvent(player.getId()));
    }

    private void onFinish() {
        closeLoadingDialog();
        eventBus.post(new FinishSignInActivityEvent(isNewPlayer));
        finish();
    }


}
