package net.kevineleven.undertale_healthbars.config;


import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.kevineleven.undertale_healthbars.client.UndertaleHealthBarsClient;
import net.kevineleven.undertale_healthbars.util.Reference;
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
        var optAlwaysShowHealthbar = optBoolean(alwaysShowHealthbar, false, "alwaysShowHealthbar").build();

        return YetAnotherConfigLib.create(ModConfig.HANDLER, (defaults, config, builder) -> {
                builder.title(Component.translatable("config.undertale_healthbars.title"));
                builder.category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.undertale_healthbars.category.rendering"))
                        .option(optBoolean(modEnabled, true, "modEnabled").build())
                        .option(optBoolean(showHealthbar, true, "showHealthbar")
                                .listener(((opt, val) -> {optAlwaysShowHealthbar.setAvailable(val);})).build())
                        .option(optAlwaysShowHealthbar)

                        .option(optFloatSlider(maxDistance, "maxDistance", MAXIMUM_MAX_DISTANCE, 0.5f, MAXIMUM_MAX_DISTANCE, 0.5f,
                            val -> (val >= MAXIMUM_MAX_DISTANCE
                                    ? Component.literal("Infinite")
                                    : formatInBlocks(val)
                            ) // Format to show infinite if max distance, else show distance in blocks
                        ).build())

                        .option(optFloat(damageHealNumbersShowDuration, 1f, "damageHealNumbersShowDuration", ModConfig::formatInTime).build())
                        .option(optBoolean(showDamageNumbers, true, "showDamageNumbers").build())
                        .option(optBoolean(showHealNumbers, true, "showHealNumbers").build())
                        .option(optFloat(YOffset, 0f, "YOffset", ModConfig::formatInBlocks).build())
                        .option(optFloat(ZOffset, 0f, "ZOffset", ModConfig::formatInBlocks).build())
                        .option(optBoolean(renderForYourself, false, "renderForYourself").build())
                .build());

                builder.category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.undertale_healthbars.category.sounds"))

                        .option(optVolumeSlider(damageSoundVolume, 100, "damageSoundVolume").build())
                        .option(optVolumeSlider(healSoundVolume, 100, "healSoundVolume").build())

                        .option(optBoolean(damageSoundForYourself, false, "damageSoundForYourself").build())
                        .option(optBoolean(healSoundForYourself, false, "healSoundForYourself").build())

                        .option(optVolumeSlider(vaporizedSoundVolume, 100, "vaporizedSoundVolume").build())
                        .option(optBoolean(vaporizedSoundForYourself, false, "vaporizedSoundForYourself").build())
                .build());


                builder.category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.undertale_healthbars.category.bossBars"))

                        .option(optBoolean(showUndertaleBossbars, true, "showUndertaleBossbars").build())
                        .option(optFloat(bossbarsDamageHealNumbersShowDuration, 1f, "bossbarsDamageHealNumbersShowDuration", ModConfig::formatInTime).build())
                        .option(optBoolean(showUndertaleBossbarDamageNumbers, true, "showUndertaleBossbarDamageNumbers").build())
                        .option(optBoolean(showUndertaleBossbarHealNumbers, true, "showUndertaleBossbarHealNumbers").build())
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
    private static MutableComponent formatInBlocks(float val) {
        return Component.literal(val + " block" + (val == 1f ? "" : "s")); // Output "block(s)"
    }
    private static MutableComponent formatInTime(float val) {
        return Component.literal(val + " second" + (val == 1f ? "" : "s")); // Output "second(s)"
    }

    private static Option.Builder<Boolean> optBoolean(Reference<Boolean> variable, boolean default_value, String translation_name) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(
                        opt -> BooleanControllerBuilder.create(opt)
                                .formatValue(val -> val ? Component.literal("Yes") : Component.literal("No"))
                                .coloured(true)
                );
    }
    private static Option.Builder<Float> optFloat(Reference<Float> variable, float default_value, String translation_name, ValueFormatter<Float> formatter) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .formatValue(formatter)
                );
    }
    private static Option.Builder<Float> optFloat(Reference<Float> variable, float default_value, String translation_name) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> FloatFieldControllerBuilder.create(opt));
    }
    private static Option.Builder<Float> optFloatSlider(Reference<Float> variable, String translation_name, float default_value, float min, float max, float step, ValueFormatter<Float> formatter) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .formatValue(formatter)
                        .range(min, max).step(step)
                );
    }
    private static Option.Builder<Float> optFloatSlider(Reference<Float> variable, float default_value, String translation_name, float min, float max, float step) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(min, max).step(step)
                );
    }
    private static Option.Builder<Integer> optInt(Reference<Integer> variable, int default_value, String translation_name, ValueFormatter<Integer> formatter) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .formatValue(formatter)
                );
    }
    private static Option.Builder<Integer> optInt(Reference<Integer> variable, int default_value, String translation_name) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt));
    }
    private static Option.Builder<Integer> optIntSlider(Reference<Integer> variable, int default_value, String translation_name, int min, int max, int step, ValueFormatter<Integer> formatter) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .formatValue(formatter)
                        .range(min, max).step(step)
                );
    }
    private static Option.Builder<Integer> optIntSlider(Reference<Integer> variable, int default_value, String translation_name, int min, int max, int step) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.undertale_healthbars.option." + translation_name))
                .description(OptionDescription.of(Component.translatable("config.undertale_healthbars.option." + translation_name + ".description")))
                .binding(default_value, variable::get, variable::set)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(min, max).step(step)
                );
    }
    private static Option.Builder<Integer> optVolumeSlider(Reference<Integer> variable, int default_value, String translation_name) {
        return optIntSlider(variable, default_value, translation_name, 0, 100, 1, ModConfig::formatVolume);
    }

    // Rendering
    @SerialEntry
    public static Reference<Boolean> modEnabled = new Reference<>(true);
    @SerialEntry
    public static Reference<Boolean> showHealthbar = new Reference<>(true);
    @SerialEntry
    public static Reference<Boolean> alwaysShowHealthbar = new Reference<>(false);
    @SerialEntry
    public static Reference<Float> maxDistance = new Reference<>(MAXIMUM_MAX_DISTANCE);
    @SerialEntry
    public static Reference<Float> damageHealNumbersShowDuration = new Reference<>(1f);
    @SerialEntry
    public static Reference<Boolean> showDamageNumbers = new Reference<>(true);
    @SerialEntry
    public static Reference<Boolean> showHealNumbers = new Reference<>(true);
    @SerialEntry
    public static Reference<Float> YOffset = new Reference<>(0f);
    @SerialEntry
    public static Reference<Float> ZOffset = new Reference<>(0f);
    @SerialEntry
    public static Reference<Boolean> renderForYourself = new Reference<>(false);

    // Sounds
    @SerialEntry
    public static Reference<Integer> damageSoundVolume = new Reference<>(100);
    @SerialEntry
    public static Reference<Integer> healSoundVolume = new Reference<>(100);
    @SerialEntry
    public static Reference<Boolean> damageSoundForYourself = new Reference<>(false);
    @SerialEntry
    public static Reference<Boolean> healSoundForYourself = new Reference<>(false);
    @SerialEntry
    public static Reference<Integer> vaporizedSoundVolume = new Reference<>(100);
    @SerialEntry
    public static Reference<Boolean> vaporizedSoundForYourself = new Reference<>(false);

    // Bossbars
    @SerialEntry
    public static Reference<Boolean> showUndertaleBossbars = new Reference<>(true);
    @SerialEntry
    public static Reference<Float> bossbarsDamageHealNumbersShowDuration = new Reference<>(1f);
    @SerialEntry
    public static Reference<Boolean> showUndertaleBossbarDamageNumbers = new Reference<>(true);
    @SerialEntry
    public static Reference<Boolean> showUndertaleBossbarHealNumbers = new Reference<>(true);
}
