package com.github.alphardpaarthurnax.script.utils;


import java.awt.*;

public class GraphicUtils {
    public static float smoothstep(float edge0, float edge1, float x) {
        x = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return x * x * (3f - 2f * x);   // 3x^2 - 2x^3
    }
    public static Color lerpColor(Color a, Color b, float t) {
        float r = a.getRed() + (b.getRed() - a.getRed()) * t;
        float g = a.getGreen() + (b.getGreen() - a.getGreen()) * t;
        float bl = a.getBlue() + (b.getBlue() - a.getBlue()) * t;
        float al = a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t;
        return new Color((int) r, (int) g, (int) bl, (int) al);
    }
}
