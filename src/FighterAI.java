import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;
import struct.MotionData;

import java.util.ArrayList;
import java.util.Random;

public class FighterAI implements AIInterface {

    Key inputKey;
    Random random;
    FrameData frameData;
    CommandCenter commandCenter;
    private boolean player;
    private CharacterData myCharacter;
    private CharacterData oppCharacter;
    private GameData gameData;
    private ArrayList<MotionData> myMotion;
    private Action[] actionOnAir;
    private Action[] actionOnGround;
    private Action[] actionSp;
    ArrayList<String> result;

    public void close() {
    }

    public void getInformation(final FrameData frameData, final boolean arg1) {
        this.frameData = frameData;
        this.commandCenter.setFrameData(frameData, this.player);
        this.myCharacter = frameData.getCharacter(this.player);
        this.oppCharacter = frameData.getCharacter(!this.player);
    }

    public int initialize(final GameData gameData, final boolean player) {
        this.inputKey = new Key();
        this.random = new Random();
        this.frameData = new FrameData();
        this.player = player;
        this.gameData = gameData;
        this.commandCenter = new CommandCenter();
        this.myMotion = (ArrayList<MotionData>)gameData.getMotionData(player);

        // Base – Actions that are used in the normal status.
        // STAND | CROUCH | AIR
        // Move – Actions that can change the character’s position.
        // FORWARD_WALK | DASH | BACK_STEP | JUMP | FOR_JUMP | BACK_JUMP
        // Guard – Actions that the character uses for protecting itself and reducing damage from the opponent.
        // STAND_GUARD | CROUCH_GUARD | AIR_GUARD
        // Recovery – Actions that arise when the character is hit or performs a landing.
        // STAND_GUARD_RECOV | CROUCH_GUARD_RECOV | AIR_GUARD_RECOV | STAND_RECOV
        // CROUCH_RECOV | AIR_RECOV | CHANGE_DOWN | DOWN
        // RISE | LANDING | THROW_HIT | THROW_SUFFER
        // Skill – Actions that can generate attack objects such as a projectile (fire ball). These skills have 3 parts from start to the end: Startup, Active and Recovery.
        // THROW_A | THROW_B | STAND_A | STAND_B
        // CROUCH_A | CROUCH_B | AIR_A | AIR_B
        // AIR_DA | AIR_DB | STAND_FA | STAND_FB
        // CROUCH_FA | CROUCH_FB | AIR_FA | AIR_FB
        // AIR_UA | AIR_UA | AIR_UB | STAND_D_DF_FA
        // STAND_D_DF_FB | STAND_F_D_DFA | STAND_F_D_DFB | STAND_D_DB_BA
        // STAND_D_DB_BB | AIR_D_DF_FA | AIR_D_DF_FB | AIR_F_D_DFA
        // AIR_F_D_DFB | AIR_D_DB_BA | AIR_D_DB_BB | STAND_D_DF_FC
        // COMMAND http://www.ice.ci.ritsumei.ac.jp/~ftgaic/Downloadfiles/Information%20about%20the%20character.pdf

        // You can put command here
        this.actionOnAir = new Action[] { Action.AIR_GUARD, Action.AIR_A, Action.AIR_B /*, Action.BLA_BLA*/ };
        this.actionOnGround = new Action[] { Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK /*, Action.BLA_BLA*/ };
        this.actionSp = new Action[] { Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA /*, Action.BLA_BLA*/ };

        return 0;
    }

    public Key input() {
        return this.inputKey;
    }

    // You can modify these command too
    public void processing() {
        if (!this.frameData.getEmptyFlag()) {
            if (this.commandCenter.getSkillFlag() && this.frameData.getRemainingFramesNumber() > 0) {
                this.inputKey = this.commandCenter.getSkillKey();
            } else {
                this.inputKey.empty();
                this.commandCenter.skillCancel();
                String nameAction = "STAND";
                final ArrayList<Action> availableAction = new ArrayList<Action>();
                final int dist = this.frameData.getDistanceX();
                if (dist > 200) {
                    availableAction.add(Action.DASH);
                    availableAction.add(Action.FOR_JUMP);
                } else if (dist <= 1) {
                    availableAction.add(Action.BACK_STEP);
                    availableAction.add(Action.STAND_GUARD);
                } else {
                    final CharacterData myCharacter = this.frameData.getCharacter(this.player);
                    final CharacterData oppCharacter = this.frameData.getCharacter(!this.player);
                    final boolean onAir = myCharacter.getState() == State.AIR;
                    final boolean oppOnGround = oppCharacter.getState() == State.STAND;
                    if (onAir) {
                        Action[] actionOnAir;
                        for (int length = (actionOnAir = this.actionOnAir).length, i = 0; i < length; ++i) {
                            final Action action = actionOnAir[i];
                            if (Math.abs(this.myMotion.get(Action.valueOf(action.name()).ordinal()).getAttackStartAddEnergy()) <= myCharacter.getEnergy()) {
                                availableAction.add(action);
                            }
                        }
                    } else if (oppOnGround && dist > 60) {
                        Action[] actionSp;
                        for (int length2 = (actionSp = this.actionSp).length, j = 0; j < length2; ++j) {
                            final Action action = actionSp[j];
                            if (Math.abs(this.myMotion.get(Action.valueOf(action.name()).ordinal()).getAttackStartAddEnergy()) <= myCharacter.getEnergy()) {
                                availableAction.add(action);
                            }
                        }
                    } else {
                        Action[] actionOnGround;
                        for (int length3 = (actionOnGround = this.actionOnGround).length, k = 0; k < length3; ++k) {
                            final Action action = actionOnGround[k];
                            if (Math.abs(this.myMotion.get(Action.valueOf(action.name()).ordinal()).getAttackStartAddEnergy()) <= myCharacter.getEnergy()) {
                                availableAction.add(action);
                            }
                        }
                    }
                }
                final Random rnd = new Random();
                final int count = rnd.nextInt(availableAction.size());
                nameAction = availableAction.get(count).name();
                System.err.println("Dist: " + nameAction);
                this.commandCenter.commandCall(nameAction);
            }
        }
    }

    public void roundEnd(final int arg0, final int arg1, final int arg2) { }
}
