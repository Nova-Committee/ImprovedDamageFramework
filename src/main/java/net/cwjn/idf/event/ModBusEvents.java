package net.cwjn.idf.event;

import net.cwjn.idf.particle.IDFParticles;
import net.cwjn.idf.particle.custom.NumberParticle;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.register(IDFParticles.NUMBER_PARTICLE.get(), NumberParticle.Provider::new);
    }

}
