package com.timmie.mightyarchitect.gui;

import com.timmie.mightyarchitect.control.ArchitectManager;
import com.timmie.mightyarchitect.control.composition.CompositionLibrary;
import com.timmie.mightyarchitect.gui.widgets.Checkbox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class CompositionLibraryScreen extends AbstractSimiScreen {

	private static final int WIDTH = 320;
	private static final int ROW_HEIGHT = 22;
	private static final int HEADER_HEIGHT = 56;
	private static final int FOOTER_HEIGHT = 12;

	@Override
	public void init() {
		super.init();
		int rows = Math.max(1, CompositionLibrary.getEntries().size());
		int height = HEADER_HEIGHT + rows * ROW_HEIGHT + FOOTER_HEIGHT;
		setWindowSize(WIDTH, height);
		widgets.clear();

		Button save = Button.builder(Component.literal("Save current"), b -> promptSave())
				.pos(topLeftX + 8, topLeftY + 24)
				.size(120, 20)
				.build();
		save.active = canSaveCurrent();
		widgets.add(save);

		Button close = Button.builder(Component.literal("Close"), b -> minecraft.setScreen(null))
				.pos(topLeftX + WIDTH - 60 - 8, topLeftY + 24)
				.size(60, 20)
				.build();
		widgets.add(close);

		int y = topLeftY + HEADER_HEIGHT;
		for (CompositionLibrary.Entry entry : CompositionLibrary.getEntries()) {
			final UUID id = entry.composition.id;
			final String currentName = entry.composition.name;

			Checkbox visibilityToggle = new Checkbox(topLeftX + 8, y + 4, entry.visible, checked -> {
				CompositionLibrary.Entry e = CompositionLibrary.findById(id);
				if (e != null)
					e.visible = checked;
			});
			widgets.add(visibilityToggle);

			Button load = Button.builder(Component.literal("Load"), b -> {
				ArchitectManager.loadComposition(id);
				minecraft.setScreen(null);
			}).pos(topLeftX + WIDTH - 168, y).size(50, 20).build();
			widgets.add(load);

			Button rename = Button.builder(Component.literal("Rename"), b -> promptRename(id, currentName))
					.pos(topLeftX + WIDTH - 114, y)
					.size(54, 20)
					.build();
			widgets.add(rename);

			Button delete = Button.builder(Component.literal("Delete"), b -> {
				CompositionLibrary.remove(id);
				init();
			}).pos(topLeftX + WIDTH - 56, y).size(50, 20).build();
			widgets.add(delete);

			y += ROW_HEIGHT;
		}
	}

	private void promptRename(UUID id, String currentName) {
		TextInputPromptScreen prompt = new TextInputPromptScreen(
				newName -> {
					if (newName != null && !newName.isEmpty())
						CompositionLibrary.rename(id, newName);
					ScreenHelper.open(new CompositionLibraryScreen());
				},
				name -> ScreenHelper.open(new CompositionLibraryScreen()));
		prompt.setButtonTextConfirm("Rename");
		prompt.setButtonTextAbort("Cancel");
		prompt.setTitle("Rename composition:");
		prompt.setInitialText(currentName);
		ScreenHelper.open(prompt);
	}

	private boolean canSaveCurrent() {
		return ArchitectManager.getModel() != null
				&& ArchitectManager.getModel().getSketch() != null
				&& ArchitectManager.getModel().getMaterializedSketch() != null;
	}

	private void promptSave() {
		if (!canSaveCurrent())
			return;
		TextInputPromptScreen prompt = new TextInputPromptScreen(
				name -> {
					ArchitectManager.saveCurrentComposition(name);
					ScreenHelper.open(new CompositionLibraryScreen());
				},
				name -> ScreenHelper.open(new CompositionLibraryScreen()));
		prompt.setButtonTextConfirm("Save");
		prompt.setButtonTextAbort("Cancel");
		prompt.setTitle("Enter a name for the composition:");
		ScreenHelper.open(prompt);
	}

	@Override
	protected void renderWindow(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
		ms.fill(topLeftX - 2, topLeftY - 2, topLeftX + sWidth + 2, topLeftY + sHeight + 2, 0xFF202028);
		ms.fill(topLeftX, topLeftY, topLeftX + sWidth, topLeftY + sHeight, 0xFF303038);

		ms.drawString(font, Component.literal("Composition Library"),
				topLeftX + 8, topLeftY + 8, ScreenResources.FONT_COLOR);

		int y = topLeftY + HEADER_HEIGHT;
		Minecraft mc = Minecraft.getInstance();
		for (CompositionLibrary.Entry entry : CompositionLibrary.getEntries()) {
			ms.drawString(mc.font, entry.composition.name, topLeftX + 26, y + 6, 0xEEEEEE);
			y += ROW_HEIGHT;
		}

		if (CompositionLibrary.getEntries().isEmpty()) {
			ms.drawString(mc.font, Component.literal("No saved compositions yet."),
					topLeftX + 8, topLeftY + HEADER_HEIGHT + 6, 0xAAAAAA);
		}
	}
}
