package net.cwjn.idf.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.cwjn.idf.ImprovedDamageFramework;
import net.cwjn.idf.util.Keybinds;
import net.cwjn.idf.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import static net.cwjn.idf.attribute.IDFAttributes.*;
import static net.cwjn.idf.damage.DamageHandler.armourFormula;
import static net.cwjn.idf.util.Color.*;
import static net.cwjn.idf.util.Util.numericalAttributeComponent;
import static net.cwjn.idf.util.Util.translationComponent;
import static net.minecraft.world.entity.ai.attributes.Attributes.*;

@OnlyIn(Dist.CLIENT)
public class StatsScreen extends Screen {

    private static final ResourceLocation ATTRIBUTES =
            new ResourceLocation(ImprovedDamageFramework.MOD_ID, "textures/gui/stat_screen/stat_screen_attributes.png");

    private static final DecimalFormat df = new DecimalFormat("##.#");
    private static final Style indicators = Style.EMPTY.withFont(ImprovedDamageFramework.FONT_INDICATORS);
    private int w;
    private int h;
    public static final DecimalFormat healthFormat = new DecimalFormat();
    private Player player;
    private Font font;

    static {
        healthFormat.setMinimumFractionDigits(1);
        healthFormat.setMaximumFractionDigits(1);
        healthFormat.setMinimumIntegerDigits(2);
    }

    public StatsScreen() {
        super(translationComponent("idf.stats_screen"));
    }

