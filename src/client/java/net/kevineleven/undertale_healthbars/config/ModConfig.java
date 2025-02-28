package net.kevineleven.undertale_healthbars.config;


import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ModConfig {
        public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
                .id(Identifier.of(UndertaleHealthBarsClient.MOD_ID, "config"))
                .serializer(config -> GsonConfigSerializerBuilder.create(config)
                        .setPath(FabricLoader.getInstance().getConfigDir().resolve("undertale_healthbars.json5"))
                        .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                        .setJson5(true)
                        .build()
                )
                .build();

    public static Screen getScreen(Screen parent) {
        var optAlwaysShowHealthbar = Option.<Boolean>createBuilder()
                .name(Text.translatable("config.undertale_healthbars.option.alwaysShowHealthbar"))
                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.alwaysShowHealthbar.description")))
                .binding(false, () -> alwaysShowHealthbar, newVal -> alwaysShowHealthbar = newVal)
                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                .build();

        return YetAnotherConfigLib.create(ModConfig.HANDLER, (defaults, config, builder) -> {
                builder.title(Text.translatable("config.undertale_healthbars.title"));
                builder.category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.undertale_healthbars.category.rendering"))


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.modEnabled"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.modEnabled.description")))
                                .binding(true, () -> modEnabled, newVal -> modEnabled = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.showHealthbar"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.showHealthbar.description")))
                                .binding(true, () -> showHealthbar, newVal -> showHealthbar = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                                .listener(((opt, val) -> {optAlwaysShowHealthbar.setAvailable(val);}))
                                .build())

                        .option(optAlwaysShowHealthbar)


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.showDamageNumbers"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.showDamageNumbers.description")))
                                .binding(true, () -> showDamageNumbers, newVal -> showDamageNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.showHealNumbers"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.showHealNumbers.description")))
                                .binding(true, () -> showHealNumbers, newVal -> showHealNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.healthbarOffset"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.healthbarOffset.description")))
                                .binding(0f, () -> healthbarOffset, newVal -> healthbarOffset = newVal)
                                .controller(opt -> FloatFieldControllerBuilder.create(opt).formatValue(val -> Text.literal(val + " blocks")))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.renderForYourself"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.renderForYourself.description")))
                                .binding(false, () -> renderForYourself, newVal -> renderForYourself = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())


                .build());
                builder.category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.undertale_healthbars.category.sounds"))

                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.damageSoundVolume"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.damageSoundVolume.description")))
                                .binding(100, () -> damageSoundVolume, newVal -> damageSoundVolume = newVal)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1).formatValue(val -> Text.literal(val + "%")))
                        .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.healSoundVolume"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.healSoundVolume.description")))
                                .binding(100, () -> healSoundVolume, newVal -> healSoundVolume = newVal)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1).formatValue(val -> Text.literal(val + "%")))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.damageSoundForYourself"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.damageSoundForYourself.description")))
                                .binding(false, () -> damageSoundForYourself, newVal -> damageSoundForYourself = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.healSoundForYourself"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.healSoundForYourself.description")))
                                .binding(false, () -> healSoundForYourself, newVal -> healSoundForYourself = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())



                .build());


                builder.category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.undertale_healthbars.category.bossBars"))


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.showUndertaleBossbars"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.showUndertaleBossbars.description")))
                                .binding(true, () -> showUndertaleBossbars, newVal -> showUndertaleBossbars = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.showUndertaleBossbarDamageNumbers"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.showUndertaleBossbarDamageNumbers.description")))
                                .binding(true, () -> showUndertaleBossbarDamageNumbers, newVal -> showUndertaleBossbarDamageNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())


                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.undertale_healthbars.option.showUndertaleBossbarHealNumbers"))
                                .description(OptionDescription.of(Text.translatable("config.undertale_healthbars.option.showUndertaleBossbarHealNumbers.description")))
                                .binding(true, () -> showUndertaleBossbarHealNumbers, newVal -> showUndertaleBossbarHealNumbers = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).valueFormatter(val -> val ? Text.literal("Yes") : Text.literal("No")).coloured(true))
                        .build())
                .build());

            builder.build();

            return builder;
        }).generateScreen(parent);
    }

    // Rendering
    @SerialEntry
    public static boolean modEnabled = true;
    @SerialEntry
    public static boolean showHealthbar = true;
    @SerialEntry
    public static boolean alwaysShowHealthbar = false;
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
    public static boolean showUndertaleBossbarDamageNumbers = true;
    @SerialEntry
    public static boolean showUndertaleBossbarHealNumbers = true;
}
