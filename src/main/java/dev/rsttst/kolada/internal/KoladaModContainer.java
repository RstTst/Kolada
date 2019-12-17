package dev.rsttst.kolada.internal;

import dev.rsttst.kolada.KMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBusErrorMessage;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LifecycleEventProvider.LifecycleEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static kotlin.jvm.JvmClassMappingKt.getKotlinClass;
import static net.minecraftforge.fml.Logging.LOADING;
import static net.minecraftforge.fml.ModLoadingStage.*;

public class KoladaModContainer extends ModContainer {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Type EVENT_SUBSCRIBER = Type.getType(KMod.KEventBusSubscriber.class);

    private final ModFileScanData scanData;

    private final IEventBus eventBus;
    private final Class<?> modClass;
    private Object modInstance;

    public KoladaModContainer(final IModInfo info, final String className, final ClassLoader modClassLoader, final ModFileScanData scanData) {
        super(info);
        this.scanData = scanData;

        LOGGER.debug(LOADING, "Creating KoladaModContainer instance for {} with classLoader {} & {}", className, modClassLoader, getClass().getClassLoader());

        final List<ModLoadingStage> defaultHandlerStages = Arrays.asList(CREATE_REGISTRIES, LOAD_REGISTRIES, COMMON_SETUP, SIDED_SETUP, ENQUEUE_IMC, PROCESS_IMC, COMPLETE, GATHERDATA);
        final Consumer<LifecycleEvent> defaultHandler = createHandler(this::fireEvent);
        defaultHandlerStages.forEach(stage -> triggerMap.put(stage, defaultHandler));
        triggerMap.put(ModLoadingStage.CONSTRUCT, createHandler(this::constructMod));

        eventBus = BusBuilder.builder()
                .setExceptionHandler((bus, event, iEventListeners, i, throwable) -> LOGGER.error(new EventBusErrorMessage(event, i, iEventListeners, throwable)))
                .setTrackPhases(false)
                .build();

        this.configHandler = Optional.of(this.eventBus::post);

        contextExtension = () -> null;

        try {
            //NOTE: Don't initialize here so the init{} block can be used!
            modClass = Class.forName(className, false, modClassLoader);
            LOGGER.debug(LOADING, "Loaded modclass {} with {}", modClass.getName(), modClass.getClassLoader());
        } catch (Throwable ex) {
            LOGGER.error(LOADING, "Failed to load class {}. Error: {}", className, ex);
            throw new ModLoadingException(info, CONSTRUCT, "fml.modloading.failedtoloadmodclass", ex);
        }
    }

    private Consumer<LifecycleEvent> createHandler(Consumer<LifecycleEvent> mainHandler) {
            return mainHandler.andThen(this::afterEvent);
    }

    private void afterEvent(final LifecycleEvent lifecycleEvent) {
        if (getCurrentState() == ERROR) {
            LOGGER.error(LOADING, "An error occurred while dispatching event {} to {}", lifecycleEvent.fromStage(), getModId());
        }
    }

    private void fireEvent(final LifecycleEvent lifecycleEvent) {
        LOGGER.debug(LOADING, "LifecycleEvent: {}", lifecycleEvent);
        Event event = lifecycleEvent.getOrBuildEvent(this);
        LOGGER.debug(LOADING, "Firing event for modid {} : {}", this.getModId(), event);
        try {
            eventBus.post(event); //seems to rethrow exceptions that happen in event subscribers
            LOGGER.debug(LOADING, "Fired event for modid {} : {}", this.getModId(), event);
        } catch (Throwable ex) {
            LOGGER.error(LOADING, "Caught exception during event {} dispatch for modid {}", event, this.getModId(), ex);
            throw new ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.errorduringevent", ex);
        }
    }

    private void constructMod(final LifecycleEvent lifecycleEvent) {
        LOGGER.debug(LOADING, "Loading mod instance {} of type {}", getModId(), modClass.getName());
        this.modInstance = getKotlinClass(modClass).getObjectInstance();
        if (this.modInstance == null) {
            LOGGER.error(LOADING, "@KMod annotated class {} in mod {} is not an object declaration", modClass.getName(), getModId());
            throw new ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.failedtoloadmod",
                    new IllegalStateException("Mod class" + modClass.getName() + "is not an object declaration"), modClass);
        }
        LOGGER.debug(LOADING, "Loaded mod instance {} of type {}", getModId(), modClass.getName());