    @Override
    protected void init() {
        super.init();
        player = minecraft.player;
        font = minecraft.font;
        w = (minecraft.getWindow().getGuiScaledWidth() - 384)/2;
        h = (minecraft.getWindow().getGuiScaledHeight() - 512)/2 - 7;
        //122, 443
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        pPoseStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ATTRIBUTES);
        blit(pPoseStack, w, h, 0, 0, 384, 512, 384, 512);
        renderAttributes(pPoseStack, pMouseX, pMouseY);
        pPoseStack.popPose();
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    private void renderAttributes(PoseStack matrix, int mouseX, int mouseY) {
        InventoryScreen.renderEntityInInventory(w + 193, h + 387, 40, (float)(w + 192) - mouseX, (float)(h + 387 - 66) - mouseY, this.minecraft.player);
        drawCenteredString(font, matrix, player.getAttributeValue(ATTACK_DAMAGE), w+158, h+52.5f, PHYSICAL_COLOUR.getColor());
        drawCenteredPercentage(font, matrix, armourFormula(player.getAttributeValue(ARMOR)), w+232, h+52.5f, PHYSICAL_COLOUR.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(ARMOR_TOUGHNESS), w+306.5f, h+52.5f, PHYSICAL_COLOUR.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(FIRE_DAMAGE.get()), w+158, h+77.5f, FIRE_COLOUR.getColor());
        drawCenteredPercentage(font, matrix, armourFormula(player.getAttributeValue(FIRE_RESISTANCE.get())), w+232, h+77.5f, FIRE_COLOUR.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(WATER_DAMAGE.get()), w+158, h+102.5f, WATER_COLOUR.getColor());
        drawCenteredPercentage(font, matrix, armourFormula(player.getAttributeValue(WATER_RESISTANCE.get())), w+232, h+102.5f, WATER_COLOUR.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(LIGHTNING_DAMAGE.get()), w+158, h+127.5f, LIGHTNING_COLOUR.getColor());
        drawCenteredPercentage(font, matrix, armourFormula(player.getAttributeValue(LIGHTNING_RESISTANCE.get())), w+232, h+127.5f, LIGHTNING_COLOUR.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(MAGIC_DAMAGE.get()), w+158, h+152.5f, MAGIC_COLOUR.getColor());
        drawCenteredPercentage(font, matrix, armourFormula(player.getAttributeValue(MAGIC_RESISTANCE.get())), w+232, h+152.5f, MAGIC_COLOUR.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(DARK_DAMAGE.get()), w+158, h+177.5f, DARK_COLOUR.getColor());
        drawCenteredPercentage(font, matrix, armourFormula(player.getAttributeValue(DARK_RESISTANCE.get())), w+232, h+177.5f, DARK_COLOUR.getColor());
        drawCenteredString(font, matrix, (player.getAttributeValue(ATTACK_SPEED)), w+104, h+408, ChatFormatting.YELLOW.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(FORCE.get()), w+282, h+408, ChatFormatting.GRAY.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(CRUSH_MULT.get()) * 100, w+67f, h+211.5f,
                player.getAttributeValue(CRUSH_MULT.get()) > 1.0 ? ChatFormatting.RED.getColor() : ChatFormatting.GREEN.getColor());
        drawCenteredString(font, matrix, (player.getAttributeValue(STRIKE_MULT.get())*100), w+136f, h+211.5f,
                player.getAttributeValue(STRIKE_MULT.get()) > 1.0 ? ChatFormatting.RED.getColor() : ChatFormatting.GREEN.getColor());
        drawCenteredString(font, matrix, (player.getAttributeValue(PIERCE_MULT.get())*100), w+205f, h+211.5f,
                player.getAttributeValue(PIERCE_MULT.get()) > 1.0 ? ChatFormatting.RED.getColor() : ChatFormatting.GREEN.getColor());
        drawCenteredString(font, matrix, (player.getAttributeValue(SLASH_MULT.get())*100), w+274f, h+211.5f,
                player.getAttributeValue(SLASH_MULT.get()) > 1.0 ? ChatFormatting.RED.getColor() : ChatFormatting.GREEN.getColor());
        drawCenteredString(font, matrix, (player.getAttributeValue(GENERIC_MULT.get())*100), w+343f, h+211.5f,
                player.getAttributeValue(GENERIC_MULT.get()) > 1.0 ? ChatFormatting.RED.getColor() : ChatFormatting.GREEN.getColor());
        drawCenteredPercentage(font, matrix, player.getAttributeValue(CRIT_CHANCE.get()), w+104, h+318, ORANGE.getColor());
        drawCenteredPercentage(font, matrix, (player.getAttributeValue(ATTACK_KNOCKBACK)/0.4) * 100, w+104, h+363, VIOLET.getColor());
        drawCenteredPercentage(font, matrix, player.getAttributeValue(KNOCKBACK_RESISTANCE)*100, w+283, h+363, VIOLET.getColor());
        drawCenteredPercentage(font, matrix, player.getAttributeValue(PENETRATING.get()), w+223.5f, h+273, WHITESMOKE.getColor());
        drawCenteredString(font, matrix, Util.pBPS(player.getAttributeValue(MOVEMENT_SPEED)), w+162, h+454, ChatFormatting.DARK_GREEN.getColor());
        drawCenteredString(font, matrix, player.getAttributeValue(LUCK), w+223.5f, h+454, GREENYELLOW.getColor());
        drawCenteredString(font, matrix, Util.pBPS(player.getAttributeValue(MOVEMENT_SPEED)), w+162, h+454, ChatFormatting.DARK_GREEN.getColor());
        drawCenteredPercentage(font, matrix, player.getAttributeValue(EVASION.get()), w+283, h+318, ChatFormatting.DARK_GRAY.getColor());
        drawCenteredPercentage(font, matrix, player.getAttributeValue(LIFESTEAL.get()), w+162, h+273, ChatFormatting.DARK_RED.getColor());
        Util.drawCenteredString(font, matrix, Util.textComponent(df.format(player.getHealth()) + "/" + df.format(player.getMaxHealth())), w + 193.5f, h + 399, INDIANRED.getColor());
        //223.5, 273
    }

    private void drawCenteredString(Font font, PoseStack matrix, double value, float x, float y, int colour) {
        Util.drawCenteredString(font, matrix, numericalAttributeComponent(value).withStyle(indicators), x, y, colour);
    }

    private void drawCenteredPercentage(Font font, PoseStack matrix, double value, float x, float y, int colour) {
        Util.drawCenteredPercentageString(font, matrix, numericalAttributeComponent(value).withStyle(indicators), x, y, colour);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == Keybinds.openStats.getKey().getValue() || pKeyCode == KeyEvent.VK_E) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

}
