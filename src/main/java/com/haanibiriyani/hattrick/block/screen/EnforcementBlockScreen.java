package com.haanibiriyani.hattrick.block.screen;

import com.haanibiriyani.hattrick.block.menu.EnforcementBlockMenu;
import com.haanibiriyani.hattrick.network.ModNetwork;
import com.haanibiriyani.hattrick.network.UpdateEnforcementBlockPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnforcementBlockScreen extends AbstractContainerScreen<EnforcementBlockMenu> {
    private EditBox radiusField;
    private EditBox commandField;

    public EnforcementBlockScreen(EnforcementBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 120;
        this.imageWidth = 176;
        this.inventoryLabelY = 10000; // Hide inventory label
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Radius input
        radiusField = new EditBox(this.font, x + 80, y + 20, 60, 20, Component.literal("Radius"));
        radiusField.setValue(String.valueOf(menu.getBlockEntity().getRadius()));
        radiusField.setMaxLength(3);
        addRenderableWidget(radiusField);

        // Command input
        commandField = new EditBox(this.font, x + 10, y + 50, 156, 20, Component.literal("Command"));
        commandField.setValue(menu.getBlockEntity().getCommand());
        commandField.setMaxLength(256);
        commandField.setResponder(text -> {
            // Keep focus and don't close on typing
        });
        addRenderableWidget(commandField);

        // Save button
        addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
            int radius;
            try {
                radius = Integer.parseInt(radiusField.getValue());
            } catch (NumberFormatException e) {
                radius = 10;
            }

            String command = commandField.getValue();

            // Send packet to server
            ModNetwork.CHANNEL.sendToServer(new UpdateEnforcementBlockPacket(
                    menu.getBlockEntity().getBlockPos(),
                    radius,
                    command
            ));

            this.onClose();
        }).bounds(x + 10, y + 80, 156, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw background
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw labels
        graphics.drawString(this.font, "Radius (blocks):", x + 10, y + 20, 0x404040, false);
        graphics.drawString(this.font, "Command:", x + 10, y + 50, 0x404040, false);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If either text field is focused, handle input there and don't close the screen
        if (radiusField.isFocused() || commandField.isFocused()) {
            if (keyCode == 256) { // ESC key
                this.onClose();
                return true;
            }
            // Let the text field handle the key
            if (radiusField.keyPressed(keyCode, scanCode, modifiers) ||
                    commandField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // If either text field is focused, let it handle the character
        if (radiusField.isFocused() || commandField.isFocused()) {
            return radiusField.charTyped(codePoint, modifiers) ||
                    commandField.charTyped(codePoint, modifiers);
        }

        return super.charTyped(codePoint, modifiers);
    }
}