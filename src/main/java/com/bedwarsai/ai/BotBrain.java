package com.bedwarsai.ai;

import com.bedwarsai.bot.Difficulty;
import com.bedwarsai.bot.BotPlayer;
import com.bedwarsai.bot.Personality;
import com.bedwarsai.build.BridgeBuilder;
import com.bedwarsai.core.BedWarsAI;
import com.bedwarsai.core.BwaiConfig;
import com.bedwarsai.inventory.BotInventory;
import com.bedwarsai.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;

import java.util.Random;

/** Maquina de estados do bot: IDLE, CHASE, FIGHT, EAT, BRIDGE. */
public class BotBrain {

    public enum State { IDLE, CHASE, FIGHT, EAT, BRIDGE }

    public final EntityPlayerMP bot;
    public Difficulty difficulty;
    public Personality personality;

    private final Random rnd = new Random();
    private final BotMotor motor;
    private final BridgeBuilder bridge;

    private State state = State.IDLE;
    private EntityPlayer target;
    private int targetTicks;
    private float attackAccum;
    private int strafeDir = 1;
    private int strafeTimer;
    private int bridgeCooldown;
    private int eatCooldown;
    private int errorCount;
    private boolean disabled;

    public BotBrain(BotPlayer bot, Difficulty difficulty, Personality personality) {
        this.bot = bot;
        this.difficulty = difficulty;
        this.personality = personality;
        this.motor = new BotMotor(bot, rnd);
        this.bridge = new BridgeBuilder(bot, motor);
    }

    public State getState() { return state; }

    public String targetName() {
        return target == null ? "-" : target.getName();
    }

    /** Chamado uma vez por tick do servidor. Nunca lanca excecao. */
    public void tick() {
        if (disabled) {
            motor.reset();
            motor.apply();
            return;
        }
        try {
            think();
        } catch (Throwable t) {
            errorCount++;
            BedWarsAI.log.error("Erro na IA do bot " + bot.getName() + " (" + errorCount + "/5)", t);
            motor.reset();
            motor.apply();
            if (errorCount >= 5) {
                disabled = true;
                BedWarsAI.log.error("IA do bot " + bot.getName() + " desativada apos erros repetidos.");
            }
        }
    }

    private void think() {
        motor.reset();
        if (bridgeCooldown > 0) bridgeCooldown--;
        if (eatCooldown > 0) eatCooldown--;

        updateTarget();
        if (target == null) {
            state = State.IDLE;
            bridge.cancel();
            motor.apply();
            return;
        }

        double reach = WorldUtil.eyeDistanceToBox(bot, target);
        double dx = target.posX - bot.posX;
        double dz = target.posZ - bot.posZ;
        double flat = Math.sqrt(dx * dx + dz * dz);

        // 1. Comer maca dourada
        if (bot.isUsingItem()) {
            state = State.EAT;
            motor.lookAt(target.posX, target.posY + target.getEyeHeight(), target.posZ,
                    difficulty.turnSpeed, difficulty.aimError);
            motor.apply();
            return;
        }
        if (shouldEat(reach)) {
            startEating();
            if (bot.isUsingItem()) {
                state = State.EAT;
                motor.apply();
                return;
            }
        }

        // 2. Construindo ponte
        if (state == State.BRIDGE) {
            if (flat <= 3.5) {
                bridge.cancel();
                state = State.CHASE;
            } else {
                BridgeBuilder.Result r = bridge.tick();
                if (r != BridgeBuilder.Result.RUNNING) {
                    state = State.CHASE;
                    bridgeCooldown = 30;
                }
                motor.apply();
                return;
            }
        }

        // 3. Perseguir ou lutar
        float yawErr = motor.lookAt(target.posX, target.posY + target.getEyeHeight(), target.posZ,
                difficulty.turnSpeed, difficulty.aimError);
        if (reach <= 4.0) {
            state = State.FIGHT;
            fight(reach, yawErr);
        } else {
            state = State.CHASE;
            chase(dx, dz);
        }
        motor.apply();
    }

