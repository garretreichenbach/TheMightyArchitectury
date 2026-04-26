package com.timmie.mightyarchitect.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.function.Consumer;

public class Checkbox extends AbstractSimiWidget {

	private boolean checked;
	private Consumer<Boolean> onToggle;

	public Checkbox(int x, int y, boolean initial, Consumer<Boolean> onToggle) {
		super(x, y, 12, 12);
		this.checked = initial;
		this.onToggle = onToggle;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	public void renderWidget(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;
		this.isHovered = mouseX >= getX() && mouseY >= getY()
				&& mouseX < getX() + width && mouseY < getY() + height;

		int border = isHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
		int fill = 0xFF202020;
		int gx = getX();
		int gy = getY();
		ms.fill(gx, gy, gx + width, gy + height, border);
		ms.fill(gx + 1, gy + 1, gx + width - 1, gy + height - 1, fill);
		if (checked) {
			int c = 0xFF7AC74F;
			ms.fill(gx + 3, gy + 3, gx + width - 3, gy + height - 3, c);
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		checked = !checked;
		if (onToggle != null)
			onToggle.accept(checked);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		defaultButtonNarrationText(narrationElementOutput);
	}
}
