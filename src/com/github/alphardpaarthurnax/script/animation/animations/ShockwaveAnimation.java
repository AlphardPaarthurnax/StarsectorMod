package com.github.alphardpaarthurnax.script.animation.animations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

import static com.github.alphardpaarthurnax.script.utils.GraphicUtils.lerpColor;
import static com.github.alphardpaarthurnax.script.utils.GraphicUtils.smoothstep;

public class ShockwaveAnimation extends Animation{
    private transient SpriteAPI sprite;
    private final float startRadius;
    private final float endRadius;
    private final float thickness;
    private final Color startColor;
    private final Color endColor;
    private Vector2f location;
    private PlanetAPI planet;
    private float radius;
    private float alpha;

    public ShockwaveAnimation(Vector2f location, float startRadius, float endRadius, float thickness, float delay, float duration, Color startColor, Color endColor) {
        super(delay, duration);
        this.location = location;
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.thickness = thickness;
        this.startColor = startColor;
        this.endColor = endColor;
    }
    public ShockwaveAnimation(PlanetAPI planet, float endRadius, float thickness, float delay, float duration, Color startColor, Color endColor) {
        super(delay, duration);
        this.location = planet.getLocation();
        this.planet = planet;
        this.startRadius = planet.getRadius();
        this.endRadius = endRadius;
        this.thickness = thickness;
        this.startColor = startColor;
        this.endColor = endColor;
    }
    public ShockwaveAnimation(PlanetAPI planet, float thickness, float delay, float duration, Color startColor, Color endColor) {
        super(delay, duration);
        this.location = planet.getLocation();
        this.planet = planet;
        this.startRadius = planet.getRadius();
        this.endRadius = planet.getRadius() * 3f;
        this.thickness = thickness;
        this.startColor = startColor;
        this.endColor = endColor;
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (alpha <= 0f) return;

        if (sprite == null) sprite = Global.getSettings().getSprite("misc", "nebula_particles");

        int segments = Math.max(32, (int)(radius / 8f));

        Color color = lerpColor(startColor, endColor, 1f - alpha);
        for (int i = 0; i < segments; i++) {
            float angle = i * 360f / segments;
            Vector2f pos = MathUtils.getPointOnCircumference(location, radius, angle);

            sprite.setTexX(0f);
            sprite.setTexY(0f);
            sprite.setTexWidth(0.25f);
            sprite.setTexHeight(0.25f);
            sprite.setAdditiveBlend();
            sprite.setColor(color);
            sprite.setAlphaMult(alpha);
            sprite.setSize(thickness, thickness);
            sprite.renderAtCenter(pos.x, pos.y);
        }
    }
    @Override
    public void update(float amt) {
        if(planet != null)
            location = planet.getLocation();

        float t = Math.min(liveTimer / durationTime, 1f);
        radius = startRadius + (endRadius - startRadius) * smoothstep(0f, 1f, t);
        alpha = 1f - smoothstep(0f, 1f, t);
    }
    @Override
    public EnumSet<CampaignEngineLayers> getLayers() {
        return EnumSet.of(CampaignEngineLayers.TERRAIN_6A);
    }
}
