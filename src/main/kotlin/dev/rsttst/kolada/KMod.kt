package dev.rsttst.kolada

import net.minecraftforge.api.distmarker.Dist

/**
* This annotation replaces the default FML [@Mod][net.minecraftforge.fml.common.Mod] annotation and defines the entry point of your mod.
*
* Object declarations are the only valid target of this annotation.
* Code that would go in the default constructor of an [@Mod][net.minecraftforge.fml.common.Mod] annotated class can be put inside the init block.
* The value parameter specifies the mod-ID of your mod.
* @param value The mod-ID of the mod
*/
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KMod(val value: String) {

    /**
     * Replaces the default FML [@Mod.EventBusSubscriber][net.minecraftforge.fml.common.Mod.EventBusSubscriber] annotation and marks an object declaration as an event subscriber.
     *
     * Object declarations are the only valid target of this annotation.
     * All methods of the object annotated with [@SubscribeEvent][net.minecraftforge.eventbus.api.SubscribeEvent] will be registered at the event bus specified by the [bus] parameter.
     * The `Dist`(s) on which the methods will get registered can be selected via the [value] parameter.
     * Every object declaration annotated with [@KMod.KEventBusSubscriber][KEventBusSubscriber] that is not nested inside of a [@KMod][KMod] annotated object declaration should have the [modId] parameter explicitly specified.
     * @param value The `Dist`(s) on which the methods will get registered. Defaults to all Dists.
     * @param bus Specifies at which event bus the methods will get registered. Defaults to [Bus.FORGE]
     * @param modId The mod-ID of the mod this event bus subscriber belongs to. Defaults to an empty String. Explicitly set this parameter if this event bus subscriber is not nested inside your [@KMod][KMod] annotated mod entry point.
     * */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class KEventBusSubscriber(val value: Array<Dist> = [Dist.CLIENT, Dist.DEDICATED_SERVER], val bus: Bus = Bus.FORGE, val modId: String = "") {

        enum class Bus {
            FORGE,
            MOD
        }

    }

}