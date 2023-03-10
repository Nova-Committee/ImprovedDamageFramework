package net.cwjn.idf.event;

import com.google.common.collect.Multimap;
import net.cwjn.idf.ImprovedDamageFramework;
import net.cwjn.idf.gui.EquipmentInspectScreen;
import net.cwjn.idf.gui.StatScreen;
import net.cwjn.idf.gui.buttons.TabButton;
import net.cwjn.idf.util.Color;
import net.cwjn.idf.util.Keybinds;
import net.cwjn.idf.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.cwjn.idf.ImprovedDamageFramework.*;
import static net.cwjn.idf.gui.buttons.TabButton.TabType.INVENTORY;
import static net.cwjn.idf.gui.buttons.TabButton.TabType.STATS;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.*;

@Mod.EventBusSubscriber(modid = ImprovedDamageFramework.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventsForgeBus {

    private static final DecimalFormat hundredths = new DecimalFormat("#.##");
    private static final DecimalFormat tenths = new DecimalFormat("#.#");
    private static final Style ICON = Style.EMPTY.withFont(FONT_ICONS);
    private static final Style JRPG = Style.EMPTY.withFont(FONT_ALTIMA);

    public static void addInspectText(ItemTooltipEvent event) {
        ItemStack hoveredItem = event.getItemStack();
        if (hoveredItem.hasTag() && hoveredItem.getTag().contains("idf.equipment") && Minecraft.getInstance().player != null) {
            event.getToolTip().add(Component.translatable("idf.press_to_inspect"));
            if (Keybinds.inspectItem.isDown() && !(Minecraft.getInstance().screen instanceof EquipmentInspectScreen)) {
                Minecraft.getInstance().pushGuiLayer(new EquipmentInspectScreen(hoveredItem));
                Keybinds.inspectItem.setDown(false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        //get variables from event
        List<Component> list = event.getToolTip();
        ItemStack item = event.getItemStack();
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(item);
        LivingEntity owner = event.getEntity();
        boolean doAttributeTooltips = false;
        boolean isWeapon = false;
        //durability, damage class, and attack speed (if melee weapon)
        if (item.hasTag() && item.getTag().contains("idf.equipment")) {
            doAttributeTooltips = true;
            MutableComponent component = Util.textComponent("");
            component.append(Util.translationComponent("idf.icon.durability").withStyle(ICON));
            if (item.isDamageableItem()) {
                double percentage = (double)(item.getMaxDamage()-item.getDamageValue())/(double)item.getMaxDamage();
                component.append(Util.withColor(
                        Util.textComponent((int)(percentage*100) + "%"),
                        new Color((int) (128+(128*0.5*(1.0-percentage))), (int) (255*percentage), 0)));
            } else {
                component.append(Util.withColor(Util.translationComponent("idf.infinity.symbol"), Color.DARKSEAGREEN));
            }
            component.append(Util.withColor(Util.textComponent(" | "), Color.LIGHTGOLDENRODYELLOW));
            if (item.getTag().contains("idf.damage_class")) {
                isWeapon = true;
                component.append(Util.translationComponent("idf.icon.damage_class").withStyle(ICON));
                component.append(Util.translationComponent("idf.damage_class.tooltip." + item.getTag().getString("idf.damage_class")));
                if (!item.getTag().getBoolean("idf.ranged_weapon")) {
                    component.append(Util.withColor(Util.textComponent(" | "), Color.LIGHTGOLDENRODYELLOW));
                    component.append(Util.translationComponent("idf.icon.attack_speed").withStyle(ICON));
                    double atkSpd = item.getAttributeModifiers(slot).get(Attributes.ATTACK_SPEED).stream().
                            filter(m -> m.getOperation() == ADDITION).
                            mapToDouble(AttributeModifier::getAmount).
                            sum();
                    component.append(Util.textComponent(tenths.format(4 + atkSpd)));
                }
            } else {
                component.append(Util.translationComponent("idf.icon.defense").withStyle(ICON));
                double def = item.getAttributeModifiers(slot).get(Attributes.ARMOR_TOUGHNESS).stream().
                        filter(m -> m.getOperation() == ADDITION).
                        mapToDouble(AttributeModifier::getAmount).
                        sum();
                component.append(Util.textComponent(tenths.format(def)));
            }
            list.add(component);
        }
        if (doAttributeTooltips) {
            Multimap<Attribute, AttributeModifier> multimap = item.getAttributeModifiers(slot);
            List<Component> damage = new ArrayList<>();
            List<Component> resistance = new ArrayList<>();
            MutableComponent damageComponent = Util.textComponent("");
            damageComponent.append(Util.translationComponent("idf.icon.damage").withStyle(ICON));
            damageComponent.append(Util.translationComponent("idf.damage.tooltip").withStyle(ChatFormatting.BLUE));
            damage.add(damageComponent);
            MutableComponent resistanceComponent = Util.textComponent("");
            resistanceComponent.append(Util.translationComponent("idf.icon.resistance").withStyle(ICON));
            resistanceComponent.append(Util.translationComponent("idf.resistance.tooltip").withStyle(ChatFormatting.BLUE));
            resistance.add(resistanceComponent);
            List<Component> other = new ArrayList<>();
            for (Attribute a : multimap.keySet()) {
                String name = a.getDescriptionId().toLowerCase();
                if (isWeapon) {
                    if (name.contains("attack_speed")) continue;
                } else {
                    if (name.contains("armor_toughness")) continue;
                }
                MutableComponent component = Util.textComponent("");
                component.append(Util.translationComponent("idf.icon." + a.getDescriptionId()).withStyle(ICON));
                Collection<AttributeModifier> mods = multimap.get(a);
                if (slot.getType() == EquipmentSlot.Type.HAND && name.contains("damage")) {
                    final double flat = mods.stream().filter((modifier) -> modifier.getOperation().equals(ADDITION)).mapToDouble(AttributeModifier::getAmount).sum();
                    double mult = mods.stream().filter((modifier) -> modifier.getOperation().equals(MULTIPLY_TOTAL)).mapToDouble(AttributeModifier::getAmount).map((amount) -> amount + 1.0).reduce(1.0, (x, y) -> x * y);
                    double finalValue = flat*mult;
                    component.append(Util.textComponent(Util.threeDigit(hundredths.format(finalValue))).withStyle(JRPG));
                } else {
                    double flat = mods.stream().filter((modifier) -> modifier.getOperation().equals(ADDITION)).mapToDouble(AttributeModifier::getAmount).sum();
                    component.append(Util.textComponent(Util.threeDigit(hundredths.format(flat))));
                    double totalMult = mods.stream().filter((modifier) -> modifier.getOperation().equals(MULTIPLY_TOTAL)).mapToDouble(AttributeModifier::getAmount).map((amount) -> amount + 1.0).reduce(1.0, (x, y) -> x * y);
                    if (totalMult != 1) component.append(Util.textComponent(" + " + Util.threeDigit(hundredths.format(totalMult * 100)) + "%").withStyle(JRPG));
                }
                if (name.contains("damage")) {
                    damage.add(Util.textComponent(" ").append(component));
                } else if (name.contains("resistance") && !name.contains("knock") || name.contains("armor")) {
                    resistance.add(Util.textComponent(" ").append(component));
                } else {
                    other.add(component);
                }
            }
            while (!other.isEmpty()) {
                MutableComponent component = Util.textComponent("");
                for (int i = 1; i <= 3; ++i) {
                    component.append(other.remove(0));
                    if (!other.isEmpty() && i < 3) component.append(Util.withColor(Util.textComponent(" | "), Color.LIGHTGOLDENRODYELLOW));
                    if (other.isEmpty()) break;
                }
                list.add(component);
            }
            if (damage.size() > 1) list.addAll(damage);
            if (resistance.size() > 1) list.addAll(resistance);
        }
    }

    @SubscribeEvent
    public static void onInitGui(ScreenEvent.Init event) {
        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen || screen instanceof StatScreen) {
            int x = (screen.width - (screen instanceof CreativeModeInventoryScreen ? 195 : 176)) / 2 - 28;
            int y = (screen.height - (screen instanceof CreativeModeInventoryScreen ? 136 : 166)) / 2;
            event.addListener(new TabButton(x, y + 7, INVENTORY, !(screen instanceof StatScreen)));
            event.addListener(new TabButton(x, y + 36, STATS, (screen instanceof StatScreen)));
        }
    }

    @SubscribeEvent
    public static void openStatsScreen(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        onInput(minecraft, event.getKey(), event.getAction());
    }

    private static void onInput(Minecraft minecraft, int key, int action) {
        if (Keybinds.openStats.isDown() && minecraft.screen == null) {
            minecraft.setScreen(new StatScreen());
        }
    }

    public static boolean checkShiftDown() {
        return Screen.hasShiftDown();
    }

}
