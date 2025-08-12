package com.drtdrc.cocktails.recipe;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.*;

public class CocktailsRecipe extends SpecialCraftingRecipe {
    public CocktailsRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        int potionCount = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getStackInSlot(i);
            if (s.isEmpty()) continue;

            Item it = s.getItem();
            boolean isPotion = it instanceof PotionItem || it instanceof SplashPotionItem || it instanceof LingeringPotionItem;
            if (!isPotion) return false;

            // theoretically filters out non effect potions
            PotionContentsComponent c = s.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            if (!c.hasEffects()) return false;

            potionCount++;
        }
        return potionCount >= 2;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        List<ItemStack> potionInputs = new ArrayList<>();
        boolean anyLingering = false, anySplash = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getStackInSlot(i);
            if (s.isEmpty()) continue;
            potionInputs.add(s);
            if (s.getItem() instanceof LingeringPotionItem) anyLingering = true;
            else if (s.getItem() instanceof SplashPotionItem) anySplash = true;
        }
        if (potionInputs.size() < 2) return ItemStack.EMPTY;


        // decide which status effects to apply to potion
        Map<StatusEffect, StatusEffectInstance> chosen = new HashMap<>();
        for (ItemStack p : potionInputs) {
            // p.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).forEachEffect(eff -> chooseStatusEffect(chosen, eff), 1.0f);
            p.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).forEachEffect(eff -> {

                StatusEffect key = eff.getEffectType().value();
                StatusEffectInstance current = chosen.get(key);
                if (current == null
                        || eff.getAmplifier() > current.getAmplifier()
                        || (eff.getAmplifier() == current.getAmplifier()
                            && eff.getDuration() > current.getDuration())) {

                    chosen.put(key, new StatusEffectInstance(
                            eff.getEffectType(),
                            eff.getDuration(),
                            eff.getAmplifier(),
                            eff.isAmbient(),
                            eff.shouldShowParticles(),
                            eff.shouldShowIcon()
                    ));

                }
            }, 1.0f);

        }

        int blended = blendColorFromEffects(new ArrayList<>(chosen.values()));
        // create potion data components
        PotionContentsComponent potionContentsOut = new PotionContentsComponent(Optional.empty(), Optional.of(blended), new ArrayList<>(chosen.values()), Optional.empty());

        // create potion item. lingering > splash > normal
        ItemStack potionOutput = new ItemStack(anyLingering ? Items.LINGERING_POTION : anySplash ? Items.SPLASH_POTION : Items.POTION);
        potionOutput.set(DataComponentTypes.POTION_CONTENTS, potionContentsOut);
        MutableText name = Text.literal(anyLingering ? "Lingering Cocktail" : anySplash ? "Splash Cocktail" : "Cocktail").styled(s -> s.withItalic(false));
        potionOutput.set(DataComponentTypes.CUSTOM_NAME, name);
        return potionOutput;
    }

    private static int blendColorFromEffects(Collection<StatusEffectInstance> effects) {
        if (effects.isEmpty()) return 0;

        long rSum = 0, gSum = 0, bSum = 0, wSum = 0;
        for (StatusEffectInstance inst : effects) {
            // Base color of the status effect
            int c = inst.getEffectType().value().getColor();
            int r = (c >> 16) & 0xFF;
            int g = (c >>  8) & 0xFF;
            int b =  c        & 0xFF;

            int w = inst.getAmplifier() + 1;
            rSum += (long) r * w;
            gSum += (long) g * w;
            bSum += (long) b * w;
            wSum += w;
        }
        if (wSum == 0) return 0;
        int r = (int)(rSum / wSum);
        int g = (int)(gSum / wSum);
        int b = (int)(bSum / wSum);
        return (r << 16) | (g << 8) | b;
    }


    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> rem = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);
        int seen = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getStackInSlot(i);
            if (s.isEmpty()) continue;
            Item it = s.getItem();
            if (it instanceof PotionItem || it instanceof SplashPotionItem || it instanceof LingeringPotionItem) {
                seen++;
                if (seen > 1) {
                    rem.set(i, new ItemStack(Items.GLASS_BOTTLE));
                }
            }
        }
        return rem;
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return CocktailsRecipeSerializer.COCKTAIL_POTIONS;
    }
}
