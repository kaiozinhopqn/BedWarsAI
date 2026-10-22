package com.bedwarsai.bot;

/** Dificuldade: altera qualidade de mira, velocidade de reacao e CPS. Nunca da vida/dano extra. */
public enum Difficulty {
    //      cps   turn/tick  aimErr  reaction  strafes  eatBelowHealth
    EASY  ( 4f,   12f,      6.0f,   12,       false,   6f),
    NORMAL( 7f,   25f,      3.0f,    6,       true,    8f),
    HARD  (10f,   45f,      1.5f,    3,       true,   10f),
    INSANE(13f,   90f,      0.5f,    1,       true,   12f);

    public final float cps;
    public final float turnSpeed;
    public final float aimError;
    public final int reactionTicks;
    public final boolean strafes;
    public final float eatBelowHealth;

    Difficulty(float cps, float turnSpeed, float aimError, int reactionTicks,
               boolean strafes, float eatBelowHealth) {
        this.cps = cps;
        this.turnSpeed = turnSpeed;
        this.aimError = aimError;
        this.reactionTicks = reactionTicks;
        this.strafes = strafes;
        this.eatBelowHealth = eatBelowHealth;
    }

    public static Difficulty parse(String s) {
        for (Difficulty d : values()) {
            if (d.name().equalsIgnoreCase(s)) return d;
        }
        return null;
    }
}