        //TODO: Consider a @KEventHandler annotation
        LOGGER.debug(LOADING, "Injecting @KMod.KEventBusSubscriber annotated classes for mod {}", getModId());
        injectEventSubscribers(lifecycleEvent);
        LOGGER.debug(LOADING, "Completed injecting @KMod.KEventSubscriber annotated classes for mod {}", getModId());
    }


    private void injectEventSubscribers(LifecycleEvent lifecycleEvent) {
        if (scanData == null) return; //TODO test if this can even happen

        List<ModFileScanData.AnnotationData> ebsTargets = scanData.getAnnotations().stream()
                .filter(ad -> EVENT_SUBSCRIBER.equals(ad.getAnnotationType()))
                .peek(ad -> LOGGER.debug(LOADING, "Found @KMod.KEventSubscriber class {}", ad.getClassType().getClassName()))
                .collect(Collectors.toList());

        for (ModFileScanData.AnnotationData eventSubscriber : ebsTargets) {

            /* All the calls to 'getAnnotationData().containsKey("value")' should be true I think. FML checks here anyway, so Kolada does it as well. */
            @SuppressWarnings("unchecked")
            EnumSet<Dist> sides = eventSubscriber.getAnnotationData().containsKey("value") ?
                    ((List<ModAnnotation.EnumHolder>) eventSubscriber.getAnnotationData().get("value")).stream()
                            .map(distHolder -> Dist.valueOf(distHolder.getValue()))
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Dist.class)))
                    : EnumSet.allOf(Dist.class);

            KMod.KEventBusSubscriber.Bus busTarget = eventSubscriber.getAnnotationData().containsKey("bus") ?
                    KMod.KEventBusSubscriber.Bus.valueOf(((ModAnnotation.EnumHolder) eventSubscriber.getAnnotationData().get("bus")).getValue())
                    : KMod.KEventBusSubscriber.Bus.FORGE;

            if ((!eventSubscriber.getAnnotationData().containsKey("modId") || modId.equals(eventSubscriber.getAnnotationData().get("modId"))) && sides.contains(FMLEnvironment.dist)) {
                LOGGER.debug(LOADING, "Trying to subscribe @KMod.KEventSubscriber {} to {}", eventSubscriber.getClassType().getClassName(), busTarget);
                Class<?> subscriberClass;
                try {
                    subscriberClass = Class.forName(eventSubscriber.getClassType().getClassName(), true, modClass.getClassLoader());
                } catch (Throwable ex) {
                    LOGGER.error(LOADING, "Failed to load class {}. Error: {}", eventSubscriber.getClassType().getClassName(), ex);
                    throw new ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.failedtoloadmodclass", ex);
                }
                Object subscriberInstance = getKotlinClass(subscriberClass).getObjectInstance();
                if (subscriberInstance == null) {
                    LOGGER.error(LOADING, "@KMod.KEventSubscriber annotated class {} in mod {} is not an object declaration.", subscriberClass.getName(), getModId());
                    throw new ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.failedtoloadmod",
                            new IllegalStateException("@KMod.KEventSubscriber annotated class " + subscriberClass.getName() + " is not an object declaration."), modClass);
                }
                switch (busTarget) {
                    case FORGE:
                        MinecraftForge.EVENT_BUS.register(subscriberInstance);
                        break;
                    case MOD:
                        eventBus.register(subscriberInstance);
                        break;
                }
                LOGGER.debug(LOADING, "Subscribed @KMod.KEventSubscriber {} to {}", eventSubscriber.getClassType().getClassName(), busTarget);
            }
        }

    }

    public IEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public boolean matches(Object other) {
        return modInstance == other;
    }

    @Override
    public Object getMod() {
        return modInstance;
    }

    @Override
    public void acceptEvent(Event event) {
        eventBus.post(event);
    }

}