    private void updateTarget() {
        EntityPlayer prev = target;
        double range = personality.aggroRange;
        EntityPlayer best = null;
        double bestD = Double.MAX_VALUE;

        for (EntityPlayer p : bot.worldObj.playerEntities) {
            if (!validTarget(p)) continue;
            double d = bot.getDistanceToEntity(p);
            double limit = (p == prev) ? range * 1.3 : range;
            if (d > limit) continue;
            if (p == prev) d *= 0.5; // histerese: prefere manter o alvo atual
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        target = best;
        if (target != prev) targetTicks = 0; else targetTicks++;
    }

    private boolean validTarget(EntityPlayer p) {
        if (p == bot || p.isDead || p.getHealth() <= 0f) return false;
        if (p.capabilities.isCreativeMode || p.isSpectator()) return false;
        if (p instanceof BotPlayer && !BwaiConfig.botsFightBots) return false;
        return true;
    }

    private boolean shouldEat(double reach) {
        if (eatCooldown > 0 || bot.isPotionActive(Potion.absorption)) return false;
        if (BotInventory.findGoldenApple(bot) < 0) return false;
        float threshold = difficulty.eatBelowHealth + personality.eatBonus;
        return bot.getHealth() <= threshold && (reach > 4.5 || bot.getHealth() <= 5f);
    }

    private void startEating() {
        int slot = BotInventory.findGoldenApple(bot);
        if (slot < 0) return;
        BotInventory.select(bot, slot);
        ItemStack st = bot.inventory.getCurrentItem();
        if (st != null) {
            bot.setItemInUse(st, st.getMaxItemUseDuration());
            eatCooldown = 60;
        }
    }

    private void fight(double reach, float yawErr) {
        int sword = BotInventory.findSword(bot);
        if (sword >= 0) BotInventory.select(bot, sword);

        attackAccum = Math.min(attackAccum + difficulty.cps / 20f, 2f);
        boolean ready = targetTicks >= difficulty.reactionTicks;
        if (ready && attackAccum >= 1f && yawErr < 25f) {
            attackAccum -= 1f;
            bot.swingItem();
            // 1.8.9: dano so entra se o alvo saiu do tempo de invulnerabilidade
            if (reach <= 3.0 && target.hurtResistantTime <= 10) {
                bot.attackTargetEntityWithCurrentItem(target);
            }
        }

        motor.forward = reach > 2.0 ? 1f : 0f;
        motor.sprint = motor.forward > 0f;

        if (difficulty.strafes) {
            if (--strafeTimer <= 0) {
                strafeDir = rnd.nextBoolean() ? 1 : -1;
                strafeTimer = 8 + rnd.nextInt(15);
            }
            motor.strafe = 0.8f * strafeDir;
        }

        if (!safe(motor.forward, motor.strafe)) {
            motor.forward = 0f;
            motor.strafe = 0f;
        }
        if (bot.isCollidedHorizontally && bot.onGround) motor.jump = true;
    }

    private void chase(double dx, double dz) {
        motor.forward = 1f;
        motor.sprint = true;
        if (bot.isCollidedHorizontally && bot.onGround) motor.jump = true;

        if (!safe(1f, 0f)) {
            motor.forward = 0f;
            motor.sprint = false;
            if (bridgeCooldown == 0 && bot.onGround && BotInventory.findBlockSlot(bot) >= 0) {
                EnumFacing dir = WorldUtil.dominantDirection(dx, dz);
                bridge.start(dir);
                state = State.BRIDGE;
            }
        }
    }

    /** Ha chao seguro ~1.3 blocos a frente na direcao do movimento? */
    private boolean safe(float fwd, float str) {
        double yaw = Math.toRadians(bot.rotationYaw);
        double mx = -Math.sin(yaw) * fwd + Math.cos(yaw) * str;
        double mz = Math.cos(yaw) * fwd + Math.sin(yaw) * str;
        double len = Math.sqrt(mx * mx + mz * mz);
        if (len < 1e-4) return true;
        mx /= len;
        mz /= len;
        return WorldUtil.groundWithin(bot.worldObj, bot.posX + mx * 1.3, bot.posY, bot.posZ + mz * 1.3, 4);
    }

    public String describe() {
        return bot.getName() + " [" + difficulty + "/" + personality + "] estado=" + state
                + " alvo=" + targetName()
                + " vida=" + String.format("%.1f", bot.getHealth())
                + " blocos=" + BotInventory.countBlocks(bot)
                + (disabled ? " (IA DESATIVADA por erros)" : "");
    }
}
