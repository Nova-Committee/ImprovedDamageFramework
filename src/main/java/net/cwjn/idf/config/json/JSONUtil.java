package net.cwjn.idf.config.json;

import com.google.gson.*;
import net.cwjn.idf.ImprovedDamageFramework;
import net.cwjn.idf.config.json.records.*;
import net.cwjn.idf.config.json.records.subtypes.AuxiliaryData;
import net.cwjn.idf.config.json.records.subtypes.DefenceData;
import net.cwjn.idf.config.json.records.subtypes.OffenseData;
import net.cwjn.idf.iaf.RpgItemData;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Type;

@SuppressWarnings(value = "deprecation")
public class JSONUtil {
    public static final Gson SERIALIZER = new GsonBuilder().
            setPrettyPrinting().
            registerTypeAdapter(ArmourData.class, new ArmourData.ArmourSerializer()).
            registerTypeAdapter(WeaponData.class, new WeaponData.WeaponSerializer()).
            registerTypeAdapter(ItemData.class, new ItemData.ItemSerializer()).
            registerTypeAdapter(EntityData.class, new EntityData.EntityDataSerializer()).
            registerTypeAdapter(SourceCatcherData.class, new SourceCatcherData.SourceCatcherDataSerializer()).
            registerTypeAdapter(OffenseData.class, new OffenseData.OffensiveDataSerializer()).
            registerTypeAdapter(DefenceData.class, new DefenceData.DefensiveDataSerializer()).
            registerTypeAdapter(AuxiliaryData.class, new AuxiliaryData.AuxiliaryDataSerializer()).
            registerTypeAdapter(RpgItemData.StatObject.class, new RpgItemData.StatObject.StatObjectSerializer()).
            registerTypeAdapter(RpgItemData.class, new RpgItemData.RpgItemSerializer()).
            registerTypeAdapter(PresetData.class, new PresetData.PresetSerializer()).
            registerTypeAdapter(PresetData.AttributeAndModifier.class, new PresetData.AttributeAndModifierSerializer()).
            create();

    public static <T> T getOrCreateConfigFile(File configDir, String configName, T defaults, Type type) {

        File configFile = new File(configDir, configName);

        if (!configFile.exists()) {
            writeFile(configFile, defaults);
        }

        try {
            return SERIALIZER.fromJson(FileUtils.readFileToString(configFile), type);
        }
        catch (Exception e) {
            ImprovedDamageFramework.LOGGER.error("Error parsing config from json: " + configFile.toString(), e);
        }

        return null;
    }

    public static <T> T getConfigFile(File configDir, String configName, Type type) {
        File configFile = new File(configDir, configName);

        try {
            return SERIALIZER.fromJson(FileUtils.readFileToString(configFile), type);
        }
        catch (Exception e) {
            ImprovedDamageFramework.LOGGER.error("Error parsing config from json: " + configFile.toString(), e);
        }

        return null;
    }

    public static void writeFile(File outputFile, Object obj) {
        try {
            FileUtils.write(outputFile, SERIALIZER.toJson(obj));
        }
        catch (Exception e) {
            ImprovedDamageFramework.LOGGER.error("Error writing config file " + outputFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

}
