# Kolada 
Kolada increases the compatibility of Forge with the Kotlin™ programming language.
It provides a language loader with custom annotations and bundles commonly used Kotlin libraries in its mod-jar.
Kolada works with MC version 1.14.4.

Kolada is similar to [Shadowfacts' Forgelin](https://www.curseforge.com/minecraft/mc-mods/shadowfacts-forgelin/files) (up to MC version 1.8.4) in its function.

## Mod-Jar Contents
The following libraries including their respective dependencies are bundled into the Kolada mod-jar (kolada-runtime artifact):
* Kotlin Standard Library _1.3.60_
* Kotlin Reflection _1.3.60_
* Kotlinx Coroutines Core _1.3.2_
* Kotlinx Serialization Runtime _0.13.0_

## Install
There are three maven artifacts of Kolada available:
* **kolada:** All of Kolada including internal files
* **kolada-api:** The API of Kolada
* **kolada-runtime:** All of Kolada + bundled libraries (see [Mod-Jar Contents](#mod-jar-contents))

All of these can be found in my Bintray repository (https://dl.bintray.com/rsttst/Minecraft-Forge-Mods/) under group `dev.rsttst.kolada`.

Recommended Gradle configuration:
```groovy
repositories {
    maven {
        url 'https://dl.bintray.com/rsttst/Minecraft-Forge-Mods/'
    }
}

dependencies {
    // *UNCOMMENT LIBRARIES NEEDED*
    //compileOnly 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.60'
    //compileOnly 'org.jetbrains.kotlin:kotlin-reflect:1.3.60'
    //compileOnly 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2'
    //compileOnly 'org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0'
    compileOnly fg.deobf('dev.rsttst.kolada:kolada-api:1.0')
    runtimeOnly fg.deobf('dev.rsttst.kolada:kolada-runtime:1.0')
}
```
## Use
### Config
To load a mod with Kolada edit/add the following lines in your `mods.toml` file:
```toml
modLoader="kolada" #select Kolada as the mod loader
loaderVersion="[1.0,)" #version 1.0

[[dependencies.REPLACE_WITH_MOD_ID]] #add Kolada as a dependency of your mod
    modId="kolada"
    mandatory=true
    versionRange="[1.0,)"
    ordering="NONE"
    side="BOTH"
```
### Code
Kolada introduces its own annotations for mod loading. Kolada annotations that replace existing FML annotations have the same name prefixed with a 'K'.
#### `@KMod`
Replaces the default FML `@Mod` annotation and defines the entry point of your mod.

Object declarations are the only valid target of this annotation.  
Code that would go in the default constructor of an `@Mod` annotated class can be put inside the `init` block.
The `value` parameter specifies the mod-ID of your mod.
```kotlin
@KMod(MOD_ID)
object Mod {

    const val MOD_ID = "REPLACE_WITH_MOD_ID"

    init {
        val modEventBus = getModEventBus()
        modEventBus.addListener<FMLCommonSetupEvent> { this.setup(it) }
        // "modEventBus.addListener(this::setup)" fails here, I will look into that
    }
    
    private fun setup(event: FMLCommonSetupEvent) { /*pre-init code*/ }

}
```
(Note the use of the `getModEventBus()` top-level function. It returns the event bus for the mod currently being loaded.
This replaces the `FMLJavaModLoadingContext.get().getModEventBus()` call.)
#### `@KMod.KEventBusSubscriber`
Replaces the default FML `@Mod.EventBusSubscriber` annotation and marks an object declaration as an event subscriber.

Object declarations are the only valid target of this annotation.  
All methods of the object annotated with `@SubscribeEvent` will be registered at the event bus specified by the `bus` parameter.
The `Dist`(s) on which the methods will get registered can be selected via the `value` parameter.  
Every object declaration annotated with `@KMod.KEventBusSubscriber` that is not nested inside of a `@KMod` annotated object declaration should have the `modId` parameter explicitly specified.
```kotlin
@KMod.KEventBusSubscriber(value = [Dist.CLIENT, Dist.SERVER], bus = KMod.KEventBusSubscriber.Bus.MOD, modId = "REPLACE_WITH_MOD_ID")
object EventSubscriber {
    
   @SubscribeEvent
   fun onBlockRegistry(blockRegistryEvent: RegistryEvent.Register<Block>) { /*block-registry code*/ }

}
```
