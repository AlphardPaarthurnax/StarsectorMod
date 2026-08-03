package com.github.alphardpaarthurnax.script.animation;

import com.fs.starfarer.api.EveryFrameScript;
import com.github.alphardpaarthurnax.script.animation.animations.Animation;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAnimationGroupScript implements EveryFrameScript {

    public List<Animation> AnimationList = new ArrayList<>();
    public boolean allDone = false;

    @Override
    public boolean isDone() {
        return allDone;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (allDone) return;
        if (AnimationList.isEmpty()) loadAnimations();

        for (Animation animation : AnimationList) animation.advance(amount);
        for (Animation animation : AnimationList) if (!animation.isDone()) return;

        //if every animation is done
        allDone = true;
    }

    public void addAnimation(Animation animation) {
        AnimationList.add(animation);
    }

    public abstract void loadAnimations();
}
