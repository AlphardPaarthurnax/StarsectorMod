package com.github.alphardpaarthurnax.script.animation;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.github.alphardpaarthurnax.script.animation.animations.FlashAnimation;
import com.github.alphardpaarthurnax.script.animation.animations.ShockwaveAnimation;

import java.awt.Color;

public class PlanetDestructionAnimationScript extends BaseAnimationGroupScript {
    private PlanetAPI planet;
    public PlanetDestructionAnimationScript(PlanetAPI planet) {
        this.planet = planet;
    }
    @Override
    public void loadAnimations() {
        AnimationList.add(new FlashAnimation(planet,0.4f,100000f,0f,5f,Color.white));
        AnimationList.add(new ShockwaveAnimation(planet, 80f, 2f, 1.5f, new Color(255, 205,40), new Color(255,140,40)));
        AnimationList.add(new ShockwaveAnimation(planet, 80f, 2.5f, 1.5f, new Color(245, 157, 77, 255), new Color(194, 146, 81)));
        AnimationList.add(new ShockwaveAnimation(planet, 80f, 3f, 1.5f, new Color(255, 255, 255), new Color(152, 152, 152)));
        AnimationList.add(new FlashAnimation(planet,0.5f,2f,40f,new Color(255, 128, 0)));

    }
}
