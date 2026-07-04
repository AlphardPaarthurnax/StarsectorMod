package eco.mixin;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.campaign.ui.marketinfo.CommodityTooltipFactory;
import com.fs.starfarer.settings.StarfarerSettings;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GameSettings {
    private static final Method getSprite$new;
    private static final Field getFont$float_new;

    static{
        try {
            getSprite$new = StarfarerSettings.class.getDeclaredMethod("new", String.class, String.class);
            getFont$float_new = StarfarerSettings.class.getDeclaredField("float.new");
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getSpritePath(String a, String b){
        try{
            return (String) getSprite$new.invoke(null,a,b);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getFont(){
        try{
            return (String) getFont$float_new.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
