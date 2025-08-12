package com.drtdrc.cocktails;

import com.drtdrc.cocktails.recipe.CocktailsRecipeSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cocktails implements ModInitializer {
    public static final String MOD_ID = "cocktails";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Init Cocktails");
        CocktailsRecipeSerializer.init();
    }
}
