package eco.mixin.neo.industry;

import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;

import java.util.Objects;
import java.util.function.Supplier;

public final class BridgedMutableCommodityQuantity extends MutableCommodityQuantity {
    private final BridgedMutableStat bridgedQuantity;
    public BridgedMutableCommodityQuantity(String commodityId, Supplier<? extends MutableStat> sourceSupplier, Supplier<? extends MutableStat> calculatedSupplier) {
        super(Objects.requireNonNull(commodityId, "commodityId"));
        this.bridgedQuantity = new BridgedMutableStat(sourceSupplier, calculatedSupplier);
    }
    @Override
    public MutableStat getQuantity() {
        return bridgedQuantity;
    }
}
