package eco.mixin.neo.mixin;

import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
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
    @Unique
    private transient Map<String, BridgedMutableCommodityQuantity> coreCracking$modSupply = new LinkedHashMap<>();
    @Unique
    private transient Map<String, BridgedMutableCommodityQuantity> coreCracking$modDemand = new LinkedHashMap<>();

    @Unique
    private void checkTransientMap(){
        if (coreCracking$modSupply == null) { coreCracking$modSupply = new LinkedHashMap<>(); }
        if (coreCracking$modDemand == null) { coreCracking$modDemand = new LinkedHashMap<>(); }
    }
    @Unique
    private transient IndustryEconomy coreCracking$industryEconomy;
    @Unique
    private transient BridgedMutableStat coreCracking$modIncome;
    @Unique
    private transient BridgedMutableStat coreCracking$modUpkeep;
    @Override
    public void coreCracking$dataUpdate(IndustryEconomy industryEconomy) {
        this.coreCracking$industryEconomy = industryEconomy;
        checkTransientMap();

        Set<String> supplyIds = new LinkedHashSet<>(supply.keySet());
        supplyIds.addAll(industryEconomy.getAllModSupply().keySet());

        for (String commodityId : supplyIds) {
            coreCracking$getOrCreateSupplyBridge(commodityId);
        }

        Set<String> demandIds = new LinkedHashSet<>(demand.keySet());
        demandIds.addAll(industryEconomy.getAllModDemand().keySet());

        for (String commodityId : demandIds) {
            coreCracking$getOrCreateDemandBridge(commodityId);
        }
    }
    @Unique
    private BridgedMutableCommodityQuantity coreCracking$getOrCreateSupplyBridge(String commodityId){
        checkTransientMap();
        return coreCracking$modSupply.computeIfAbsent(commodityId, id -> new BridgedMutableCommodityQuantity(
                id,
                () -> supply.computeIfAbsent(id, MutableCommodityQuantity::new).getQuantity(),
                () -> {
                    IndustryEconomy economy = coreCracking$industryEconomy;
                    if (economy == null) return null;
                    MutableCommodityQuantity calculated = economy.getAllModSupply().get(id);
                    return calculated == null ? null : calculated.getQuantity();
                }));
    }
    @Unique
    private BridgedMutableCommodityQuantity coreCracking$getOrCreateDemandBridge(String commodityId){
        checkTransientMap();
        return coreCracking$modDemand.computeIfAbsent(commodityId, id -> new BridgedMutableCommodityQuantity(
                id,
                () -> demand.computeIfAbsent(id, MutableCommodityQuantity::new).getQuantity(),
                () -> {
                    IndustryEconomy economy = coreCracking$industryEconomy;
                    if (economy == null) return null;
                    MutableCommodityQuantity calculated = economy.getAllModDemand().get(id);
                    return calculated == null ? null : calculated.getQuantity();
                }));
    }
    @Unique
    private BridgedMutableStat coreCracking$getOrCreateIncomeBridge(){
        if (coreCracking$modIncome == null) coreCracking$modIncome = new BridgedMutableStat(
                () -> income,
                () -> coreCracking$industryEconomy == null ? null : coreCracking$industryEconomy.getModIncome());
        return coreCracking$modIncome;
    }
    @Unique
    private BridgedMutableStat coreCracking$getOrCreateUpkeepBridge(){
        if (coreCracking$modUpkeep == null) coreCracking$modUpkeep = new BridgedMutableStat(
                () -> upkeep,
                () -> coreCracking$industryEconomy == null ? null : coreCracking$industryEconomy.getModUpkeep());
        return coreCracking$modUpkeep;
    }
    @Inject(method = "getAllSupply", at = @At("HEAD"), cancellable = true)
    public void injectGetAllSupply(CallbackInfoReturnable<List<MutableCommodityQuantity>> cir){
        checkTransientMap();
        Map<String, MutableCommodityQuantity> current = coreCracking$industryEconomy == null ? supply : coreCracking$industryEconomy.getAllModSupply();

        List<MutableCommodityQuantity> result = new ArrayList<>();
        for (Map.Entry<String, MutableCommodityQuantity> entry : current.entrySet()){
            if (entry.getValue().getQuantity().getModifiedValue() > 0f){
                result.add(coreCracking$getOrCreateSupplyBridge(entry.getKey()));
            }
        }
        cir.setReturnValue(result);
    }
    @Inject(method = "getSupply", at = @At("HEAD"), cancellable = true)
    public void injectGetSupply(String id, CallbackInfoReturnable<MutableCommodityQuantity> cir){
        cir.setReturnValue(coreCracking$getOrCreateSupplyBridge(id));
    }
    @Inject(method = "getAllDemand", at = @At("HEAD"), cancellable = true)
    public void injectGetAllDemand(CallbackInfoReturnable<List<MutableCommodityQuantity>> cir){
        checkTransientMap();
        Map<String, MutableCommodityQuantity> current = coreCracking$industryEconomy == null ? demand : coreCracking$industryEconomy.getAllModDemand();

        List<MutableCommodityQuantity> result = new ArrayList<>();
        for (Map.Entry<String, MutableCommodityQuantity> entry : current.entrySet()){
            if (entry.getValue().getQuantity().getModifiedValue() > 0f){
                result.add(coreCracking$getOrCreateDemandBridge(entry.getKey()));
            }
        }
        cir.setReturnValue(result);
    }
    @Inject(method = "getDemand", at = @At("HEAD"), cancellable = true)
    public void injectGetDemand(String id, CallbackInfoReturnable<MutableCommodityQuantity> cir){
        cir.setReturnValue(coreCracking$getOrCreateDemandBridge(id));
    }
    @Inject(method = "getIncome", at = @At("HEAD"), cancellable = true)
    public void injectGetIncome(CallbackInfoReturnable<MutableStat> cir){
        cir.setReturnValue(coreCracking$getOrCreateIncomeBridge());
    }
    @Inject(method = "getUpkeep", at = @At("HEAD"), cancellable = true)
    public void injectGetUpkeep(CallbackInfoReturnable<MutableStat> cir){
        cir.setReturnValue(coreCracking$getOrCreateUpkeepBridge());
    }
    @Inject(method = "getMaxDeficit", at = @At("HEAD"), cancellable = true)
    public void injectGetMaxDeficit(CallbackInfoReturnable<Pair<String, Integer>> cir){
        cir.setReturnValue(new Pair<>(null,0));
    }
    @Inject(method = "getAllDeficit()Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void coreCracking$disableAllDeficit(CallbackInfoReturnable<List<Pair<String, Integer>>> cir) {
        cir.setReturnValue(new ArrayList<>());
    }
    @Inject(method = "getAllDeficit([Ljava/lang/String;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void coreCracking$disableAllDeficitForCommodities(String[] commodityIds, CallbackInfoReturnable<List<Pair<String, Integer>>> cir) {
        cir.setReturnValue(new ArrayList<>());
    }
}
