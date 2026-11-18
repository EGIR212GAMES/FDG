package eu.pb4.farmersdelightpatch.mixin.bedrock;

import eu.pb4.farmersdelightpatch.impl.bedrock.BedrockSupport;
import eu.pb4.farmersdelightpatch.impl.item.PolySkilletItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PolySkilletItem.class)
public class PolySkilletItemMixin {
    @Inject(method = "getPolymerItem", at = @At("HEAD"), cancellable = true)
    private void farmersdelight_unmaskSkilletForBedrock(ItemStack itemStack, PacketContext packetContext, CallbackInfoReturnable<Item> cir) {
        if (BedrockSupport.isBedrockPlayer(packetContext)) {
            cir.setReturnValue(itemStack.getItem());
        }
    }

    @Inject(method = "modifyBasePolymerItemStack", at = @At("HEAD"), cancellable = true)
    private void farmersdelight_skipTrialKeyModifyForBedrock(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup, CallbackInfo ci) {
        if (BedrockSupport.isBedrockPlayer(context)) {
            ci.cancel();
        }
    }
}
