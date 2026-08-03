package tests.ui.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.github.alphardpaarthurnax.script.data.DialogData;
import org.lwjgl.util.vector.Vector2f;
import tests.ui.plugin.CloseUIPanelPlugin;
import tests.ui.plugin.CombinatorialPlugin;
import tests.ui.plugin.ImageUIPanelPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SuperEventDialogDelegate implements CustomVisualDialogDelegate {
    private CustomVisualDialogDelegate.DialogCallbacks callbacks;
    private String titleString = "";
    private List<String> desStrings = new ArrayList<>();
    private String buttonString = "";
    private final String planteName;

    public SuperEventDialogDelegate(String planteName) {
        this.planteName = planteName;
    }

    private void setter(){
        titleString = planteName + " 在爆炸中被毁灭";
        desStrings.add("根据通讯中继站传来的繁杂信息，经过汇总和整理，我们得知");
        desStrings.add(planteName + " 就在刚刚被彻底摧毁了！");
        desStrings.add("“我们知道世界从此不一样了。有人发笑，有人落泪，大多数人陷入沉默。”");
        desStrings.add("——罗伯特·奥本海默");
        buttonString = "这种罪行无法被饶恕";
    }
    @Override
    public void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks) {
        setter();

        float pw = panel.getPosition().getWidth();
        float ph = panel.getPosition().getHeight();
        this.callbacks = callbacks;

        TooltipMakerAPI title = panel.createUIElement(pw, 20f, false);
        title.addSectionHeading(titleString, DialogData.fillForeground, DialogData.fillBackground, Alignment.MID, 0f);
        panel.addUIElement(title).inTL(0f, 10f);

        float imgAreaH = ph - 124f - 29f;
        CustomPanelAPI imgPanel = Global.getSettings().createCustom(pw, imgAreaH,
                new ImageUIPanelPlugin("superevent","se_corecracking", new Vector2f(pw,imgAreaH)));
        TooltipMakerAPI imgElement = panel.createUIElement(pw, imgAreaH, false);
        imgElement.addCustom(imgPanel, 0f);
        panel.addUIElement(imgElement).inTL(0f, 29f);

        float btnW = 300f;
        float btnH = 20f;
        TooltipMakerAPI button = panel.createUIElement(pw, btnH, false);
        button.addButton(buttonString, "close", DialogData.fillForeground, DialogData.fillBackground, Alignment.MID, CutStyle.ALL, btnW, btnH, 0f);
        panel.addUIElement(button).inTL((pw - btnW) / 2f, ph - btnH - 25f);

        float textH = desStrings.size() * 14f + (desStrings.size()-1) * 13f;
        float textY = ph - btnH - 25f - 25f - textH;
        TooltipMakerAPI text = panel.createUIElement(pw, textH, false);
        for(String s : desStrings){
            if(Objects.equals(s, "")) continue;
            text.addPara(s, 10f).setAlignment(Alignment.RMID);
        }
        panel.addUIElement(text).inTL(0f, textY);
    }
    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return new CloseUIPanelPlugin(()->callbacks);
    }
    @Override
    public float getNoiseAlpha() {
        return 0f;
    }
    @Override
    public void advance(float amount) {}
    @Override
    public void reportDismissed(int reason) {}
}
