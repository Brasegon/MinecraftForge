/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     cpw - implementation
 */

package cpw.mods.fml.client;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.StringTranslate;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Strings;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/**
 * @author cpw
 *
 */
public class GuiModList extends GuiScreen
{
    private GuiScreen mainMenu;
    private GuiSlotModList modList;
    private int selected = -1;
    private ModContainer selectedMod;
    private int listWidth;
    private ArrayList<ModContainer> mods;

    /**
     * @param mainMenu
     */
    public GuiModList(GuiScreen mainMenu)
    {
        this.mainMenu=mainMenu;
        this.mods=new ArrayList<ModContainer>();
        FMLClientHandler.instance().addSpecialModEntries(mods);
        for (ModContainer mod : Loader.instance().getModList()) {
            if (mod.getMetadata()!=null && mod.getMetadata().parentMod==null && !Strings.isNullOrEmpty(mod.getMetadata().parent)) {
                String parentMod = mod.getMetadata().parent;
                ModContainer parentContainer = Loader.instance().getIndexedModList().get(parentMod);
                if (parentContainer != null)
                {
                    mod.getMetadata().parentMod = parentContainer;
                    parentContainer.getMetadata().childMods.add(mod);
                    continue;
                }
            }
            else if (mod.getMetadata()!=null && mod.getMetadata().parentMod!=null)
            {
            	 continue;
            }
            mods.add(mod);
        }
    }

    @Override
    public void func_73866_w_()
    {
        for (ModContainer mod : mods) {
            listWidth=Math.max(listWidth,getFontRenderer().func_78256_a(mod.getName()) + 10);
            listWidth=Math.max(listWidth,getFontRenderer().func_78256_a(mod.getVersion()) + 10);
        }
        listWidth=Math.min(listWidth, 150);
        StringTranslate translations = StringTranslate.func_74808_a();
        this.field_73887_h.add(new GuiSmallButton(6, this.field_73880_f / 2 - 75, this.field_73881_g - 38, translations.func_74805_b("gui.done")));
        this.modList=new GuiSlotModList(this, mods, listWidth);
        this.modList.registerScrollButtons(this.field_73887_h, 7, 8);
    }

    @Override
    protected void func_73875_a(GuiButton button) {
        if (button.field_73742_g)
        {
            switch (button.field_73741_f)
            {
                case 6:
                    this.field_73882_e.func_71373_a(this.mainMenu);
                    return;
            }
        }
        super.func_73875_a(button);
    }

    public int drawLine(String line, int offset, int shifty)
    {
        this.field_73886_k.func_78276_b(line, offset, shifty, 0xd7edea);
        return shifty + 10;
    }

    @Override
    public void func_73863_a(int p_571_1_, int p_571_2_, float p_571_3_)
    {
        this.modList.drawScreen(p_571_1_, p_571_2_, p_571_3_);
        this.func_73732_a(this.field_73886_k, "Mod List", this.field_73880_f / 2, 16, 0xFFFFFF);
        int offset = this.listWidth  + 20;
        if (selectedMod != null) {
            GL11.glEnable(GL11.GL_BLEND);
            if (!selectedMod.getMetadata().autogenerated) {
                int shifty = 35;
                if (!selectedMod.getMetadata().logoFile.isEmpty())
                {
                    List<Texture> texList = TextureManager.func_94267_b().func_94266_e(selectedMod.getMetadata().logoFile);
                    if (texList != null)// Potentially could not find the texture
                    {
                        Texture texture = texList.get(0);
                        texture.func_94277_a(0);
                        int top = 32;
                        Tessellator tess = Tessellator.field_78398_a;
                        tess.func_78382_b();
                        tess.func_78374_a(offset,                          top + texture.func_94284_b(), field_73735_i, 0, 1);
                        tess.func_78374_a(offset + texture.func_94282_c(), top + texture.func_94284_b(), field_73735_i, 1, 1);
                        tess.func_78374_a(offset + texture.func_94282_c(), top,                          field_73735_i, 1, 0);
                        tess.func_78374_a(offset,                          top,                          field_73735_i, 0, 0);
                        tess.func_78381_a();

                        shifty += 65;
                    }
                }
                this.field_73886_k.func_78261_a(selectedMod.getMetadata().name, offset, shifty, 0xFFFFFF);
                shifty += 12;

                shifty = drawLine(String.format("Version: %s (%s)", selectedMod.getDisplayVersion(), selectedMod.getVersion()), offset, shifty);
                shifty = drawLine(String.format("Mod ID: '%s' Mod State: %s", selectedMod.getModId(), Loader.instance().getModState(selectedMod)), offset, shifty);
                if (!selectedMod.getMetadata().credits.isEmpty()) {
                   shifty = drawLine(String.format("Credits: %s", selectedMod.getMetadata().credits), offset, shifty);
                }
                shifty = drawLine(String.format("Authors: %s", selectedMod.getMetadata().getAuthorList()), offset, shifty);
                shifty = drawLine(String.format("URL: %s", selectedMod.getMetadata().url), offset, shifty);
                shifty = drawLine(selectedMod.getMetadata().childMods.isEmpty() ? "No child mods for this mod" : String.format("Child mods: %s", selectedMod.getMetadata().getChildModList()), offset, shifty);
                this.getFontRenderer().func_78279_b(selectedMod.getMetadata().description, offset, shifty + 10, this.field_73880_f - offset - 20, 0xDDDDDD);
            } else {
                offset = ( this.listWidth + this.field_73880_f ) / 2;
                this.func_73732_a(this.field_73886_k, selectedMod.getName(), offset, 35, 0xFFFFFF);
                this.func_73732_a(this.field_73886_k, String.format("Version: %s",selectedMod.getVersion()), offset, 45, 0xFFFFFF);
                this.func_73732_a(this.field_73886_k, String.format("Mod State: %s",Loader.instance().getModState(selectedMod)), offset, 55, 0xFFFFFF);
                this.func_73732_a(this.field_73886_k, "No mod information found", offset, 65, 0xDDDDDD);
                this.func_73732_a(this.field_73886_k, "Ask your mod author to provide a mod mcmod.info file", offset, 75, 0xDDDDDD);
            }
            GL11.glDisable(GL11.GL_BLEND);
        }
        super.func_73863_a(p_571_1_, p_571_2_, p_571_3_);
    }

    Minecraft getMinecraftInstance() {
        return field_73882_e;
    }

    FontRenderer getFontRenderer() {
        return field_73886_k;
    }

    /**
     * @param var1
     */
    public void selectModIndex(int var1)
    {
        this.selected=var1;
        if (var1>=0 && var1<=mods.size()) {
            this.selectedMod=mods.get(selected);
        } else {
            this.selectedMod=null;
        }
    }

    public boolean modIndexSelected(int var1)
    {
        return var1==selected;
    }
}
