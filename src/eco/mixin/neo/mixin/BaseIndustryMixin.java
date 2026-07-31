package eco.mixin.neo.mixin;

import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.util.Pair;
import eco.core.IndustryEconomy;
import eco.mixin.neo.BaseIndustryBridge;
import eco.mixin.neo.BridgedMutableCommodityQuantity;
import eco.mixin.neo.BridgedMutableStat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BaseIndustry.class)
public abstract class BaseIndustryMixin implements BaseIndustryBridge {

    @Shadow protected Map<String, MutableCommodityQuantity> supply;
    @Shadow protected Map<String, MutableCommodityQuantity> demand;
    @Shadow protected MutableStat income;
    @Shadow protected MutableStat upkeep;
    @Shadow protected MarketAPI market;
    @Unique private transient Map<String, BridgedMutableCommodityQuantity> ECON$modSupply = new LinkedHashMap<>();
    @Unique private transient Map<String, BridgedMutableCommodityQuantity> ECON$modDemand = new LinkedHashMap<>();
    @Unique private transient IndustryEconomy ECON$industryEconomy;
    @Unique private transient BridgedMutableStat ECON$modIncome;
    @Unique private transient BridgedMutableStat ECON$modUpkeep;
    @Unique private boolean ECON$inApply;
    @Unique
    private void checkTransientMap(){
        if (ECON$modSupply == null) { ECON$modSupply = new LinkedHashMap<>(); }
        if (ECON$modDemand == null) { ECON$modDemand = new LinkedHashMap<>(); }
    }
    @Override
    public void ECON$dataUpdate(IndustryEconomy industryEconomy) {
        this.ECON$industryEconomy = industryEconomy;
        checkTransientMap();

        Set<String> supplyIds = new LinkedHashSet<>(supply.keySet());
        supplyIds.addAll(industryEconomy.getAllModSupply().keySet());

        for (String commodityId : supplyIds) {
            ECON$getOrCreateSupplyBridge(commodityId);
        }

        Set<String> demandIds = new LinkedHashSet<>(demand.keySet());
        demandIds.addAll(industryEconomy.getAllModDemand().keySet());

        for (String commodityId : demandIds) {
            ECON$getOrCreateDemandBridge(commodityId);
        }
    }
    @Unique
    private BridgedMutableCommodityQuantity ECON$getOrCreateSupplyBridge(String commodityId){
        checkTransientMap();
        return ECON$modSupply.computeIfAbsent(commodityId, id -> new BridgedMutableCommodityQuantity(
                id,
                () -> supply.computeIfAbsent(id, MutableCommodityQuantity::new).getQuantity(),
                () -> {
                    IndustryEconomy economy = ECON$industryEconomy;
                    if (economy == null) return null;
                    MutableCommodityQuantity calculated = economy.getAllModSupply().get(id);
                    return calculated == null ? null : calculated.getQuantity();
                }));
    }
    @Unique
    private BridgedMutableCommodityQuantity ECON$getOrCreateDemandBridge(String commodityId){
        checkTransientMap();
        return ECON$modDemand.computeIfAbsent(commodityId, id -> new BridgedMutableCommodityQuantity(
                id,
                () -> demand.computeIfAbsent(id, MutableCommodityQuantity::new).getQuantity(),
                () -> {
                    IndustryEconomy economy = ECON$industryEconomy;
                    if (economy == null) return null;
                    MutableCommodityQuantity calculated = economy.getAllModDemand().get(id);
                    return calculated == null ? null : calculated.getQuantity();
                }));
    }
    @Inject(method = "getAllSupply", at = @At("HEAD"), cancellable = true)
    public void injectGetAllSupply(CallbackInfoReturnable<List<MutableCommodityQuantity>> cir){
        checkTransientMap();
        Map<String, MutableCommodityQuantity> current = ECON$industryEconomy == null ? supply : ECON$industryEconomy.getAllModSupply();

        List<MutableCommodityQuantity> result = new ArrayList<>();
        for (Map.Entry<String, MutableCommodityQuantity> entry : current.entrySet()){
            if (entry.getValue().getQuantity().getModifiedValue() > 0f){
                result.add(ECON$getOrCreateSupplyBridge(entry.getKey()));
            }
        }
        cir.setReturnValue(result);
    }
    @Inject(method = "getAllDemand", at = @At("HEAD"), cancellable = true)
    public void injectGetAllDemand(CallbackInfoReturnable<List<MutableCommodityQuantity>> cir){
        checkTransientMap();
        Map<String, MutableCommodityQuantity> current = ECON$industryEconomy == null ? demand : ECON$industryEconomy.getAllModDemand();

        List<MutableCommodityQuantity> result = new ArrayList<>();
        for (Map.Entry<String, MutableCommodityQuantity> entry : current.entrySet()){
            if (entry.getValue().getQuantity().getModifiedValue() > 0f){
                result.add(ECON$getOrCreateDemandBridge(entry.getKey()));
            }
        }
        cir.setReturnValue(result);
    }
    @Inject(method = "getSupply", at = @At("HEAD"), cancellable = true)
    public void injectGetSupply(String id, CallbackInfoReturnable<MutableCommodityQuantity> cir){
        cir.setReturnValue(ECON$getOrCreateSupplyBridge(id));
    }
    @Inject(method = "getDemand", at = @At("HEAD"), cancellable = true)
    public void injectGetDemand(String id, CallbackInfoReturnable<MutableCommodityQuantity> cir){
        cir.setReturnValue(ECON$getOrCreateDemandBridge(id));
    }
    @Inject(method = "getIncome", at = @At("HEAD"), cancellable = true)
    public void injectGetIncome(CallbackInfoReturnable<MutableStat> cir){
        if (ECON$modIncome == null) {
            ECON$modIncome = new BridgedMutableStat(
                () -> income,
                () -> ECON$industryEconomy == null ? null : ECON$industryEconomy.getModIncome()
            );
        }
        cir.setReturnValue(ECON$modIncome);
    }
    @Inject(method = "getUpkeep", at = @At("HEAD"), cancellable = true)
    public void injectGetUpkeep(CallbackInfoReturnable<MutableStat> cir){
        if (ECON$modUpkeep == null) {
            ECON$modUpkeep = new BridgedMutableStat(
                    () -> upkeep,
                    () -> ECON$industryEconomy == null ? null : ECON$industryEconomy.getModUpkeep()
            );
        }
        cir.setReturnValue(ECON$modUpkeep);
    }
    /** 运行在 apply() 时设置状态*/
    @Redirect(method = "reapply", at = @At(value = "INVOKE", target = "Lcom/fs/starfarer/api/impl/campaign/econ/impl/BaseIndustry;apply()V"))
    private void applyInReapply(BaseIndustry industry) {
        if (industry instanceof PopulationAndInfrastructure) {
            industry.apply();
            ECON$inApply = false;
            return;
        }

        ECON$inApply = true;
        try {
            industry.apply();
        } finally {
            ECON$inApply = false;
        }
    }
    @Inject(method = "getMaxDeficit", at = @At("HEAD"), cancellable = true)
    private void injectGetMaxDeficit(String[] commodityIds, CallbackInfoReturnable<Pair<String, Integer>> cir) {
        if (ECON$inApply) {
            cir.setReturnValue(new Pair<>(null, 0));
        }
    }
    @Inject(method = "getAllDeficit()Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void injectGetAllDeficit(CallbackInfoReturnable<List<Pair<String, Integer>>> cir) {
        if (ECON$inApply) {
            cir.setReturnValue(new ArrayList<>());
        }
    }
    @Inject(method = "getAllDeficit([Ljava/lang/String;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void injectGetAllDeficit(String[] commodityIds, CallbackInfoReturnable<List<Pair<String, Integer>>> cir) {
        if (ECON$inApply) {
            cir.setReturnValue(new ArrayList<>());
        }
    }
}
