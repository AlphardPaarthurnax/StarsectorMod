package com.github.alphardpaarthurnax.script.animation.animations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

import static com.github.alphardpaarthurnax.script.utils.GraphicUtils.smoothstep;

/** fusion_lamp_glow原图中心16*16约为满alpha,32*32为光晕大小<br>
 * 因此在使用时需注意将图片大小设为期望核心大小*4 */
public class FlashAnimation extends Animation{
    //render
    private transient SpriteAPI sprite;
    private float flashAlpha = 0f;
    private float flashSize = 0f;
    private Vector2f location = new Vector2f(0f,0f);
    private PlanetAPI planet;
    private Color color;
    //final
    private final float k;
    private final float minRadius;
    private final float maxRadius;

    public FlashAnimation(Vector2f location,float k, float minRadius, float maxRadius, float delayTime, float durationTime, Color color) {
        super(delayTime, durationTime);
        this.location = location;
        this.color = color;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.k = k;
    }
    public FlashAnimation(PlanetAPI planet,float k, float maxRadius, float delayTime, float durationTime, Color color) {
        super(delayTime, durationTime);
        this.planet = planet;
        this.color = color;
        this.minRadius = planet.getRadius() * 5f;
        this.maxRadius = maxRadius;
        this.k = k;
    }
    public FlashAnimation(PlanetAPI planet,float k, float delayTime, float durationTime, Color color) {
        super(delayTime, durationTime);
        this.planet = planet;
        this.color = color;
        this.minRadius = planet.getRadius() * 5f;
        this.maxRadius = planet.getRadius() * 5f;
        this.k = k;
    }
    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (flashAlpha <= 0f) return;
        if (sprite == null) sprite = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");

        sprite.setAdditiveBlend();
        sprite.setColor(color);
        sprite.setSize(flashSize, flashSize);
        sprite.setAlphaMult(flashAlpha);
        sprite.renderAtCenter(location.x, location.y);
    }
    @Override
    public void update(float amt) {
        if(planet != null)
            location = planet.getLocation();

        // 归一化
        float t = Math.min(liveTimer / durationTime, 1f);

        if (t < k) {
            flashSize = minRadius + (maxRadius - minRadius) * k/t;
            flashAlpha = 1f;
        } else {
            flashAlpha = 1f - smoothstep(0f, 1f, (t - k) / (1f - k));
        }
    }
    @Override
    public EnumSet<CampaignEngineLayers> getLayers() {
        return EnumSet.of(CampaignEngineLayers.ABOVE);
    }
}
