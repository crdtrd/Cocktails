package com.drtdrc.cocktails.recipe;

import com.drtdrc.cocktails.Cocktails;
import com.nimbusds.oauth2.sdk.id.Identifier;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class CocktailsRecipeSerializer {
    public static final SpecialCraftingRecipe.SpecialRecipeSerializer<CocktailsRecipe> COCKTAIL_POTIONS = Registry.register(Registries.RECIPE_SERIALIZER, "cocktails:cocktail_potions", new SpecialCraftingRecipe.SpecialRecipeSerializer<>(CocktailsRecipe::new));
    public static void init() {
    }
}
