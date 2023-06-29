package com.valorin.ranking.hologram;

public enum HologramPlugin {
    HOLOGRAPHIC_DISPLAY("HolographicDisplay"),
    TRHOLOGRAM("TrHologram"),
    CMI("CMI"),
    DECENT_HOLOGRAM("DecentHolograms");

    private final String realName;
    HologramPlugin(String realName) {
        this.realName = realName;
    }

    public String getRealName() {
        return realName;
    }
}
