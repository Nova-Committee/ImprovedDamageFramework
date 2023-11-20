package net.cwjn.idf.config.json.records;

import com.google.gson.*;
import net.cwjn.idf.config.json.records.subtypes.AuxiliaryData;
import net.cwjn.idf.config.json.records.subtypes.DefenceData;
import net.cwjn.idf.config.json.records.subtypes.OffenseData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.Attribute;
import oshi.util.tuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static net.cwjn.idf.attribute.IDFAttributes.*;
import static net.minecraft.world.entity.ai.attributes.Attributes.*;

public record ItemData(OffenseData oData, DefenceData dData, AuxiliaryData aData)
        implements Iterable<Pair<Attribute, Double>> {

    @NotNull
    @Override
    public Iterator<Pair<Attribute, Double>> iterator() {
        ArrayList<Pair<Attribute, Double>> values = new ArrayList<>();
        values.add(new Pair<>(ATTACK_DAMAGE, oData.pDmg()));
        values.add(new Pair<>(FIRE_DAMAGE.get(), oData.fDmg()));
        values.add(new Pair<>(WATER_DAMAGE.get(), oData.wDmg()));
        values.add(new Pair<>(LIGHTNING_DAMAGE.get(), oData.lDmg()));
        values.add(new Pair<>(MAGIC_DAMAGE.get(), oData.mDmg()));
        values.add(new Pair<>(DARK_DAMAGE.get(), oData.dDmg()));
        values.add(new Pair<>(HOLY_DAMAGE.get(), oData.hDmg()));
        values.add(new Pair<>(ARMOR_TOUGHNESS, dData.weight()));
        values.add(new Pair<>(ARMOR, dData.pDef()));
        values.add(new Pair<>(FIRE_DEFENCE.get(), dData.fDef()));
        values.add(new Pair<>(WATER_DEFENCE.get(), dData.wDef()));
        values.add(new Pair<>(LIGHTNING_DEFENCE.get(), dData.lDef()));
        values.add(new Pair<>(MAGIC_DEFENCE.get(), dData.mDef()));
        values.add(new Pair<>(DARK_DEFENCE.get(), dData.dDef()));
        values.add(new Pair<>(HOLY_DEFENCE.get(), dData.hDef()));
        values.add(new Pair<>(LIFESTEAL.get(), oData.ls()));
        values.add(new Pair<>(PENETRATING.get(), oData.pen()));
        values.add(new Pair<>(CRIT_CHANCE.get(), oData.crit()));
        values.add(new Pair<>(CRIT_DAMAGE.get(), oData.critDmg()));
        values.add(new Pair<>(FORCE.get(), oData.force()));
        values.add(new Pair<>(ACCURACY.get(), oData.accuracy()));
        values.add(new Pair<>(ATTACK_KNOCKBACK, oData.kb()));
        values.add(new Pair<>(ATTACK_SPEED, oData.atkSpd()));
        values.add(new Pair<>(EVASION.get(), dData.eva()));
        values.add(new Pair<>(MAX_HEALTH, aData.hp()));
        values.add(new Pair<>(MOVEMENT_SPEED, aData.ms()));
        values.add(new Pair<>(KNOCKBACK_RESISTANCE, dData.kbr()));
        values.add(new Pair<>(LUCK, aData.luck()));
        values.add(new Pair<>(STRIKE_MULT.get(), dData.str()));
        values.add(new Pair<>(PIERCE_MULT.get(), dData.prc()));
        values.add(new Pair<>(SLASH_MULT.get(), dData.sls()));
        return values.iterator();
    }

    @Override
    public void forEach(Consumer<? super Pair<Attribute, Double>> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<Pair<Attribute, Double>> spliterator() {
        return Iterable.super.spliterator();
    }

    public static ItemData combine(ItemData data1, ItemData data2) {
        return new ItemData(OffenseData.combine(data1.oData, data2.oData),
                DefenceData.combine(data1.dData, data2.dData),
                AuxiliaryData.combine(data1.aData, data2.aData));
    }

    public static ItemData readData(FriendlyByteBuf buffer) {
        OffenseData newOData = OffenseData.read(buffer);
        DefenceData newDData = DefenceData.read(buffer);
        AuxiliaryData newAData = AuxiliaryData.read(buffer);
        return new ItemData(newOData, newDData, newAData);
    }

    public void writeData(FriendlyByteBuf buffer) {
        oData.write(buffer);
        dData.write(buffer);
        aData.write(buffer);
    }

    public static class ItemSerializer implements JsonSerializer<ItemData>, JsonDeserializer<ItemData> {

        public ItemSerializer() {}

        @Override
        public ItemData deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            return new ItemData(
                    ctx.deserialize(obj.get("Offense Stats"), OffenseData.class),
                    ctx.deserialize(obj.get("Defense Stats"), DefenceData.class),
                    ctx.deserialize(obj.get("Auxiliary Stats"), AuxiliaryData.class)
            );
        }

        @Override
        public JsonElement serialize(ItemData data, Type type, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();
            obj.add("Offense Stats", ctx.serialize(data.oData, OffenseData.class));
            obj.add("Defense Stats", ctx.serialize(data.dData, DefenceData.class));
            obj.add("Auxiliary Stats", ctx.serialize(data.aData, AuxiliaryData.class));
            return obj;
        }

    }

}
