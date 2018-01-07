package com.jakub.rockpaperscissorswars;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakub.rockpaperscissorswars.constants.AppConstants;
import com.jakub.rockpaperscissorswars.constants.AttackType;
import com.jakub.rockpaperscissorswars.models.Battle;
import com.jakub.rockpaperscissorswars.models.User;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class GameActivity extends AppCompatActivity {

    @BindView(R.id.player_rock_btn)
    ImageButton playerRockBtn;
    @BindView(R.id.player_paper_btn)
    ImageButton playerPaperBtn;
    @BindView(R.id.player_scissors_btn)
    ImageButton playerScissorsBtn;
    @BindView(R.id.quit_btn)
    ImageButton quitBtn;

    @BindView(R.id.player_rock_val)
    TextView playerRockVal;
    @BindView(R.id.player_paper_val)
    TextView playerPaperVal;
    @BindView(R.id.player_scissors_val)
    TextView playerScissorsVal;
    @BindView(R.id.enemy_rock_val)
    TextView enemyRockVal;
    @BindView(R.id.enemy_paper_val)
    TextView enemyPaperVal;
    @BindView(R.id.enemy_scissors_val)
    TextView enemyScissorsVal;

    @BindView(R.id.player_name_label)
    TextView playerNameLabel;
    @BindView(R.id.enemy_name_label)
    TextView enemyNameLabel;
    @BindView(R.id.player_lvl_label)
    TextView playerLvlLabel;
    @BindView(R.id.enemy_lvl_label)
    TextView enemyLvlLabel;
    @BindView(R.id.player_health_val)
    TextView playerHealthVal;
    @BindView(R.id.enemy_health_val)
    TextView enemyHealthVal;
    @BindView(R.id.player_defence_val)
    TextView playerDefenceVal;
    @BindView(R.id.enemy_defence_val)
    TextView enemyDefenceVal;

    @BindView(R.id.player_battle_field)
    ImageView playerBattleField;
    @BindView(R.id.enemy_battle_field)
    ImageView enemyBattleField;

    @BindView(R.id.player_buttons_layout)
    LinearLayout playerButtonsLayout;
    @BindView(R.id.enemy_buttons_layout)
    LinearLayout enemyButtonsLayout;

    private Battle currentBattle;
    private DatabaseReference battleRef;
    private User playerUser;
    private User enemyUser;
    private boolean isFirstPlayer;
    private boolean movePossible;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        initUsers();
        if (playerUser != null && enemyUser != null) {
            updateFieldsInit();
        }
        setDataListener();
    }

    private void initUsers() {
        currentBattle = Parcels.unwrap(getIntent().getParcelableExtra(AppConstants.BATTLE_PARCEL));
        playerUser = Parcels.unwrap(getIntent().getParcelableExtra(AppConstants.PLAYER_PARCEL));
        isFirstPlayer = playerUser.getUsername().equals(currentBattle.getFirstPlayer().getUsername());
        enemyUser = isFirstPlayer ? currentBattle.getSecondPlayer() : currentBattle.getFirstPlayer();
    }

    //Wywoywane TYLKO na poczatku bitwy
    private void updateFieldsInit() {
        playerNameLabel.setText(playerUser.getUsername());
        playerLvlLabel.setText(String.valueOf(playerUser.getLvl()));
        playerHealthVal.setText(String.valueOf(isFirstPlayer ? currentBattle.getFirstPlayerHp() : currentBattle.getSecondPlayerHp()));
        playerDefenceVal.setText(String.valueOf(playerUser.getDefence()));
        playerRockVal.setText(String.valueOf(playerUser.getRockVal()));
        playerPaperVal.setText(String.valueOf(playerUser.getPaperVal()));
        playerScissorsVal.setText(String.valueOf(playerUser.getScissorsVal()));

        enemyNameLabel.setText(enemyUser.getUsername());
        enemyLvlLabel.setText(String.valueOf(enemyUser.getLvl()));
        enemyHealthVal.setText(String.valueOf(isFirstPlayer ? currentBattle.getSecondPlayerHp() : currentBattle.getFirstPlayerHp()));
        enemyDefenceVal.setText(String.valueOf(enemyUser.getDefence()));
        enemyRockVal.setText(String.valueOf(enemyUser.getRockVal()));
        enemyPaperVal.setText(String.valueOf(enemyUser.getPaperVal()));
        enemyScissorsVal.setText(String.valueOf(enemyUser.getScissorsVal()));
        movePossible = isFirstPlayer;
        toggleButtonsLock(movePossible);
    }
    //Aktualizowanie po kazdym sygnale+99
    private void updateBattleField() {
        AttackType playerAttackType = isFirstPlayer ? currentBattle.getFirstPlayerMove() : currentBattle.getSecondPlayerMove();
        AttackType enemyAttackType = isFirstPlayer ? currentBattle.getSecondPlayerMove() : currentBattle.getFirstPlayerMove();
        if(playerAttackType != null) {
            playerBattleField.setImageDrawable(getDrawable(playerAttackType));
        }
        if(enemyAttackType != null) {
            enemyBattleField.setImageDrawable(getDrawable(enemyAttackType));
        }
    }
    private void toggleButtonsLock(boolean enabled) {
        playerRockBtn.setEnabled(enabled);
        playerPaperBtn.setEnabled(enabled);
        playerScissorsBtn.setEnabled(enabled);
        playerButtonsLayout.setBackgroundColor(enabled ? getResources().getColor(R.color.very_light_gray) : getResources().getColor(R.color.dark_gray));
        enemyButtonsLayout.setBackgroundColor(!enabled ? getResources().getColor(R.color.very_light_gray) : getResources().getColor(R.color.dark_gray));
    }

    private void setDataListener() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(AppConstants.DB_BATTLE);
        battleRef = ref.child(currentBattle.getFirstPlayer().getUsername());
        battleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentBattle = dataSnapshot.getValue(Battle.class);
                if (currentBattle != null) {
                    playerUser = isFirstPlayer ? currentBattle.getFirstPlayer() : currentBattle.getSecondPlayer(); //Potrzebne?
                    enemyUser = isFirstPlayer ? currentBattle.getSecondPlayer() : currentBattle.getFirstPlayer(); //Potrzebne?
                    movePossible = isMovePossible();
                    updateBattleField();
                    toggleButtonsLock(movePossible);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isMovePossible() {
        return (isFirstPlayer && currentBattle.getFirstPlayerMove() == null) || (!isFirstPlayer && currentBattle.getSecondPlayerMove() == null);
    }

    @OnClick(R.id.player_rock_btn)
    public void onRockBtnClick() {
        if (isFirstPlayer) {
            currentBattle.setFirstPlayerMove(AttackType.ROCK);
        } else {
            currentBattle.setSecondPlayerMove(AttackType.ROCK);
        }
        battleRef.setValue(currentBattle);
        toggleButtonsLock(false);
    }

    @OnClick(R.id.player_paper_btn)
    public void onPaperBtnClick() {
        if (isFirstPlayer) {
            currentBattle.setFirstPlayerMove(AttackType.PAPER);
        } else {
            currentBattle.setSecondPlayerMove(AttackType.PAPER);
        }
        battleRef.setValue(currentBattle);
        toggleButtonsLock(false);
    }

    @OnClick(R.id.player_scissors_btn)
    public void onScissorsBtnClick() {
        if (isFirstPlayer) {
            currentBattle.setFirstPlayerMove(AttackType.SCISSORS);
        } else {
            currentBattle.setSecondPlayerMove(AttackType.SCISSORS);
        }
        battleRef.setValue(currentBattle);
        toggleButtonsLock(false);
    }

    private Drawable getDrawable(AttackType type) {
        switch (type) {
            case ROCK:
                return getDrawable(R.drawable.rock);
            case PAPER:
                return getDrawable(R.drawable.paper);
            case SCISSORS:
                return getDrawable(R.drawable.scissors);
            default:
                return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        battleRef.removeValue();
    }
}
