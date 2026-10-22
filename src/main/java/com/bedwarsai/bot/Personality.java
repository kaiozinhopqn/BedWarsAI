package com.bedwarsai.bot;

/** Personalidade: alcance de agressividade e quando come maca. */
public enum Personality {
    AGGRESSIVE(40.0, 0f),
    BALANCED(28.0, 2f),
    DEFENSIVE(14.0, 4f);

    public final double aggroRange;
    public final float eatBonus;

    Personality(double aggroRange, float eatBonus) {
        this.aggroRange = aggroRange;
        this.eatBonus = eatBonus;
    }

    public static Personality parse(String s) {
        for (Personality p : values()) {
            if (p.name().equalsIgnoreCase(s)) return p;
        }
        return null;
    }
}
