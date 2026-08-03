package com.github.alphardpaarthurnax.script.animation.animations;

import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import lunalib.lunaUtil.campaign.LunaCampaignRenderer;
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin;

import java.util.EnumSet;

public abstract class Animation {
    public final float durationTime;
    public final float delayTime;
    /** Timer to end play*/
    public float liveTimer = 0f;
    /** Timer to end delay*/
    public float sleepTimer = 0f;
    private boolean isInit = false;


    /**
     * 0----->play-------->done <br>
     *  delay~~~~duration
     * */
    public Animation(float delayTime, float durationTime) {
        this.delayTime = delayTime;
        this.durationTime = durationTime;
    }

    public boolean isDone() {
        return liveTimer >= durationTime;
    }

    public void advance(float amt) {
        sleepTimer += amt;
        if (sleepTimer >= delayTime && !isDone()) {
            liveTimer += amt;
            if (!isInit) {
                isInit = true;
                init();
            }
            update(amt);
        }
    }
    public void init() {
        LunaCampaignRenderer.addRenderer(new LunaCampaignRenderingPlugin() {
            @Override
            public boolean isExpired() { return isDone(); }
            @Override
            public void advance(float amount) {}
            @Override
            public EnumSet<CampaignEngineLayers> getActiveLayers() {
                return Animation.this.getLayers();
            }
            @Override
            public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
                Animation.this.render(layer, viewport);
            }
        });
    }
    /** 图层 */
    public abstract EnumSet<CampaignEngineLayers> getLayers();
    /** 渲染函数 */
    public abstract void render(CampaignEngineLayers layer, ViewportAPI viewport);
    /** 帧更新 */
    public abstract void update(float amt);
}
