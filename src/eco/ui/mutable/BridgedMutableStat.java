package eco.ui.mutable;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p> 桥接MutableStat
 * <p> mod set->BridgedMutableStat->原版↓
 * <p> mod get<-BridgedMutableStat<-经济系统
 * */
public final class BridgedMutableStat extends MutableStat {
    private final Supplier<? extends MutableStat> sourceSupplier;
    private final Supplier<? extends MutableStat> calculatedSupplier;
    public BridgedMutableStat(Supplier<? extends MutableStat> sourceSupplier, Supplier<? extends MutableStat> calculatedSupplier) {
        super(0f);
        this.sourceSupplier = Objects.requireNonNull(sourceSupplier, "sourceSupplier");
        this.calculatedSupplier = Objects.requireNonNull(calculatedSupplier, "calculatedSupplier");
    }
    public MutableStat getSourceStat() {
        MutableStat source = Objects.requireNonNull(sourceSupplier.get(), "sourceSupplier is null");
        if (source == this) {
            throw new IllegalStateException("BridgedMutableStat source cannot be itself");
        }
        return source;
    }
    public MutableStat getCalculatedStat() {
        MutableStat calculated = calculatedSupplier.get();
        if (calculated == null || calculated == this) {
            calculated = getSourceStat();
        }
        return calculated;
    }


    //<editor-fold desc="Calculated Data Getter">
    // ---------------------- //
    // calculated data getter //
    // ---------------------- //
    @Override
    public MutableStat createCopy() {
        return getCalculatedStat().createCopy();
    }
    @Override
    public boolean isUnmodified() {
        return getCalculatedStat().isUnmodified();
    }
    @Override
    public boolean isPositive() {
        return getCalculatedStat().isPositive();
    }
    @Override
    public boolean isNegative() {
        return getCalculatedStat().isNegative();
    }
    @Override
    public float getFlatMod() {
        return getCalculatedStat().getFlatMod();
    }
    @Override
    public float getPercentMod() {
        return getCalculatedStat().getPercentMod();
    }
    @Override
    public float getMult() {
        return getCalculatedStat().getMult();
    }
    @Override
    public HashMap<String, StatMod> getFlatMods() {
        return getCalculatedStat().getFlatMods();
    }
    @Override
    public HashMap<String, StatMod> getPercentMods() {
        return getCalculatedStat().getPercentMods();
    }
    @Override
    public HashMap<String, StatMod> getMultMods() {
        return getCalculatedStat().getMultMods();
    }
    @Override
    public StatMod getFlatStatMod(String id) {
        return getCalculatedStat().getFlatStatMod(id);
    }
    @Override
    public StatMod getPercentStatMod(String id) {
        return getCalculatedStat().getPercentStatMod(id);
    }
    @Override
    public StatMod getMultStatMod(String id) {
        return getCalculatedStat().getMultStatMod(id);
    }
    @Override
    public float computeMultMod() {
        return getCalculatedStat().computeMultMod();
    }
    @Override
    public float getBaseValue() {
        this.base = getCalculatedStat().getBaseValue();
        return base;
    }
    @Override
    public float getModifiedValue() {
        this.modified = getCalculatedStat().getModifiedValue();
        return modified;
    }
    @Override
    public int getModifiedInt() {
        return Math.round(getModifiedValue());
    }
    //</editor-fold>

    //<editor-fold desc="Source Data Setter">
    // ------------------ //
    // source data setter //
    // ------------------ //
    @Override
    public void applyMods(MutableStat other) {
        getSourceStat().applyMods(other);
    }
    @Override
    public void applyMods(StatBonus other) {
        getSourceStat().applyMods(other);
    }
    @Override
    public void modifyFlat(String id, float value) {
        getSourceStat().modifyFlat(id, value);
    }
    @Override
    public void modifyPercent(String id, float value) {
        getSourceStat().modifyPercent(id, value);
    }
    @Override
    public void modifyMult(String id, float value) {
        getSourceStat().modifyMult(id, value);
    }
    @Override
    public void modifyFlat(String id, float value, String desc) {
        getSourceStat().modifyFlat(id, value, desc);
    }
    @Override
    public void modifyPercent(String id, float value, String desc) {
        getSourceStat().modifyPercent(id, value, desc);
    }
    @Override
    public void modifyMult(String id, float value, String desc) {
        getSourceStat().modifyMult(id, value, desc);
    }
    @Override
    public void modifyFlatAlways(String id, float value, String desc) {
        getSourceStat().modifyFlatAlways(id, value, desc);
    }
    @Override
    public void modifyPercentAlways(String id, float value, String desc) {
        getSourceStat().modifyPercentAlways(id, value, desc);
    }
    @Override
    public void modifyMultAlways(String id, float value, String desc) {
        getSourceStat().modifyMultAlways(id, value, desc);
    }
    @Override
    public void unmodify() {
        getSourceStat().unmodify();
    }
    @Override
    public void unmodify(String id) {
        getSourceStat().unmodify(id);
    }
    @Override
    public void unmodifyFlat(String id) {
        getSourceStat().unmodifyFlat(id);
    }
    @Override
    public void unmodifyPercent(String id) {
        getSourceStat().unmodifyPercent(id);
    }
    @Override
    public void unmodifyMult(String id) {
        getSourceStat().unmodifyMult(id);
    }
    @Override
    public void setBaseValue(float value) {
        getSourceStat().setBaseValue(value);
    }
    //</editor-fold>
}
