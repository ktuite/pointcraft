/*
 * Copyright (c) 2008-2011, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.washington.cs.games.ktuite.pointcraft.gui;

import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;

/**
 * 
 * @author Matthias Mann
 */
public class OnscreenInventoryPanel extends Widget {

	private int numSlotsX;
	private int numSlotsY;
	public final ItemSlot[] slot;

	private int slotSpacing;

	public OnscreenInventoryPanel(int numSlotsX, int numSlotsY) {
		this.numSlotsX = numSlotsX;
		this.numSlotsY = numSlotsY;
		this.slot = new ItemSlot[numSlotsX * numSlotsY];

		for (int i = 0; i < slot.length; i++) {
			slot[i] = new ItemSlot();
			add(slot[i]);
		}
	}

	public void setSlotFromMode(GunMode mode) {
		for (ItemSlot s : slot) {
			if (s.getGunMode() == mode && s.getItem() != null) {
				s.setDropState(true, true);
			} else {
				s.setDropState(false, false);
			}
		}
	
	}

	@Override
	public int getPreferredInnerWidth() {
		return (slot[0].getPreferredWidth() + slotSpacing) * numSlotsX
				- slotSpacing;
	}

	@Override
	public int getPreferredInnerHeight() {
		return (slot[0].getPreferredHeight() + slotSpacing) * numSlotsY
				- slotSpacing;
	}

	@Override
	protected void layout() {
		int slotWidth = slot[0].getPreferredWidth();
		int slotHeight = slot[0].getPreferredHeight();

		for (int row = 0, y = getInnerY(), i = 0; row < numSlotsY; row++) {
			for (int col = 0, x = getInnerX(); col < numSlotsX; col++, i++) {
				slot[i].adjustSize();
				slot[i].setPosition(x, y);
				x += slotWidth + slotSpacing;
			}
			y += slotHeight + slotSpacing;
		}
	}

	@Override
	protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		slotSpacing = themeInfo.getParameter("slotSpacing", 5);
	}

}
