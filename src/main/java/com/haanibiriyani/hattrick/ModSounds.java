package com.haanibiriyani.hattrick;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "hattrick");

    public static final RegistryObject<SoundEvent> ENFORCER_AGGRO =
            SOUND_EVENTS.register("enforcer_aggro",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_aggro")));

    public static final RegistryObject<SoundEvent> ENFORCER_AGGRO_BG =
            SOUND_EVENTS.register("enforcer_aggro_bg",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_aggro_bg")));

    public static final RegistryObject<SoundEvent> ENFORCER_DAMAGE =
            SOUND_EVENTS.register("enforcer_damage",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_damage")));

    public static final RegistryObject<SoundEvent> ENFORCER_DEATH =
            SOUND_EVENTS.register("enforcer_death",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_death")));

    public static final RegistryObject<SoundEvent> ENFORCER_IDLE =
            SOUND_EVENTS.register("enforcer_idle",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_idle")));

    public static final RegistryObject<SoundEvent> ENFORCER_OBSERVE =
            SOUND_EVENTS.register("enforcer_observe",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_observe")));

    public static final RegistryObject<SoundEvent> ENFORCER_SUMMON =
            SOUND_EVENTS.register("enforcer_summon",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_summon")));

    public static final RegistryObject<SoundEvent> ENFORCER_WARN =
            SOUND_EVENTS.register("enforcer_warn",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation("hattrick", "enforcer_warn")));
}