package eu.pb4.farmersdelightpatch.mixin.bedrock;

import eu.pb4.farmersdelightpatch.impl.bedrock.BedrockSupport;
import eu.pb4.farmersdelightpatch.impl.block.MapPolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapPolymerBlock.class)
public class MapPolymerBlockMixin {
    @Inject(method = "getPolymerBlockState", at = @At("HEAD"), cancellable = true)
    private void farmersdelight_unmaskBlockForBedrock(BlockState state, PacketContext context, CallbackInfoReturnable<BlockState> cir) {
        if (BedrockSupport.isBedrockPlayer(context)) {
            cir.setReturnValue(state);
        }
    }
}
