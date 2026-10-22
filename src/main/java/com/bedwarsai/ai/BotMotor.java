package com.bedwarsai.ai;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;

import java.util.Random;

/** Simula o "teclado e mouse" do bot: escreve entradas que a fisica do jogo consome no tick. */
public class BotMotor {

    private final EntityPlayerMP bot;
    private final Random rnd;

    public float forward;
    public float strafe;
    public boolean jump;
    public boolean sneak;
    public boolean sprint;

    public BotMotor(EntityPlayerMP bot, Random rnd) {
        this.bot = bot;
        this.rnd = rnd;
    }

    public void reset() {
        forward = 0f;
        strafe = 0f;
        jump = false;
        sneak = false;
        sprint = false;
    }

    public void apply() {
        bot.moveForward = forward;
        bot.moveStrafing = strafe;
        bot.setJumping(jump);
        bot.setSneaking(sneak);
        bot.setSprinting(sprint && forward > 0f && !sneak && !bot.isUsingItem());
    }

    /** Gira em direcao ao ponto. Retorna o erro de yaw restante (graus, valor absoluto). */
    public float lookAt(double x, double y, double z, float maxStep, float errorDeg) {
        double dx = x - bot.posX;
        double dy = y - (bot.posY + bot.getEyeHeight());
        double dz = z - bot.posZ;
        double h = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90f;
        float pitch = (float) (-(Math.atan2(dy, h) * 180.0 / Math.PI));
        yaw += (rnd.nextFloat() - 0.5f) * 2f * errorDeg;
        pitch += (rnd.nextFloat() - 0.5f) * 2f * errorDeg;
        return turnTo(yaw, pitch, maxStep);
    }

    public float turnTo(float yaw, float pitch, float maxStep) {
        float dYaw = MathHelper.clamp_float(
                MathHelper.wrapAngleTo180_float(yaw - bot.rotationYaw), -maxStep, maxStep);
        float dPitch = MathHelper.clamp_float(pitch - bot.rotationPitch, -maxStep, maxStep);
        bot.rotationYaw += dYaw;
        bot.rotationPitch = MathHelper.clamp_float(bot.rotationPitch + dPitch, -90f, 90f);
        bot.rotationYawHead = bot.rotationYaw;
        return Math.abs(MathHelper.wrapAngleTo180_float(yaw - bot.rotationYaw));
    }
}
