package dev.rsttst.kolada

import dev.rsttst.kolada.internal.KoladaModContainer
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext

/**
 * Replaces the default FML [FMLJavaModLoadingContext.get().getModEventBus()][net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext] call
 * often used in the public constructor of the main mod class.
 *
 * Use this method to get the [IEventBus] associated with your mod.
 * Any event listeners subscribed to this bus will only receive events specific to your mod.
 *
 * This method can only be called during mod loading and only within mods loaded with Kolada.
 * The init block of your [@KMod][KMod] annotated main mod object is a good place to use this method.
 * @return The [IEventBus] associated with your mod.
 */
fun getModEventBus() : IEventBus {
    val container = ModLoadingContext.get().activeContainer;
    check(container is KoladaModContainer) { "'getModEventBus()' can only used while loading a mod using Kolada" }
    return container.eventBus
}