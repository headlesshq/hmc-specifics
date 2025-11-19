package me.earth.headlessmc.mc.mixins;

import me.earth.headlessmc.mc.scheduling.SchedulesTasks;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Mixin(BlockableEventLoop.class)
public abstract class MixinBlockableEventLoop implements SchedulesTasks {
    @Shadow
    public abstract CompletableFuture<Void> submit(Runnable run);

    @Override
    public Future<?> schedule(Runnable task) {
        return submit(task);
    }

}
