package net.cwjn.idf.config.json.data.subtypes;

import com.google.gson.*;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Type;

public record DefensiveData (double defense, double pRes, double fRes, double wRes, double lRes, double mRes, double dRes, double hRes,
                             double eva, double kbr, double str, double prc, double sls) {

    public static DefensiveData entityStandard() {
        return new DefensiveData(0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 1.0, 1.0, 1.0);
    }

    public static DefensiveData empty() {
        return new DefensiveData(0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0);
    }

    public static DefensiveData resistance(double d, double p, double f, double w, double l, double m, double da, double h) {
        return new DefensiveData(d, p, f, w, l, m, da, h, 0, 0, 0, 0, 0);
    }

    public static DefensiveData resistanceAndClass(double d, double p, double f, double w, double l, double m, double da, double h, double st, double pr, double sl) {
        return new DefensiveData(d, p, f, w, l, m, da, h, 0, 0, st, pr, sl);
    }

    public static DefensiveData combine (DefensiveData data1, DefensiveData data2) {
        return new DefensiveData(
                data1.defense + data2.defense,
                data1.pRes + data2.pRes,
                data1.fRes + data2.fRes,
                data1.wRes + data2.wRes,
                data1.lRes + data2.lRes,
                data1.mRes + data2.mRes,
                data1.dRes + data2.dRes,
                data1.hRes + data2.hRes,
                data1.eva + data2.eva,
                data1.kbr + data2.kbr,
                data1.str + data2.str,
                data1.prc + data2.prc,
                data1.sls + data2.sls);
    }

    public static DefensiveData read(FriendlyByteBuf buffer) {
        return new DefensiveData(
                buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),
                buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),
                buffer.readDouble(),buffer.readDouble(),buffer.readDouble()
        );
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(defense);
        buffer.writeDouble(pRes);
        buffer.writeDouble(fRes);
        buffer.writeDouble(wRes);
        buffer.writeDouble(lRes);
        buffer.writeDouble(mRes);
        buffer.writeDouble(dRes);
        buffer.writeDouble(hRes);
        buffer.writeDouble(eva);
        buffer.writeDouble(kbr);
        buffer.writeDouble(str);
        buffer.writeDouble(prc);
        buffer.writeDouble(sls);
    }

    public static class DefensiveDataSerializer implements JsonSerializer<DefensiveData>, JsonDeserializer<DefensiveData> {

        public DefensiveDataSerializer() {}

        @Override
        public DefensiveData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            return new DefensiveData(
                    obj.get("Weight").getAsDouble(),
                    obj.get("Defence - PHYSICAL").getAsDouble(),
                    obj.get("Defence - FIRE").getAsDouble(),
                    obj.get("Defence - WATER").getAsDouble(),
                    obj.get("Defence - LIGHTNING").getAsDouble(),
                    obj.get("Defence - MAGIC").getAsDouble(),
                    obj.get("Defence - DARK").getAsDouble(),
                    obj.get("Defence - HOLY").getAsDouble(),
                    obj.get("Evasion").getAsDouble(),
                    obj.get("Knockback Resistance").getAsDouble(),
                    obj.get("Damage Class - STRIKE").getAsDouble(),
                    obj.get("Damage Class - PIERCE").getAsDouble(),
                    obj.get("Damage Class - SLASH").getAsDouble());
        }

        @Override
        public JsonElement serialize(DefensiveData src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("Defence - PHYSICAL", src.pRes);
            obj.addProperty("Defence - FIRE", src.fRes);
            obj.addProperty("Defence - WATER", src.wRes);
            obj.addProperty("Defence - LIGHTNING", src.lRes);
            obj.addProperty("Defence - MAGIC", src.mRes);
            obj.addProperty("Defence - DARK", src.dRes);
            obj.addProperty("Defence - HOLY", src.hRes);
            obj.addProperty("Damage Class - STRIKE", src.str);
            obj.addProperty("Damage Class - PIERCE", src.prc);
            obj.addProperty("Damage Class - SLASH", src.sls);
            obj.addProperty("Weight", src.defense);
            obj.addProperty("Evasion", src.eva);
            obj.addProperty("Knockback Resistance", src.kbr);
            return obj;
        }
    }

}
