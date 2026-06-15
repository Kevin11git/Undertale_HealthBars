package net.kevineleven.undertale_healthbars.config;


import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;


public class ModConfig {

    public static final float MAXIMUM_MAX_DISTANCE = 100f;


        public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
                .id(Identifier.fromNamespaceAndPath(UndertaleHealthBarsClient.MOD_ID, "config"))
                .serializer(config -> GsonConfigSerializerBuilder.create(config)
                        .setPath(FabricLoader.getInstance().getConfigDir().resolve("undertale_healthbars.json5"))
                        .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                        .setJson5(true)
                        .build()
                )
                .build();

    public static Screen getScreen(Screen parent) {
        var optAlwaysShowHealthbar = Option.<Boolean>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option.alwaysShowHealthbar"))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.alwaysShowHealthbar.description")))
                .binding(false, () -> alwaysShowHealthbar, newVal -> alwaysShowHealthbar = newVal)
                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                .build();

        return YetAnotherConfigLib.create(ModConfig.HANDLER, (defaults, config, builder) -> {
                builder.title(Component.translatable("config.undertale_healthbars.title"));
                builder.category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.undertale_healthbars.category.rendering"))


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.modEnabled"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.modEnabled.description")))
                                .binding(true, () -> modEnabled, newVal -> modEnabled = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.showHealthbar"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.showHealthbar.description")))
                                .binding(true, () -> showHealthbar, newVal -> showHealthbar = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                                .listener(((opt, val) -> {optAlwaysShowHealthbar.setAvailable(val);}))
                                .build())

                        .option(optAlwaysShowHealthbar)


                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.maxDistance"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.maxDistance.description")))
                                .binding(MAXIMUM_MAX_DISTANCE, () -> maxDistance, newVal -> maxDistance = newVal)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.5f, MAXIMUM_MAX_DISTANCE).step(0.5f)
                                        .formatValue(val -> (val >= MAXIMUM_MAX_DISTANCE
                                                ? Component.literal("Infinite")
                                                : Component.literal(val + " block" + (val == 1f ? "" : "s"))
                                        )) // Format to show infinite if max distance, else show distance in blocks
                                )
                                .build())


                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.damageHealNumbersShowDuration"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.damageHealNumbersShowDuration.description")))
                                .binding(1.0f, () -> damageHealNumbersShowDuration, newVal -> damageHealNumbersShowDuration = newVal)
                                .controller(opt -> FloatFieldControllerBuilder.create(opt).formatValue(val -> Component.literal(val + " second" + (val == 1f ? "" : "s"))))
                                .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.showDamageNumbers"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.showDamageNumbers.description")))
                                .binding(true, () -> showDamageNumbers, newVal -> showDamageNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.showHealNumbers"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.showHealNumbers.description")))
                                .binding(true, () -> showHealNumbers, newVal -> showHealNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.healthbarOffset"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.healthbarOffset.description")))
                                .binding(0f, () -> healthbarOffset, newVal -> healthbarOffset = newVal)
                                .controller(opt -> FloatFieldControllerBuilder.create(opt).formatValue(val -> Component.literal(val + " block" + (val == 1f ? "" : "s"))))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.renderForYourself"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.renderForYourself.description")))
                                .binding(false, () -> renderForYourself, newVal -> renderForYourself = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())


                .build());

                builder.category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.undertale_healthbars.category.sounds"))

                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.damageSoundVolume"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.damageSoundVolume.description")))
                                .binding(100, () -> damageSoundVolume, newVal -> damageSoundVolume = newVal)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1).formatValue(ModConfig::formatVolume))
                        .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.healSoundVolume"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.healSoundVolume.description")))
                                .binding(100, () -> healSoundVolume, newVal -> healSoundVolume = newVal)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1).formatValue(ModConfig::formatVolume))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.damageSoundForYourself"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.damageSoundForYourself.description")))
                                .binding(false, () -> damageSoundForYourself, newVal -> damageSoundForYourself = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.healSoundForYourself"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.healSoundForYourself.description")))
                                .binding(false, () -> healSoundForYourself, newVal -> healSoundForYourself = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())



                .build());


                builder.category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.undertale_healthbars.category.bossBars"))


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.showUndertaleBossbars"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.showUndertaleBossbars.description")))
                                .binding(true, () -> showUndertaleBossbars, newVal -> showUndertaleBossbars = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.bossbarsDamageHealNumbersShowDuration"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.bossbarsDamageHealNumbersShowDuration.description")))
                                .binding(1.0f, () -> bossbarsDamageHealNumbersShowDuration, newVal -> bossbarsDamageHealNumbersShowDuration = newVal)
                                .controller(opt -> FloatFieldControllerBuilder.create(opt).formatValue(val -> Component.literal(val + " second" + (val == 1f ? "" : "s"))))
                                .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.showUndertaleBossbarDamageNumbers"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.showUndertaleBossbarDamageNumbers.description")))
                                .binding(true, () -> showUndertaleBossbarDamageNumbers, newVal -> showUndertaleBossbarDamageNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.showUndertaleBossbarHealNumbers"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.showUndertaleBossbarHealNumbers.description")))
                                .binding(true, () -> showUndertaleBossbarHealNumbers, newVal -> showUndertaleBossbarHealNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())
                .build());

                builder.category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.undertale_healthbars.category.extras"))

                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.vaporizedSoundVolume"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.vaporizedSoundVolume.description")))
                                .binding(0, () -> vaporizedSoundVolume, newVal -> vaporizedSoundVolume = newVal)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1).formatValue(ModConfig::formatVolume))
                        .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.undertale_healthbars.option.vaporizedSoundForYourself"))
                                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option.vaporizedSoundForYourself.description")))
                                .binding(false, () -> vaporizedSoundForYourself, newVal -> vaporizedSoundForYourself = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Component.literal("Yes") : Component.literal("No")).coloured(true))
                        .build())

                .build());

            builder.build();

            return builder;
        }).generateScreen(parent);
    }

    private static MutableComponent formatVolume(int val) {
        return val > 0 ?
                Component.literal(val + "%").withStyle(ChatFormatting.GREEN) :
                Component.literal("0% (Off)").withStyle(ChatFormatting.RED);
    }

    // Rendering
    @SerialEntry
    public static boolean modEnabled = true;
    @SerialEntry
    public static boolean showHealthbar = true;
    @SerialEntry
    public static boolean alwaysShowHealthbar = false;
    @SerialEntry
    public static float maxDistance = MAXIMUM_MAX_DISTANCE;
    @SerialEntry
    public static float damageHealNumbersShowDuration = 1.0f;
    @SerialEntry
    public static boolean showDamageNumbers = true;
    @SerialEntry
    public static boolean showHealNumbers = true;
    @SerialEntry
    public static float healthbarOffset = 0.0f;
    @SerialEntry
    public static boolean renderForYourself = false;

    // Sounds
    @SerialEntry
    public static int damageSoundVolume = 100;
    @SerialEntry
    public static int healSoundVolume = 100;
    @SerialEntry
    public static boolean damageSoundForYourself = false;
    @SerialEntry
    public static boolean healSoundForYourself = false;

    // Bossbars
    @SerialEntry
    public static boolean showUndertaleBossbars = true;
    @SerialEntry
    public static float bossbarsDamageHealNumbersShowDuration = 1.0f;
    @SerialEntry
    public static boolean showUndertaleBossbarDamageNumbers = true;
    @SerialEntry
    public static boolean showUndertaleBossbarHealNumbers = true;

    // Extras
    @SerialEntry
    public static int vaporizedSoundVolume = 0;
    @SerialEntry
    public static boolean vaporizedSoundForYourself = false;
}
