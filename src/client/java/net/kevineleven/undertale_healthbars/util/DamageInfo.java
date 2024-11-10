package net.kevineleven.undertale_healthbars.util;

public class DamageInfo {
    public static float GRAVITY = 0.05f;

    public float damage;
    public int timer;

    public float y_offset;
    public float y_velocity;

    public DamageInfo(float damage, int time, float jump_height) {
        this.damage = damage;
        this.timer = time;
        this.y_offset = 0;
        this.y_velocity = jump_height;

        // for changing values quick
//        this.y_velocity = 0.23f;
//        GRAVITY = 0.05f;
    }
}
